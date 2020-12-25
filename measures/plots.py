import os
import numpy as np
from measures_constants import *
from matplotlib import legend, pyplot as plt

output_file = [lambda f, n, d, t : f"{server_output_file(f, n, d, t)}_queue.txt".replace('\\', ''),
               lambda f, n, d, t : f"{server_output_file(f, n, d, t)}_service.txt".replace('\\', ''),
               lambda f, n, d, t : f"{client_output_file(f, n, d, t)}.txt".replace('\\', '')]

plot_types = ["Queue", "Service", "Response"]
plots = ["Number of clients [-]", "Mean delay [ms]", "Number of threads [-]"]
input_values = [variable_inputs[np.logical_and(variable_inputs[:,1] == delays[0]   , variable_inputs[:,2] == n_threads[0])],
                variable_inputs[np.logical_and(variable_inputs[:,0] == n_clients[0], variable_inputs[:,2] == n_threads[0])],
                variable_inputs[np.logical_and(variable_inputs[:,0] == n_clients[0], variable_inputs[:,1] == delays[0]   )]]

# plot the different plots (n_clients, delay and threads)
for j, plot, input_variables in zip(range(len(plots)), plots, input_values):
    plt.figure(figsize=(20,4))
    # plot the 3 subplots (queue, service and total)
    for i, file, plot_type in zip(range(len(output_file)), output_file, plot_types):
        # plot variables
        plt.subplot(1, len(output_file), i+1)
        plt.xlabel(plot)
        plt.ylabel(f"{plot_type} time [ms]")
        # iterate over requests types (easy, netw intensive, hard)
        for requests_type in input_files:
            # iterate over different datapoints
            avg = np.array([])
            for n, d, t in input_variables:
                # read the corresponding file
                with open(file(requests_type, n, d, t), 'r') as f:
                    # add averages to listaverage
                    lines = f.readlines()
                    avg = np.append(avg, sum( map(int, lines) ) / len(lines) )
                    # TODO : add standard deviation and stuff
            # get argsort
            idx = np.argsort(input_variables[:, j])
            plt.plot(input_variables[idx, j], avg[idx], label=requests_type.replace('-', ' '))
            plt.legend()
    plt.savefig(f"{plt_path}{plot}", bbox_inches='tight')