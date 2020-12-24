# juste pour dire
import numpy as np
# pour lancer le java
import os

VERBOSE = True


# (n_clients, input_file, mean_delay)
to_run = [(5, ),
          (),
          (),
          (),
          ()]
hostname = ""
port     = 80
db_file  = "assets/dbdata.txt"


# TODO compile java files


# run

java Clients <host name> <port number> <number of clients> <input file> <mean delay> <verbose> [results file]

java OptimizedServer <port number> <database text file> <number of threads> [result text file]