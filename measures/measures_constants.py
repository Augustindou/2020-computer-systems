# juste pour dire
import numpy as np

VERBOSE = True

# paths to directories
root_path = "../"
exec_path = f"{root_path}java_executables/"
src_path = f"{root_path}computer-systems/src/"
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
hostname = "localhost"
port     = 69
db_file  = "assets/dbdata.txt"
verbose_clients = "false"