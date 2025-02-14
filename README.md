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


# 3. Leakage-Abuse Attack against DDR-SSE
Run `./attack/attack_exact_batch.py` with the appropriate inputs (use `--help` to see the options) to run the attack. The attack results can be plotted with `./attack_results/attack_results_plot_exact.py`.

To run the approximate attacks, run `email_parser/email_parser_split.py` in the first step. Run leakage extraction in the same way as before. Run the attack with `./attack/attack_approx_batch.py`. And plot the results with `./attack_results/attack_results_plot_approx.py`.


# 4. DDR-SSE Benchmark
To run benchmark on DDR-SSE. Simply use the command
```
java -Xms60G -classpath "./bin/" Scheme.DDR_benchmark 400000 400
```

To run DDR-SSE with a different bucket size, change the last input of the command above.