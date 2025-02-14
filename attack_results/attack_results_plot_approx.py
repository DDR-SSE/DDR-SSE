import matplotlib.pyplot as plt
import numpy as np





def read_results(bucket_size, percentile, mode='exact', N_runs=10):
    results = []

    for run in range(N_runs):
        file_input = None
        if mode == 'exact':
            file_input = open(f"./exact/result_exact_200000_1200_{bucket_size}_{percentile}_{run}.txt")
        else:
            file_input = open(f"./approximate/result_approx_200000_1200_{bucket_size}_{percentile}_{run}.txt")
        result = file_input.readline().split(',')
        file_input.close()

        results.append(int(result[0])/int(result[1])*100)

        
    return np.mean(results)

def plot_results(results_exact, results_approx):
    colors = {200: 'blue',
              400: 'darkorange'
              }
    plt.figure(figsize=(6, 4.5))
    
    for bucket_size in sorted(results_exact.keys()):
        percentiles = list(sorted(results_exact[bucket_size].keys(), reverse=True))
        xs          = [ii for ii in range(len(percentiles))]
        rates       = [results_exact[bucket_size][percentile] for percentile in percentiles]

        plt.scatter(xs, rates, label=str(bucket_size) + " (exact)", color=colors[bucket_size])


    for bucket_size in sorted(results_approx.keys()):
        percentiles = list(sorted(results_exact[bucket_size].keys(), reverse=True))
        xs          = [ii for ii in range(len(percentiles))]
        rates       = [results_approx[bucket_size][percentile] for percentile in percentiles]

        plt.scatter(xs, rates, label=str(bucket_size) + " (approximate)", color=colors[bucket_size], marker="x")

        

    plt.xticks(ticks=xs, labels=["100", "95", "90", "85", "80", "75"], size=12)
    plt.yticks(size=12)
    plt.xlabel("Percentile", size=14)
    plt.ylabel("Query Reconstruction Rate (%)", size=14)

    
    plt.legend(title="Bucket Size")
    plt.show()



bucket_sizes = [200, 400]
percentiles = [100, 95, 90, 85, 80, 75]

results_exact = {}
for bucket_size in bucket_sizes:
    results_exact[bucket_size] = {}
    for percentile in percentiles:
        results_exact[bucket_size][percentile] = read_results(bucket_size, percentile, mode='exact')


results_approx = {}
for bucket_size in bucket_sizes:
    results_approx[bucket_size] = {}
    for percentile in percentiles:
        results_approx[bucket_size][percentile] = read_results(bucket_size, percentile, mode='approx')

plot_results(results_exact, results_approx)
