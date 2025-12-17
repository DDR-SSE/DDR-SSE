# Overview
You can follow the steps below to reproduce our experimental results.

## Components
The repository has a few components:
1. Enron email parser.
2. DDR-SSE benchmark.
3. DDR-SSE leakage extraction (for our leakage-abuse attack).
4. Our leakage-abuse attack against DDR-SSE.
5. State-of-the-art attacks (SAP, IHOP and Jigsaw) adapted against DDR-SSE.


## Resources Required
| Task                                 | Human Time | Computer Time | Storage |  Memory   |
|--------------------------------------|------------|:-------------:|---------|:---------:|
| Download the Enron email corpus      | 5 minutes  | 20 minutes    | 2 GB    | -         |
| Parse the Enron emails               | 5 minutes  | 25 minutes    | 4 GB    | -         |
| Benchmark DDR-SSE                    | 5 minutes  | 50 minutes    | -       | 64 GB\*   |
| Leakage extraction                   | 5 minutes  | 50 minutes    | 1 GB    | 64 GB     |
| Running our attack                   | 10 minutes | 6 days \**    | -       | 8 GB      |
| Running the SAP attack               | 5 minutes  | 1 hour        | -       | 8 GB      |
| Running the IHOP attack              | 5 minutes  | 3 hours       | -       | 8 GB      |
| Running the Jigsaw attack            | 5 minutes  | 2 hours       | -       | 8 GB      |

\* The memory requirement is an overkill. This is set so as to reduce the amount of garbage collection during the benchmark. The implementation in the repository is a proof-of-concept. It is possible to make an implementation which has a much smaller memory footprint.

\** Due to the number of experiments in total. These can be parallelized. It takes ~1 day x 6 jobs in the way it is structured in the script.


## Software Requirements
The following softwares are required to run the experiments.
- Java (JDK 17 and above): https://www.oracle.com/java/technologies/downloads/.
- Python (3.8 and above): https://www.python.org/downloads/.
    - non-standard Python moduels used: nltk, numpy, pandas, matplotlib. These can be installed using [pip](https://pypi.org/project/pip/). 


## Ethical Concerns
We use the Enron email corpus in our experiments. The dataset contains real emails from employees of Enron. Our experiments only uses the Enron emails to benchmark the performance of DDR-SSE in realistic workloads and demonstrate its effectiveness in leakage suppression. We do not intend to intrude the privacy of the users in the emails. Please be sensitive to the privacy of the people in the emails when you use the dataset.


# 1. Enron Email Parser
First, download the Enron email corpus from https://www.cs.cmu.edu/~enron/. Then run the Python script `./email_parser/email_parser.py` to parse 400K emails into an inverted index, a single file containing all the emails (after splitting emails ), files containing auxiliary information on the 100/95/90/85/80/75-th percentile keywords, and files containing the corresponding queries.


# 2. DDR-SSE Benchmark
To run benchmark on DDR-SSE. Simply use the command
```
java -Xms60G -classpath "./bin/" Scheme.DDR_benchmark <# documents> <bucket size>
```

The values used in the paper are: `<# documents> = 400000`, and `<bucket size> = 400`.


The remaining instructions are for leakage cryptanalysis of DDR-SSE. We start with our own attack proposed in the paper.


# 3. Our Leakage-Abuse Attack Against DDR-SSE
## 3.1. DDR-SSE Leakage Extraction
Navigate to `./DDR-SSE/`. Compile the code by running
```
javac -d ./bin/ ./src/**/*.java ./src/**/**/*.java
```

Use the following command to run leakage extraction for `bucket size = 400`
```
java -Xms60G -classpath "./bin/" Scheme.DDR_leakage_extraction <# documents> <bucket size>
```

The values used in the paper are: `<# documents> = 400000`, and `<bucket size> = 100, 200, or 400`.

## 3.2 Running the Attack
Run `./attack/attack_exact_batch.py` with the appropriate inputs (use `--help` to see the options) to run the attack. The attack results can be plotted with `./attack_results/attack_results_plot_exact.py`.

To run the approximate attacks, run `email_parser/email_parser_split.py` in the first step. Run leakage extraction in the same way as before. Run the attack with `./attack/attack_approx_batch.py`. And plot the results with `./attack_results/attack_results_plot_approx.py`.


# 4. The SAP Attack Against DDR-SSE
The paper describing the SAP attack can be found here: https://www.usenix.org/conference/usenixsecurity21/presentation/oya.

The original code for the SAP attack can be found here: https://github.com/simon-oya/USENIX21-sap-code.

We modified the SAP attack to make it compatible with DDR-SSE.


We work with `./USENIX21-sap-code-master/` as the root directory in this section.
We have included our run of the attack in the repository. If you just want to see the plot, jump to Step 5. Otherwise, rename or delete `./manager_df_data.pkl` and follow the following procedure.

1. To run the SAP attack against DDR-SSE, begin by running `./add_experiments_to_manager.py`. This produces `./manager_df_data.pkl`. 
2. Run `./manager_df.py`. Type `w` and press return to create jobs that will be run.
3. Run the jobs by running `./run_pending_experiments.py`. Adjust the number of cores used for the jobs on line 30 of the Python script if needed. This process can take a while (~1 hour).
4. Run `./manager_df.py`. Type `eat` and press return to consume the attack results. These will be saved in `./manager_df_data.pkl`.
5. Plot the experimental results by running `./plot5_vs_CLRZ_DDR-SSE.py`.


# 5. The IHOP Attack Against DDR-SSE
The paper describing the IHOP attack can be found here: https://www.usenix.org/conference/usenixsecurity22/presentation/oya.

The original code for the IHOP attack can be found here: https://github.com/simon-oya/USENIX22-ihop-code.

We modified the IHOP attack to make it compatible with DDR-SSE.

We work with `./USENIX22-ihop-code-master/` as the root directory in this section.
We have included our run of the attack in the repository. If you just want to see the plot, jump to Step 5. Otherwise, rename or delete `./manager_data5.pkl` and follow the following procedure.

1. To run the IHOP attack against DDR-SSE, begin by running `./add_to_manager.py`. This produces `./manager_data5.pkl`. 
2. Run `./manager.py`. Type `w` and press return to create jobs that will be run.
3. Run the jobs by running `./run_from_manager.py`. Adjust the number of cores used for the jobs on line 131 of the Python script if needed. This process can take a while (~3 hours).
4. Run `./manager.py`. Type `eat` and press return to consume the attack results. These will be saved in `./manager_data5.pkl`.
5. Plot the experimental results by running `./plot5_vs_CLRZ_DDR-SSE.py`.


# 6. The Jigsaw Attack Aagainst DDR-SSE
The paper describing the Jigsaw attack can be found here: https://www.usenix.org/conference/usenixsecurity24/presentation/nie.

The original code for the Jigsaw attack can be found here: https://github.com/JigsawAttack/JigsawAttack.

We modified the IHOP attack to make it compatible with DDR-SSE.

We work with `./USENIX24-jigsaw-code-master/` as the root directory in this section.
We have included our run of the attack in the repository. If you just want to see the plot, jump to Step 2. Otherwise, follow the procedure from the beginning.

1. Run `./test_against_CLRZ_DDR-SSE.py` to run the attacks. This takes ~2 hours.
2. Run `./generate_test_against_CLRZ_DDR-SSE.py` to plot the results.