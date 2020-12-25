# juste pour dire
import numpy as np
# pour lancer le java
import os
# pour sleep
import time
# pour itérer sur toutes les possibilités
import itertools
# threads
from threading import Thread
# import constants
from measures_constants import *

# compile java files
os.system(f"javac -cp {src_path} -d {exec_path} {src_path}Clients.java")
if VERBOSE: print("--- Compiled java client file ---")

if VERBOSE: print("--- Running tests ---")
# iterate over all combinations
for i, (f, (n, d, t)) in enumerate(itertools.product(input_files, variable_inputs)):
    if VERBOSE: print(f"--- Client Test #{i} : {n} clients, {d}ms delay, {f} (server should be on {t} threads) ---")
    # waiting for server
    time.sleep(20)
    # strings
    input_file = f"{input_path}{f}.txt"
    tic = time.time()
    # java Clients <host name> <port number> <number of clients> <input file> <mean delay> <verbose> [results file]
    os.system(f"java -cp {exec_path} Clients {hostname} {port} {n} {input_file} {d} {verbose_clients} {client_output_file(f, n, d, t)}")
    if VERBOSE: 
        print(f"Test took {time.time() - tic} s")
        print("done")
