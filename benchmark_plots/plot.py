<<<<<<< HEAD
import matplotlib.pyplot as plt
import numpy as np



def extract_query_times(filename, grouping=100, skip_row=0, skip_column=0):
    results = {}

    volume_sum = 0
    time_sum = 0
    
    file_input = open(filename, 'r')

    for ii in range(skip_row):
        file_input.readline()

    for line in file_input.readlines():
        line = line.split(',')
        query_response_volumn = int(line[skip_column])
        query_response_time = int(line[skip_column+1])

        volume_sum += query_response_volumn
        time_sum += query_response_time

        x = ((query_response_volumn // grouping) + 1) * grouping
        y = query_response_time / 10**6

        if x not in results:
            results[x] = []
        results[x].append(y)

    file_input.close()


    print(volume_sum/time_sum*10**9)

    return results


def decompose_query_times(filename, grouping=100, skip_row=0, skip_column=0):
    index_time_sum = 0
    document_time_sum = 0
    
    file_input = open(filename, 'r')

    for ii in range(skip_row):
        file_input.readline()

    for line in file_input.readlines():
        line = line.split(',')
        index_time = int(line[skip_column+2])
        document_time = int(line[skip_column+3])

        index_time_sum += index_time
        document_time_sum += document_time


    file_input.close()

    print(index_time_sum/(10**9), document_time_sum/(10**9))



def plot_line(data, label="", max_vol=100000):
    zs = sorted(list(data.keys()))

    xs = []
    for x in zs:
        if x <= max_vol:
            xs += [x]

    ys = []
    for x in xs:
        values = sorted(data[x])
        ys += [np.mean(values)]
    
    plt.plot(xs, ys, label=label)




benchmark_plaintext = extract_query_times('./benchmark_plaintext.txt', skip_row=0, skip_column=1)
benchmark_r2d2 = extract_query_times('./benchmark_DDR_SSE.txt', skip_row=4, skip_column=1)

decompose_query_times('./benchmark_DDR_SSE.txt', skip_row=4, skip_column=1)

plot_line(benchmark_r2d2, label="DDR-SSE")
plot_line(benchmark_plaintext, label="Plaintext", max_vol=max(benchmark_r2d2.keys()))


plt.yscale('log')
plt.legend()
plt.xlabel('Query Response Volume', fontsize=12)
plt.ylabel('Query Response Time (ms)', fontsize=12)

plt.xticks(fontsize=12)
plt.yticks(fontsize=12)

plt.show()
=======
import matplotlib.pyplot as plt
import numpy as np



def extract_query_times(filename, grouping=100, skip_row=0, skip_column=0):
    results = {}

    volume_sum = 0
    time_sum = 0
    
    file_input = open(filename, 'r')

    for ii in range(skip_row):
        file_input.readline()

    for line in file_input.readlines():
        line = line.split(',')
        query_response_volumn = int(line[skip_column])
        query_response_time = int(line[skip_column+1])

        volume_sum += query_response_volumn
        time_sum += query_response_time

        x = ((query_response_volumn // grouping) + 1) * grouping
        y = query_response_time / 10**6

        if x not in results:
            results[x] = []
        results[x].append(y)

    file_input.close()


    print(volume_sum/time_sum*10**9)

    return results


def decompose_query_times(filename, grouping=100, skip_row=0, skip_column=0):
    index_time_sum = 0
    document_time_sum = 0
    
    file_input = open(filename, 'r')

    for ii in range(skip_row):
        file_input.readline()

    for line in file_input.readlines():
        line = line.split(',')
        index_time = int(line[skip_column+2])
        document_time = int(line[skip_column+3])

        index_time_sum += index_time
        document_time_sum += document_time


    file_input.close()

    print(index_time_sum/(10**9), document_time_sum/(10**9))



def plot_line(data, label="", max_vol=100000):
    zs = sorted(list(data.keys()))

    xs = []
    for x in zs:
        if x <= max_vol:
            xs += [x]

    ys = []
    for x in xs:
        values = sorted(data[x])
        ys += [np.mean(values)]
    
    plt.plot(xs, ys, label=label)




benchmark_plaintext = extract_query_times('./benchmark_plaintext.txt', skip_row=0, skip_column=1)
benchmark_r2d2 = extract_query_times('./benchmark_DDR_SSE.txt', skip_row=4, skip_column=1)

decompose_query_times('./benchmark_DDR_SSE.txt', skip_row=4, skip_column=1)

plot_line(benchmark_r2d2, label="DDR-SSE")
plot_line(benchmark_plaintext, label="Plaintext", max_vol=max(benchmark_r2d2.keys()))


plt.yscale('log')
plt.legend()
plt.xlabel('Query Response Volume', fontsize=12)
plt.ylabel('Query Response Time (ms)', fontsize=12)

plt.xticks(fontsize=12)
plt.yticks(fontsize=12)

plt.show()
>>>>>>> bccd9ba9978cb8cfb4a4ee3cb7a46123bc50738e