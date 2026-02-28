import argparse
import pickle
import attack_exact_batch as attack
import multiprocessing
import time

def run_attack(args_raw):
    parser = argparse.ArgumentParser()
    parser.add_argument('--nd',     dest='N_docs',      action='store',     type=int, help="Specify the number of documents, default is 400,000.")
    parser.add_argument('--nqry',   dest='N_queries',   action='store',     type=int, help="Specify the number of queries, default is 1,200.")
    parser.add_argument('--nbkt',   dest='bucket_size', action='store',     type=int, help="Specify the bucket size, default is 200.")
    parser.add_argument('--pct',    dest='percentile',  action='store',     type=int, help="Specify the percentile, default is 100.")
    parser.add_argument('--nitr',   dest='N_iters',     action='store',     type=int, help="Specify the number of iterations to run the attack, default is 1,000,000.")
    parser.add_argument('--nrun',   dest='N_runs',      action='store',     type=int, help="Specify the number of runs, default is 10")

    args = parser.parse_args(args_raw)

    N_docs          = args.N_docs if args.N_docs != None else 400000
    N_queries       = args.N_queries if args.N_queries != None else 1200
    bucket_size     = args.bucket_size if args.bucket_size != None else 200
    percentile      = args.percentile if args.percentile != None else 100
    N_iters         = args.N_iters if args.N_iters != None else 1000000
    N_runs          = args.N_runs if args.N_runs != None else 10


    path_input = '../leakage/exact/'
    path_output = '../attack_results/exact/'

    aux_info, leakage = None, None
    try:
        file_input = open(path_input + f'aux_info_{N_docs}_{N_queries}_{percentile}.pkl', 'rb')
        aux_info = pickle.load(file_input)
        file_input.close()
    except:
        print("Auxiliary info with the specified parameters not found!")
    print("Aux info loaded.")

    try:
        leakage = attack.process_leakage(path_input + f'leakage_{N_docs}_{N_queries}_{bucket_size}_{percentile}.txt')
        leakage['bucket_size'] = bucket_size
    except:
        print("Leakage with the specified parameters not found!")
    print("Leakage processed.")


    for run_idx in range(N_runs):
        print(f'Run {run_idx} start.')
        time_start = time.time()
        assignments = attack.attack(leakage, aux_info, N_iters)
        filename_output = path_output+f"result_exact_{N_docs}_{N_queries}_{bucket_size}_{percentile}_{run_idx}.txt"
        attack.report(assignments, leakage, aux_info, filename=filename_output)

        time_end = time.time()
        print('Total time taken: %.2f seconds' % (time_end - time_start))


if __name__ == "__main__":
    args_list = []
    
    bucket_sizes = [50, 100, 200, 400]
    percentiles = [100, 95, 90, 85, 80, 75]


    for bkt in bucket_sizes:
        for pct in percentiles:
            args = f"--nbkt {bkt} --pct {pct}"
            args = args.split(' ')
            args_list.append(args)
    
    pool = multiprocessing.Pool(6)
    pool.map(run_attack, args_list, chunksize=1)
    print("Done")
