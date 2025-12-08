# Overview
You can follow the steps below to reproduce our experimental results.

The repository has a few components:
1. Enron email parser.
2. DDR-SSE leakage extraction.
3. Leakage-abuse attack against DDR-SSE.
4. DDR-SSE benchmark.


# 1. Enron Email parser
First, download the Enron email corpus from https://www.cs.cmu.edu/~enron/. Then run the Python script `./email_parser/email_parser.py` to parse 400K emails into an inverted index, a single file containing all the emails (after splitting emails ), files containing auxiliary information on the 100/95/90/85/80/75-th percentile keywords, and files containing the corresponding queries.


# 2. DDR-SSE Leakage Extraction
Navigate to `./DDR-SSE/`. Compile the code by running
```
javac -d ./bin/ ./src/**/*.java ./src/**/**/*.java
```

Use the following command to run leakage extraction for `bucket size = 400`
```
java -Xms60G -classpath "./bin/" Scheme.DDR_leakage_extraction 400000 400
```

To extract leakage for different bucket sizes, simply change the last input of the command above to the desired bucket size.


# 3. Our Leakage-Abuse Attack against DDR-SSE
Run `./attack/attack_exact_batch.py` with the appropriate inputs (use `--help` to see the options) to run the attack. The attack results can be plotted with `./attack_results/attack_results_plot_exact.py`.

To run the approximate attacks, run `email_parser/email_parser_split.py` in the first step. Run leakage extraction in the same way as before. Run the attack with `./attack/attack_approx_batch.py`. And plot the results with `./attack_results/attack_results_plot_approx.py`.


# 4. SAP and IHOP attacks against DDR-SSE
The original code for the SAP and IHOP attacks are from:
- https://github.com/simon-oya/USENIX21-sap-code
- https://github.com/simon-oya/USENIX22-ihop-code

We modified the scripts to make them compatible with DDR-SSE.


## 4.1 The SAP Attack
We work with `./USENIX21-sap-code-master/` as the root directory in this section.

We have included our run of the attack in the repository. If you just want to see the plot, jump to Step 5. Otherwise, rename or delete `./manager_df_data.pkl` and follow the following procedure.

1. To run the SAP attack against DDR-SSE, begin by running `./add_experiments_to_manager.py`. This produces `./manager_df_data.pkl`. 
2. Run `./manager_df.py`. Type `w` and press return to create jobs that will be run.
3. Run the jobs by running `./run_pending_experiments.py`. Adjust the number of cores used for the jobs on line 30 of the Python script if needed. This process can take a while (~1 hour).
4. Run `./manager_df.py`. Type `eat` and press return to consume the attack results. These will be saved in `./manager_df_data.pkl`.
5. Plot the experimental results by running `./plot5_vs_CLRZ_DDR-SSE.py`.


## 4.2 The IHOP Attack
We work with `./USENIX22-ihop-code-master/` as the root directory in this section.

We have included our run of the attack in the repository. If you just want to see the plot, jump to Step 5. Otherwise, rename or delete `./manager_data5.pkl` and follow the following procedure.

1. To run the IHOP attack against DDR-SSE, begin by running `./add_to_manager.py`. This produces `./manager_data5.pkl`. 
2. Run `./manager.py`. Type `w` and press return to create jobs that will be run.
3. Run the jobs by running `./run_from_manager.py`. Adjust the number of cores used for the jobs on line 131 of the Python script if needed. This process can take a while (~3 hour).
4. Run `./manager.py`. Type `eat` and press return to consume the attack results. These will be saved in `./manager_data5.pkl`.
5. Plot the experimental results by running `./plot5_vs_CLRZ_DDR-SSE.py`.


# 5. DDR-SSE Benchmark
To run benchmark on DDR-SSE. Simply use the command
```
java -Xms60G -classpath "./bin/" Scheme.DDR_benchmark 400000 400
```

To run DDR-SSE with a different bucket size, change the last input of the command above.