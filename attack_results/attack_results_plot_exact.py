import matplotlib.pyplot as plt
import numpy as np





def read_results(bucket_size, percentile, N_runs=10):
    results = []

    for run in range(N_runs):
        file_input = open(f"./exact/result_exact_400000_1200_{bucket_size}_{percentile}_{run}.txt")
        result = file_input.readline().split(',')
        file_input.close()

        results.append(int(result[0])/int(result[1])*100)

        
    return np.mean(results)

def plot_results(results):
    plt.figure(figsize=(6, 4.5))
    
    for bucket_size in sorted(results.keys()):
        percentiles = list(sorted(results[bucket_size].keys(), reverse=True))
        xs          = [ii for ii in range(len(percentiles))]
        rates       = [results[bucket_size][percentile] for percentile in percentiles]

        plt.scatter(xs, rates, label=str(bucket_size))

    plt.xticks(ticks=xs, labels=["100", "95", "90", "85", "80", "75"], size=12)
    plt.yticks(size=12)
    plt.xlabel("Percentile", size=14)
    plt.ylabel("Query Reconstruction Rate (%)", size=14)

    
    plt.legend(title="Bucket Size")
    plt.show()



bucket_sizes = [50, 100, 200, 400]
percentiles = [100, 95, 90, 85, 80, 75]

results = {}
for bucket_size in bucket_sizes:
    results[bucket_size] = {}
    for percentile in percentiles:
        results[bucket_size][percentile] = read_results(bucket_size, percentile)

plot_results(results)
