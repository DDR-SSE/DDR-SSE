import matplotlib
matplotlib.rcParams['pdf.fonttype'] = 42
matplotlib.rcParams['ps.fonttype'] = 42

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


    print("Throughput: ", volume_sum/time_sum*10**9)

    return results


def extract_setup_times(filename):
    file_input = open(filename, 'r')

    for ii in range(3):
        file_input.readline()

    metadata_time = int(file_input.readline().split(':')[1])
    index_time = int(file_input.readline().split(':')[1])
    document_time = int(file_input.readline().split(':')[1])

    file_input.close()

    print('Metadata time: %.3f' % (metadata_time/(10**9)))
    print('Index time: %.3f' % (index_time/(10**9)))
    print('Document time: %.3f' % (document_time/(10**9)))
    print('Total: %.3f' % ((metadata_time+index_time+document_time)/10**9))
    

def decompose_query_times(filename, grouping=100, skip_row=0, skip_column=0):
    metadata_time_sum = 0
    index_time_sum = 0
    document_time_sum = 0
    
    file_input = open(filename, 'r')

    for ii in range(skip_row):
        file_input.readline()

    for line in file_input.readlines():
        line = line.split(',')
        metadata_time = int(line[skip_column+2])
        index_time = int(line[skip_column+3])
        document_time = int(line[skip_column+4])

        metadata_time_sum += metadata_time
        index_time_sum += index_time
        document_time_sum += document_time


    file_input.close()

    print('Metadata time: %.3f' % (metadata_time_sum/(10**9)))
    print('Index time: %.3f' % (index_time_sum/(10**9)))
    print('Document time: %.3f' % (document_time_sum/(10**9)))




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


print('-----------\nSetup')
extract_setup_times('./benchmark_DDR_SSE_4096.txt')
extract_setup_times('./benchmark_DDR_SSE_8192.txt')
extract_setup_times('./benchmark_DDR_SSE_16384.txt')


print('------------\nQuery Total')
decompose_query_times('./benchmark_DDR_SSE_4096.txt', skip_row=6, skip_column=1)

print('------------\nQuery Throughput')
benchmark_4k = extract_query_times('./benchmark_DDR_SSE_4096.txt', skip_row=6, skip_column=1)
benchmark_8k = extract_query_times('./benchmark_DDR_SSE_8192.txt', skip_row=6, skip_column=1)
benchmark_16k = extract_query_times('./benchmark_DDR_SSE_16384.txt', skip_row=6, skip_column=1)

plot_line(benchmark_4k, label="4KB")
plot_line(benchmark_8k, label="8KB")
plot_line(benchmark_16k, label="16KB")



plt.yscale('log')
plt.legend(title="Document size")
plt.xlabel('Query Response Volume', fontsize=12)
plt.ylabel('Query Response Time (ms)', fontsize=12)

plt.xticks(fontsize=12)
plt.yticks(fontsize=12)

plt.show()
