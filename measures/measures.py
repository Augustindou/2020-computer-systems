# juste pour dire
import numpy as np
# pour lancer le java
import os
# pour itérer sur toutes les possibilités
import itertools
# threads
from threading import Thread

VERBOSE = True

# paths to directories
root_path = "../"
exec_path = f"{root_path}java_executables/"
src_path = f"{root_path}src/"
input_path = f"{root_path}regex-generation/"
output_path = f"{root_path}output_files/"

# number of clients
# n_clients = np.concatenate(np.arange(2, 10, 2, dtype=int), np.arange(10, 105, 5, dtype=int))
n_clients = [1, 2] # make sure script is working
# input files (txt and path will be added)
# input = ["easy-requests-100", "hard-requests-100", "network-intensive-requests-100"]
input = ["easy-requests-100"] # make sure script is working
# delay [ms]
# delays = np.arange(5, 25, 5)*1000
delays = [5000] # make sure script is working
# server threads
# n_threads = [1, 2, 4, 6, 8, 10, 12, 16]
n_threads = [1] # make sure script is working

# code variables
hostname = "" # ! TODO
port     = 80
db_file  = "assets/dbdata.txt"
verbose_clients = "false"

# compile java files
os.system(f"javac -d {exec_path} {src_path}Clients.java")
os.system(f"javac -d {exec_path} {src_path}OptimizedServer.java")
os.system(f"javac -d {exec_path} {src_path}SimpleServer.java")
if VERBOSE: print("--- Compiled java files ---")

if VERBOSE: print("--- Running tests ---")
# iterate over all combinations
for t, n, f, d in itertools.product(n_threads, n_clients, input, delays):
    if VERBOSE: print(f"--- Test : {t} threads, {n} clients, {d}ms delay, {f} ---")
    # strings
    input_file = f"{input_path}{f}.txt"
    server_output_file = f"{output_path}SERVER {f} - {n} clients - {d}ms delay - {t} threads.txt"
    client_output_file = f"{output_path}CLIENT {f} - {n} clients - {d}ms delay - {t} threads.txt"
    # server thread
    # java OptimizedServer <port number> <database text file> <number of threads> [result text file]
    server = Thread(target = os.system, args = (f"java {exec_path}OptimizedServer {port} {db_file} {t} {server_output_file}",))
    server.start()
    # client thread
    # java Clients <host name> <port number> <number of clients> <input file> <mean delay> <verbose> [results file]
    client = Thread(target = os.system, args = (f"java {exec_path}Clients {hostname} {port} {n} {input_file} {d} {verbose_clients} {client_output_file}",))
    client.start()
    # join threads
    client.join()
    server.join()
    if VERBOSE: print("done")
