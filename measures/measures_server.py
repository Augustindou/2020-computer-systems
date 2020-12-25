# juste pour dire
import numpy as np
# pour lancer le java
import os
# pour itérer sur toutes les possibilités
import itertools
# threads
from threading import Thread
# import constants
from measures_constants import *
# to measure time
import time


# compile java files
os.system(f"javac -cp {src_path} -d {exec_path} {src_path}OptimizedServer.java")
os.system(f"javac -cp {src_path} -d {exec_path} {src_path}SimpleServer.java")
if VERBOSE: print("--- Compiled java server files ---")

if VERBOSE: print("--- Running tests ---")
# iterate over all combinations
for i, (f, (n, d, t)) in enumerate(itertools.product(input_files, variable_inputs)):
    if VERBOSE: print(f"--- Server Test #{i} : {t} threads (client should be on {n} clients, {d}ms delay, {f}) ---")
    # java OptimizedServer <port number> <database text file> <number of threads> [result text file]
    tic = time.time()
    # os.system(f"java -cp {exec_path} SimpleServer {port} {db_file} {t} {verbose_server} {server_output_file(f, n, d, t)}")
    if VERBOSE: 
        print(f"Test took {time.time() - tic} s")
        print("done")
    
