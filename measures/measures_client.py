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
os.system(f"javac -d {exec_path} {src_path}Clients.java")
if VERBOSE: print("--- Compiled java client file ---")

if VERBOSE: print("--- Running tests ---")
# iterate over all combinations
for t, n, f, d in itertools.product(n_threads, n_clients, input, delays):
    if VERBOSE: print(f"--- Client Test : {n} clients, {d}ms delay, {f} (server should be on {t} threads) ---")
    # strings
    input_file = f"{input_path}{f}.txt"
    server_output_file = f"{output_path}SERVER {f} - {n} clients - {d}ms delay - {t} threads.txt"
    client_output_file = f"{output_path}CLIENT {f} - {n} clients - {d}ms delay - {t} threads.txt"
    # java Clients <host name> <port number> <number of clients> <input file> <mean delay> <verbose> [results file]
    os.system(f"java {exec_path}Clients {hostname} {port} {n} {input_file} {d} {verbose_clients} {client_output_file}")
    if VERBOSE: print("done")
