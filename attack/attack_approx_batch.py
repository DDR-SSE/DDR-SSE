import sys
import argparse
import pickle
import random

import numpy as np
from scipy.stats import poisson
from scipy.stats import binom

import time


def process_leakage(filename_input):

    file_input = open(filename_input, 'r')

    keywords = []
    responses = []

    for line in file_input.readlines():
        entries = line.split(',')
        keywords += [entries[0]]
        response = [x.split('\n')[0] for x in entries[1:]]
        responses += [set(response)]

    comatrix = np.zeros((len(keywords), len(keywords)))

    for idx1 in range(len(responses)):
        for idx2 in range(len(responses)):
            comatrix[idx1, idx2] = len(responses[idx1].intersection(responses[idx2]))

    leakage = {
        'keywords': keywords,
        'comatrix': comatrix,
        }

    return leakage
        
    


def get_padded_counts_aux(aux_info, bucket_size):
    counts_aux = {}
    for idx, keyword in enumerate(aux_info['keywords']):
        counts_aux[keyword] = aux_info['comatrix'][idx, idx]

    keywords_sorted = sorted(aux_info['keywords'], key=lambda x: counts_aux[x], reverse=True)

    _padded_counts_aux = {}
    count = 0
    for idx, keyword in enumerate(keywords_sorted):
        if idx % bucket_size == 0:
            count = counts_aux[keyword]
        _padded_counts_aux[keyword] = count

    padded_counts_aux = [0 for ii in range(len(aux_info['keywords']))]
    for idx, keyword in enumerate(keywords_sorted):
        padded_counts_aux[idx] = _padded_counts_aux[keyword]


    diff_counts_aux = [0 for ii in range(len(aux_info['keywords']))]
    for idx, keyword in enumerate(aux_info['keywords']):
        diff_counts_aux[idx] = _padded_counts_aux[keyword] - counts_aux[keyword]
    

    return padded_counts_aux, diff_counts_aux


def get_padded_counts_aux_range(padded_counts_aux):
    padded_counts_aux_range = []

    for count in padded_counts_aux:
        std_div = np.sqrt(count)
        padded_counts_aux_range.append([count-3*std_div, count+3*std_div])

    return padded_counts_aux_range


def get_candidate_assignments(leakage, aux_info):
    bucket_size = leakage['bucket_size']

    padded_counts_aux, diff_counts_aux = get_padded_counts_aux(aux_info, bucket_size)
    padded_counts_aux_range = get_padded_counts_aux_range(padded_counts_aux)

    # get maximum off-diagonal cooccurrence counts for each keyword/query
    cocount_max_leakage = []
    cocount_max_aux = []
    
    for idx in range(len(leakage['keywords'])):
        val_tmp = 0
        if len(leakage['comatrix'][idx, :idx]) > 0:
            val_tmp = max(leakage['comatrix'][idx, :idx])
        if len(leakage['comatrix'][idx, idx+1:]) > 0:
            val_tmp = max(val_tmp, max(leakage['comatrix'][idx, idx+1:]))
        cocount_max_leakage += [val_tmp]


        val_tmp = 0
        if len(aux_info['comatrix'][idx, :idx]) > 0:
            val_tmp = max(aux_info['comatrix'][idx, :idx])
        if len(aux_info['comatrix'][idx, idx+1:]) > 0:
            val_tmp = max(val_tmp, max(aux_info['comatrix'][idx, idx+1:]))
        cocount_max_aux     += [val_tmp + 3*np.sqrt(val_tmp)]


    candidate_assignments = {}
    for idx_tar in range(len(leakage['keywords'])):
        candidate_assignments[idx_tar] = []
        count_tar = leakage['comatrix'][idx_tar, idx_tar]
        for idx_aux in range(len(padded_counts_aux)):
            if count_tar >= padded_counts_aux_range[idx_aux][0] and count_tar <= padded_counts_aux_range[idx_aux][1]:
                if cocount_max_leakage[idx_tar] <= cocount_max_aux[idx_aux]:
                    candidate_assignments[idx_tar] += [idx_aux]
    
    for idx_tar in range(len(leakage['keywords'])):
        if len(candidate_assignments[idx_tar]) == 0:
            candidate_assignments[idx_tar] = [ii for ii in range(len(padded_counts_aux))]

    return candidate_assignments, diff_counts_aux



def initial_assignment(candidate_assignments, aux_info):
    free_pool = [ii for ii in range(len(aux_info['comatrix']))]
    assignments = [None for ii in range(len(candidate_assignments))]
    assignments_rev = [None for ii in range(len(aux_info['comatrix']))]

    for idx_tar in candidate_assignments:
        idx_aux = random.choice(candidate_assignments[idx_tar])
        counter = 0
        while assignments_rev[idx_aux] != None and counter < 1000:
            idx_aux = random.choice(candidate_assignments[idx_tar])
            counter += 1

        if counter == 1000:
            idx_aux = random.choice(free_pool)

        assignments[idx_tar] = idx_aux
        assignments_rev[idx_aux] = idx_tar
        free_pool.remove(idx_aux)

    return assignments, assignments_rev



def update_aux_info(aux_info, diff_counts_aux):
    for idx1 in range(len(diff_counts_aux)):
        for idx2 in range(len(diff_counts_aux)):
            if idx1 == idx2:
                aux_info['comatrix'][idx1, idx2] += diff_counts_aux[idx1]
            else:
                aux_info['comatrix'][idx1, idx2] += diff_counts_aux[idx1] * diff_counts_aux[idx2] / aux_info['N_docs']

    return aux_info



def get_score_row(idx, assignments, leakage, aux_info):
    score_row = 0

    for idx2 in range(leakage['comatrix'].shape[0]):
        if idx == idx2:
            continue
        
        count_obs   = leakage['comatrix'][idx, idx2]
        count1      = aux_info['comatrix'][assignments[idx], assignments[idx]]
        count2      = aux_info['comatrix'][assignments[idx2], assignments[idx2]]
        lambda_aux  = aux_info['comatrix'][assignments[idx], assignments[idx2]]
        delta       = (count1 - lambda_aux)*(count2 - lambda_aux) / (count1 + 1) / (count2 + 1)


        score_local = 0
        if count_obs == 0:
            score_local = 0.5 * (1-delta) / (lambda_aux+1) + delta / (lambda_aux+2)
            score_local += (1-delta) / (lambda_aux+1) / (lambda_aux+1) + delta / (lambda_aux+2) / (lambda_aux+1)
        elif count_obs == lambda_aux:
            score_local = 0.5 * (1-delta) / (lambda_aux+1) + delta / (lambda_aux+2)
            score_local += (1-delta) / (lambda_aux+1) / (lambda_aux+1) + delta / (lambda_aux+2) / (lambda_aux+1)
        elif count_obs < lambda_aux:
            score_local = 1 / (lambda_aux+1) + (1-delta) / (lambda_aux+1) / (lambda_aux+1)

        if score_local != 0:
            score_row += np.log(score_local)
        else:
            score_row += -1000

    return score_row
    


def get_neighbour(assignments, assignments_rev, candidate_assignments):
    assignments_diff = {}
    
    idx = random.randrange(len(assignments))
    while len(candidate_assignments[idx]) == 1:
        idx = random.randrange(len(assignments))

    assignment_new = random.choice(candidate_assignments[idx])

    assignments_diff[idx] = assignment_new

    if assignments_rev[assignment_new] != None:
        assignments_diff[assignments_rev[assignment_new]] = assignments[idx]

    return assignments_diff

    

def get_correct_assignments(assignments, candidate_assignments, leakage, aux_info):
    for idx in candidate_assignments:
        keyword_leakage = leakage['keywords'][idx]
        for cand in range(len(aux_info['keywords'])):
            if aux_info['keywords'][cand] == keyword_leakage:
                assignments[idx] = cand

    return assignments



def attack(leakage, aux_info, N_iters, temp_initial=1000, rate=0.995, assignments_initial=None):
    candidate_assignments, diff_counts_aux = get_candidate_assignments(leakage, aux_info)
    print("Candidates found.")

    print("Avg. candidate set size: %.2f" % np.mean([len(candidate_assignments[x]) for x in candidate_assignments]))

    aux_info_padded = update_aux_info(aux_info, diff_counts_aux)
    print("Auxiliary info modified.")

    assignments, assignments_rev = [], []
    if assignments_initial == None:
        assignments, assignments_rev = initial_assignment(candidate_assignments, aux_info)
        print("Initial assignments identified.")
    else:
        assignments = assignments_initial
        assignments_rev = [None for idx in range(len(assignments))]
        for idx in range(len(assignments)):
            assignments_rev[assignments[idx]] = idx
            print("Initial assignments loaded.")


    scores_row = []
    for idx in range(len(assignments)):
        score_row = get_score_row(idx, assignments, leakage, aux_info)
        scores_row.append(score_row)

    print("Initial score computed.")
    print("Initial score: %.2f" % (sum(scores_row)))


    '''
    get_correct_assignments(assignments, candidate_assignments, leakage, aux_info)
    scores_row = []
    for idx in range(len(assignments)):
        score_row = get_score_row(idx, assignments, leakage, aux_info)
        scores_row.append(score_row)

    print("Correct score: %.2f" % (sum(scores_row)))
    '''

    time_start = time.time()

    temperature = temp_initial
    for _iter in range(N_iters):
        if _iter % (N_iters // 20) == 0:
            print(f"Iteration {_iter} / {N_iters}")
            print(f"Score: %.2f" % (sum(scores_row)))
            report(assignments, leakage, aux_info)

            time_end = time.time()
            print('Time taken: %.2f seconds' % (time_end - time_start))
            time_start = time.time()
            
        # generate a neighbour
        assignments_diff = get_neighbour(assignments, assignments_rev, candidate_assignments)

        # cache the old assignment
        assignments_original = {}
        for idx in assignments_diff:
            assignments_original[idx] = assignments[idx]

        # temporarily overwrites the assignment
        for idx in assignments_diff:
            assignments[idx] = assignments_diff[idx]

        # compute the difference in score
        scores_neighbour = {}
        for idx in assignments_diff:
            scores_neighbour[idx] = get_score_row(idx, assignments, leakage, aux_info)

        # check if the new assignment should be accepted
        score_diff = 0
        for idx in assignments_diff:
            score_diff += scores_neighbour[idx]
            score_diff -= scores_row[idx]

        if score_diff > 0 or np.exp(score_diff / temperature) >= random.random():
            for idx in assignments_diff:
                assignments_rev[assignments_original[idx]] = None
            for idx in assignments_diff:
                assignments_rev[assignments_diff[idx]] = idx
            
            for idx in assignments_diff:
                scores_row[idx]         = scores_neighbour[idx]
            
        else:
            for idx in assignments_diff:
                assignments[idx] = assignments_original[idx]

        temperature *= rate
        if temperature < 0.0000001:
            temperature = 0.0000001


    return assignments

    


def report(assignments, leakage, aux_info, filename=None):
    N_correct = 0
    for idx in range(leakage['comatrix'].shape[1]):
        keyword_leakage = leakage['keywords'][idx]
        keyword_aux     = aux_info['keywords'][assignments[idx]]

        if keyword_leakage == keyword_aux:
            N_correct += 1


    if filename == None:
        print(f"Correct: {N_correct}/{leakage['comatrix'].shape[1]} (%.2f %%)" % (N_correct/leakage['comatrix'].shape[1]*100))
    else:
        file_output = open(filename, "w")
        file_output.write(str(N_correct) + "," + str(leakage['comatrix'].shape[1]) + "\n")
        for idx in range(len(assignments)):
            file_output.write(str(idx) + "," + str(assignments[idx]) + "\n")
        file_output.close()
    


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--nd',     dest='N_docs',      action='store',     type=int, help="Specify the number of documents, default is 200,000.")
    parser.add_argument('--nqry',   dest='N_queries',   action='store',     type=int, help="Specify the number of queries, default is 1,200.")
    parser.add_argument('--nbkt',   dest='bucket_size', action='store',     type=int, help="Specify the bucket size, default is 200.")
    parser.add_argument('--pct',    dest='percentile',  action='store',     type=int, help="Specify the percentile, default is 100.")
    parser.add_argument('--nitr',   dest='N_iters',     action='store',     type=int, help="Specify the number of iterations to run the attack, default is 1,000,000.")
    parser.add_argument('--nrun',   dest='N_runs',      action='store',     type=int, help="Specify the number of runs, default is 10")

        
    args = parser.parse_args(sys.argv[1:])

    N_docs          = args.N_docs if args.N_docs != None else 200000
    N_queries       = args.N_queries if args.N_queries != None else 1200
    bucket_size     = args.bucket_size if args.bucket_size != None else 200
    percentile      = args.percentile if args.percentile != None else 100
    N_iters         = args.N_iters if args.N_iters != None else 1000000
    N_runs          = args.N_runs if args.N_runs != None else 10


    path_input = '../leakage/approximate/'
    path_output = '../attack_results/approximate/'


    aux_info, leakage = None, None
    try:
        file_input = open(path_input + f'aux_info_{N_docs}_{N_queries}_{percentile}.pkl', 'rb')
        aux_info = pickle.load(file_input)
        file_input.close()
    except:
        print("Auxiliary info with the specified parameters not found!")
    print("Aux info loaded.")

    try:
        leakage = process_leakage(path_input + f'leakage_{N_docs}_{N_queries}_{bucket_size}_{percentile}.txt')
        leakage['bucket_size'] = bucket_size
    except:
        print("Leakage with the specified parameters not found!")
    print("Leakage processed.")


    for run_idx in range(N_runs):
        print(f'Run {run_idx} start.')
        time_start = time.time()
        assignments = attack(leakage, aux_info, N_iters)
        filename_output = path_output+f"result_approx_{N_docs}_{N_queries}_{bucket_size}_{percentile}_{run_idx}.txt"
        report(assignments, leakage, aux_info, filename=filename_output)

        time_end = time.time()
        print('Total time taken: %.2f seconds' % (time_end - time_start))

