import pickle
import time
from tqdm import tqdm
from cal_acc import calculate_acc_weighted
from run_single_attack import *
import os
def run_Ours_against_countermeasure(countermeasure,test_times=1,kws_uni_size=1000,\
                                                 datasets=["enron"],kws_extraction="sorted",observe_query_number_per_week = 500,\
                                                observe_weeks = 50,time_offset = 0,refspeed=5,beta=0.9):
    if not os.path.exists("./results"):
        os.makedirs("./results")
    if not os.path.exists("./results/test_against_countermeasures"):
        os.makedirs("./results/test_against_countermeasures")
    print("Test Ours against CLRZ and DDR-SSE")
    for dataset in datasets:
        if countermeasure =="padding_linear_2":
            if dataset == "wiki":
                Countermeasure_params = [
                    {"alg":"padding_linear_2","n":0},
                {"alg":"padding_linear_2","n":50000},
                {"alg":"padding_linear_2","n":100000},
                {"alg":"padding_linear_2","n":150000}]
            else:
                Countermeasure_params = [{"alg":"padding_linear_2","n":0},\
                {"alg":"padding_linear_2","n":500},
                {"alg":"padding_linear_2","n":1000},
                {"alg":"padding_linear_2","n":1500}]
        elif countermeasure == "obfuscation":
            if dataset == "wiki":

                Countermeasure_params=[{"alg":"obfuscation","p":1,"q":0,"m":1},\
                {"alg":"obfuscation","p":0.999,"q":0.1,"m":1},\
                {"alg":"obfuscation","p":0.999,"q":0.2,"m":1},\
                {"alg":"obfuscation","p":0.999,"q":0.3,"m":1}
                ]
            else:
                Countermeasure_params=[
                    #{"alg":"obfuscation","p":1,"q":0,"m":1},\
                    #{"alg":"obfuscation","p":0.999,"q":0.01,"m":1},\
                    #{"alg":"obfuscation","p":0.999,"q":0.02,"m":1},\
                    #{"alg":"obfuscation","p":0.999,"q":0.05,"m":1},\
                    {"alg":"obfuscation","p":0.999,"q":0.1,"m":1}
                    ]
        elif countermeasure == "padding_cluster":
            Countermeasure_params = [
                {"alg":"padding_cluster","knum_in_cluster":1},\
                {"alg":"padding_cluster","knum_in_cluster":2},
                {"alg":"padding_cluster","knum_in_cluster":4},
                {"alg":"padding_cluster","knum_in_cluster":8}]
        elif countermeasure == "padding_seal":
            Countermeasure_params = [
                {"alg":"padding_seal","n":1},
                {"alg":"padding_seal","n":2},
                {"alg":"padding_seal","n":3},
                {"alg":"padding_seal","n":4},
                ]
        elif countermeasure == "ddr-sse":
            Countermeasure_params = [
                {"alg":"ddr-sse","bkt":400}
                ]
        elif countermeasure == "none":
            Countermeasure_params = [
                {"alg":None}
                ]

            
        for countermeasure_params in Countermeasure_params:
            Our_Result = []
            Our_acc = []
            for i in tqdm(range(test_times)):
                our_attack_params={
                    "alg": "Ours",
                    "refinespeed":refspeed,
                    "alpha":0.1,
                    "beta":0.9,
                    "baseRec":15,
                    "confRec":10,
                    "step":3,
                    "no_F":False
                }
                if dataset == "wiki":
                    our_attack_params["refinespeed_exp"] = True
                else:
                    our_attack_params["refinespeed_exp"] = False

##################Our###################
                print()
                print(kws_uni_size,kws_uni_size,kws_extraction,observe_query_number_per_week,\
                    observe_weeks,time_offset,dataset,
                countermeasure_params,our_attack_params)
                result = run_single_attack(kws_uni_size,kws_uni_size,kws_extraction,observe_query_number_per_week,\
                    observe_weeks,time_offset,dataset,
                countermeasure_params,our_attack_params)
                
                data_for_acc_cal = result["data_for_acc_cal"]

                correct_count,acc,correct_id,wrong_id = \
                    calculate_acc_weighted(data_for_acc_cal,result["results"][0])
                print({"Ours step1:  dataset":dataset,"countermeasure_params":countermeasure_params,"acc":acc})

                correct_count,acc,correct_id,wrong_id = \
                    calculate_acc_weighted(data_for_acc_cal,result["results"][1])
                print({"Ours step2:  dataset":dataset,"countermeasure_params":countermeasure_params,"acc":acc})


                correct_count,acc,correct_id,wrong_id = \
                    calculate_acc_weighted(data_for_acc_cal,result["results"][2])
                print({"Ours:  dataset":dataset,"countermeasure_params":countermeasure_params,"acc":acc})
                
                Our_Result.append((dataset,countermeasure_params,acc,result))
                Our_acc.append(acc)
                


            if countermeasure_params["alg"] == "padding_linear_2":
                with open("./results/test_against_countermeasures/Ours_"+dataset+\
                    "_padding_linear_n_"+str(countermeasure_params["n"])+\
                    "_kws_uni_size_"+str(kws_uni_size)+\
                    "_test_times_"+str(test_times)+".pkl", "wb") as f:
                    pickle.dump(Our_Result,f)
            elif countermeasure_params["alg"] == "obfuscation":
                with open("./results/test_against_countermeasures/Ours_"+dataset+\
                    "_obfuscation_q_"+str(countermeasure_params["q"])+\
                    "_kws_uni_size_"+str(kws_uni_size)+\
                    "_test_times_"+str(test_times)+".pkl", "wb") as f:
                    pickle.dump(Our_Result,f)
            elif countermeasure_params["alg"] == "padding_cluster":
                with open("./results/test_against_countermeasures/Ours_"+dataset+\
                    "_padding_cluster_knum_in_cluster_"+str(countermeasure_params["knum_in_cluster"])+\
                    "_kws_uni_size_"+str(kws_uni_size)+\
                    "_test_times_"+str(test_times)+".pkl", "wb") as f:
                    pickle.dump(Our_Result,f)
            elif countermeasure_params["alg"] == "padding_seal":
                with open("./results/test_against_countermeasures/Ours_"+dataset+\
                    "_padding_seal_"+str(countermeasure_params["n"])+\
                    "_kws_uni_size_"+str(kws_uni_size)+\
                    "_test_times_"+str(test_times)+".pkl", "wb") as f:
                    pickle.dump(Our_Result,f)
            elif countermeasure_params["alg"] == "ddr-sse":
                with open("./results/test_against_countermeasures/Ours_"+dataset+\
                    "_ddrsse_"+str(countermeasure_params["bkt"])+\
                    "_kws_uni_size_"+str(kws_uni_size)+\
                    "_test_times_"+str(test_times)+".pkl", "wb") as f:
                    pickle.dump(Our_Result,f)
            elif countermeasure_params["alg"] == None:
                with open("./results/test_against_countermeasures/Ours_"+dataset+\
                    "_nodef"+\
                    "_kws_uni_size_"+str(kws_uni_size)+\
                    "_test_times_"+str(test_times)+".pkl", "wb") as f:
                    pickle.dump(Our_Result,f)
    return 0




if __name__ == "__main__":
    test_times = 30
    kws_uni_sizes = [100,500,1000]

    for kws_uni_size in kws_uni_sizes:
        run_Ours_against_countermeasure("none",         test_times=test_times, kws_uni_size=kws_uni_size, datasets=["enron"], kws_extraction="sorted")

    for kws_uni_size in kws_uni_sizes:
        run_Ours_against_countermeasure("obfuscation",  test_times=test_times, kws_uni_size=kws_uni_size, datasets=["enron"], kws_extraction="sorted")

    for kws_uni_size in kws_uni_sizes:
        run_Ours_against_countermeasure("ddr-sse",      test_times=test_times, kws_uni_size=kws_uni_size, datasets=["enron"], kws_extraction="sorted")

