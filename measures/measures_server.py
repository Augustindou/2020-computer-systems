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


# compile java files
os.system(f"javac -d {exec_path} {src_path}OptimizedServer.java")
os.system(f"javac -d {exec_path} {src_path}SimpleServer.java")
if VERBOSE: print("--- Compiled java server files ---")

if VERBOSE: print("--- Running tests ---")
# iterate over all combinations
for t, n, f, d in itertools.product(n_threads, n_clients, input, delays):
    if VERBOSE: print(f"--- Server Test : {t} threads (client should be on {n} clients, {d}ms delay, {f}) ---")
    # strings
    server_output_file = f"{output_path}SERVER {f} - {n} clients - {d}ms delay - {t} threads.txt"
    # java OptimizedServer <port number> <database text file> <number of threads> [result text file]
    os.system(f"java {exec_path}OptimizedServer {port} {db_file} {t} {server_output_file}")
    if VERBOSE: print("done")
