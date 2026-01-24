import re
import os
import sys
import pickle
import math
import json
import random

import time

import nltk
from nltk.tokenize import word_tokenize
from nltk.corpus import words

import numpy as np

import gzip
import argparse

#
# Precompiled patterns for performance
#
time_pattern = re.compile("Date: (?P<data>[A-Z][a-z]+\, \d{1,2} [A-Z][a-z]+ \d{4} \d{2}\:\d{2}\:\d{2} \-\d{4} \([A-Z]{3}\))")
subject_pattern = re.compile("Subject: (?P<data>.*)")
sender_pattern = re.compile("From: (?P<data>.*)")
recipient_pattern = re.compile("To: (?P<data>.*)")
cc_pattern = re.compile("cc: (?P<data>.*)")
bcc_pattern = re.compile("bcc: (?P<data>.*)")
msg_start_pattern = re.compile("\n\n", re.MULTILINE)
msg_end_pattern = re.compile("\n+.*\n\d+/\d+/\d+ \d+:\d+ [AP]M", re.MULTILINE)


'''
# Function to extract enron email main body
'''
feeds = []

def parse_email(pathname):
    with open(pathname) as TextFile:
        text = TextFile.read().replace("\r", "")
        try:
            time = time_pattern.search(text).group("data").replace("\n", "")
            subject = subject_pattern.search(text).group("data").replace("\n", "")

            sender = sender_pattern.search(text).group("data").replace("\n", "")

            recipient = recipient_pattern.search(text).group("data").split(", ")
            cc = cc_pattern.search(text).group("data").split(", ")
            bcc = bcc_pattern.search(text).group("data").split(", ")
            msg_start_iter = msg_start_pattern.search(text).end()
            try:
                msg_end_iter = msg_end_pattern.search(text).start()
                message = text[msg_start_iter:msg_end_iter]
            except AttributeError: # not a reply
                message = text[msg_start_iter:]
            message = re.sub("[\n\r]", " ", message)
            message = re.sub("  +", " ", message)
        except AttributeError:
            logging.error("Failed to parse %s" % pathname) 
            return None
            
        return message



'''
# Function to extract keyword frequencies from the emails
# You only need to use this if you want to get the frequency of the keywords
'''
def get_keyword_frequency(path):
    frequencies = {}
    total = 0

    for folder in os.listdir(path):
        if os.path.isfile(os.path.join(path, folder)):
            continue

        for sub_folder in os.listdir(os.path.join(path, folder)):
            if os.path.isfile(os.path.join(path, folder, sub_folder)):
                continue

            for filename in os.listdir(os.path.join(path, folder, sub_folder)):
                if os.path.isfile(os.path.join(path, folder, sub_folder, filename)):
                    main_body = parse_email(os.path.join(path, folder, sub_folder, filename))
                    total += 1

                    keywords = word_tokenize(main_body)
                    keywords = set([w.lower() for w in keywords])

                    for keyword in keywords:
                        if keyword not in frequencies:
                            frequencies[keyword] = 0
                        frequencies[keyword] += 1


    return frequencies, total
    
    


'''
# Function to extract the keywords from the emails and dump the keywords and the email body into a new file
'''
def build_inverted_index(path_input, include_keywords, N_files):
    inverted_index_target = {}
    for keyword in include_keywords:
        inverted_index_target[keyword] = []

    documents_target = []

    file_ctr = 0
    file_ctr2 = 0

    
    
    for folder in os.listdir(path_input):
        if os.path.isfile(os.path.join(path_input, folder)):
            continue


        for sub_folder in os.listdir(os.path.join(path_input, folder)):
            if os.path.isfile(os.path.join(path_input, folder, sub_folder)):
                continue

            for filename in os.listdir(os.path.join(path_input, folder, sub_folder)):
                
                if os.path.isfile(os.path.join(path_input, folder, sub_folder, filename)):
                    file_input = open(os.path.join(path_input, folder, sub_folder, filename), 'r')
                    full_text = file_input.read()
                    file_input.close()

                    
                    main_body = parse_email(os.path.join(path_input, folder, sub_folder, filename))

                    compressed_body = gzip.compress(bytes(main_body, 'utf-8'))
                    compressed_body_len = len(compressed_body)
                    dup_counter = 1
                    if compressed_body_len > DOC_LEN:
                        dup_counter = int(compressed_body_len // DOC_LEN * 1.3)

                    keywords = word_tokenize(main_body)
                    keywords = set([w.lower() for w in keywords if w.lower() in include_keywords])

                    for chunk_idx in range(dup_counter):
                        for keyword in keywords:
                            inverted_index_target[keyword] += [file_ctr2]

                        chunk_size  = int(math.ceil(len(main_body) / dup_counter))
                        start_idx   = chunk_idx * chunk_size
                        end_idx     = (chunk_idx+1) * chunk_size
                        documents_target += [main_body[start_idx:end_idx]]
                        file_ctr2 += 1

                    file_ctr += 1

                    if file_ctr % (N_docs//20) == 0:
                        print(f"Progress: {file_ctr}/{N_docs} documents processed.")

                    if file_ctr >= N_files:
                        return inverted_index_target, documents_target




def dump_inverted_index(inverted_index, path_output):
    file_output = open(path_output + f'inveted_index_{N_docs}_{DOC_LEN}.txt', 'w')

    for keyword in include_keywords:
        if len(inverted_index[keyword]) > 0:
            file_output.write(keyword + ',' + ','.join(map(str, inverted_index[keyword])) + '\n')

    file_output.close()



def dump_auxiliary_info(inverted_index_target, N_aux_keywords, N_docs, path_output1, path_output2):
    keywords = list(inverted_index_target.keys())
    keywords = sorted(keywords, key=lambda x:len(inverted_index_target[x]), reverse=True)

    percentiles = [100, 95, 90, 85, 80, 75]

    for percentile in percentiles:
        file_output = open(path_output2 + f"aux_info_{N_docs}_{N_aux_keywords}_{percentile}.pkl", 'wb')

        start_idx = (100-percentile)*len(keywords)//100
        end_idx = start_idx + N_aux_keywords
        keywords_used = keywords[start_idx:end_idx]


        for keyword in keywords_used:
            inverted_index_target[keyword] = set(inverted_index_target[keyword])

        aux_info = {}
        aux_info['N_docs']   = N_docs
        aux_info['keywords'] = keywords_used
        aux_info['comatrix'] = np.zeros((len(keywords_used), len(keywords_used)), dtype=np.uint)

        for idx1, keyword1 in enumerate(keywords_used):
            for idx2, keyword2 in enumerate(keywords_used):
                aux_info['comatrix'][idx1, idx2] = len(inverted_index_target[keyword1].intersection(inverted_index_target[keyword2]))

        pickle.dump(aux_info, file_output)
        file_output.close()

        file_output = open(path_output1 + f"queries_{N_docs}_{N_aux_keywords}_{percentile}.txt", 'w')
        for keyword in keywords_used:
            file_output.write(keyword + '\n')
        file_output.close()
    



def dump_documents(documents_target, path_output):
    file_output = open(path_output + f'emails_{N_docs}_{DOC_LEN}.txt', 'w')

    for document in documents_target:
        file_output.write("NEW_FILE\n")
        file_output.write(document + "\n")

    file_output.close()
                    
    

nltk.download('punkt_tab')
nltk.download('punkt')
nltk.download('words')



# parsing arguments
parser = argparse.ArgumentParser()
parser.add_argument('--ndoc', action='store', type=int, dest='ndoc', required=True, help="The number of documents used in the attack.")
parser.add_argument('--nkw',  action='store', type=int, dest='nkw', required=False, help="The number of keywords used in the attack. The default is 1,200.")
parser.add_argument('--padding',  action='store', type=int, dest='padding', required=False, help="The padded length of each document. The default is 4,096.")
args = parser.parse_args(sys.argv[1:])
print(f"Arguments used: {args}")
    


N_docs = 400000
if args.ndoc != None:
    N_docs = args.ndoc

N_aux_keywords = 1200
if args.nkw != None:
    N_aux_keywords = args.nkw

DOC_LEN = 4096
if args.padding != None:
    DOC_LEN = args.padding


file_input = open('./include_keywords.txt', 'r', encoding='windows-1252')
text = file_input.read()
file_input.close()
include_keywords = set(text.split(','))


path_input = '../emails_raw/maildir/'
path_output1 = '../emails_parsed/'
path_output2 = '../leakage/exact/'

time_start = time.time()
inverted_index_target, documents_target = build_inverted_index(path_input, include_keywords, N_docs)

dump_inverted_index(inverted_index_target, path_output1)
dump_auxiliary_info(inverted_index_target, N_aux_keywords, N_docs, path_output1, path_output2)
dump_documents(documents_target, path_output1)
time_end = time.time()
print('Time taken: %.2f seconds' % (time_end - time_start))
