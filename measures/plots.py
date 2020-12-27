import os
import numpy as np
from measures_constants import *
from matplotlib import legend, pyplot as plt

output_file = [lambda f, n, d, t : f"{server_output_file(f, n, d, t)}_queue.txt".replace('\\', ''),
               lambda f, n, d, t : f"{server_output_file(f, n, d, t)}_service.txt".replace('\\', ''),
               lambda f, n, d, t : f"{client_output_file(f, n, d, t)}.txt".replace('\\', '')]

plot_types = ["Queue", "Processing", "Response"]
plots = ["Number of clients [-]", "Mean delay [s]", "Number of threads [-]"]
input_values = [variable_inputs[np.logical_and(variable_inputs[:,1] == delays[0]   , variable_inputs[:,2] == n_threads[0])],
                variable_inputs[np.logical_and(variable_inputs[:,0] == n_clients[0], variable_inputs[:,2] == n_threads[0])],
                variable_inputs[np.logical_and(variable_inputs[:,0] == n_clients[0], variable_inputs[:,1] == delays[0]   )]]
plot_colors = ['b', 'g', 'r']

# plot the different plots (n_clients, delay and threads)
for j, plot, input_variables in zip(range(len(plots)), plots, input_values):
    plt.figure(figsize=(20,4))
    # plot the 3 subplots (queue, service and total)
    for i, file, plot_type in zip(range(len(output_file)), output_file, plot_types):
        # plot variables
        plt.subplot(1, len(output_file), i+1)
        plt.xlabel(plot)
        plt.ylabel(f"{plot_type} time [s]")
        # iterate over requests types (easy, netw intensive, hard)
        for requests_type, c in zip(input_files, plot_colors):
            # iterate over different datapoints
            avg = np.array([])
            std = np.array([])
            for n, d, t in input_variables:
                # read the corresponding file
                with open(file(requests_type, n, d, t), 'r') as f:
                    # add averages to listaverage
                    lines = np.array(list(map(int, f.readlines())))/1000
                    avg = np.append(avg, np.average(lines))
                    std = np.append(std, np.std(lines))
            
            # get argsort
            idx = np.argsort(input_variables[:, j])
            plt.plot(input_variables[idx, j] / (1000 if j==1 else 1), avg[idx], label=requests_type.replace('-', ' '), color=c, marker='.')
            plt.fill_between(input_variables[idx, j] / (1000 if j==1 else 1), avg[idx]-std[idx], avg[idx]+std[idx], color=c, alpha=0.1)
            plt.legend()
    plt.savefig(f"{plt_path}{plot}", bbox_inches='tight')
    

# Plotting optimized versus simple
optimized_output_file = [lambda f, n, d, t : f"{optimized_server_output_file(f, n, d, t)}_queue.txt".replace('\\', ''),
                         lambda f, n, d, t : f"{optimized_server_output_file(f, n, d, t)}_service.txt".replace('\\', ''),
                         lambda f, n, d, t : f"{optimized_client_output_file(f, n, d, t)}.txt".replace('\\', '')]
optimized_requests_type = ["hard-requests"]
output_files_types = list(zip(output_file, optimized_output_file))
plots_optimized = ["Number of threads [-]"]
input_values_optimized = [variable_inputs[np.logical_and(variable_inputs[:,0] == n_clients[0], variable_inputs[:,1] == delays[0]   )]]
type_names = ["Simple", "Optimized"]
fig_names = ["Simple-Optimized Comparison"]
plot_colors_optimized = ['b', 'g']


for plot, input_variables, fig_name in zip(plots_optimized, input_values_optimized, fig_names):
    # plot the different plots (n_clients, delay and threads)
    plt.figure(figsize=(20,4))
    # plot the 3 subplots (queue, service and total)
    for i, file_types, plot_type in zip(range(len(output_files_types)), output_files_types, plot_types):
        # plot variables
        plt.subplot(1, len(plot_types), i+1)
        plt.xlabel(plot)
        plt.ylabel(f"{plot_type} time [ms]")
        # iterate over requests types (easy, netw intensive, hard)
        for requests_type in optimized_requests_type:
            # iterate over different datapoints
            for file, name, c in zip(file_types, type_names, plot_colors):
                avg = np.array([])
                std = np.array([])
                for n, d, t in input_variables:
                    # read the corresponding file
                    with open(file(requests_type, n, d, t), 'r') as f:
                        # add averages to listaverage
                        lines = np.array(list(map(int, f.readlines())))/1000
                        avg = np.append(avg, np.average(lines))
                        std = np.append(std, np.std(lines))
                # get argsort
                idx = np.argsort(input_variables[:, 2])
                plt.plot(input_variables[idx, 2], avg[idx], label=requests_type.replace('-', ' '), color=c, marker='.')
                plt.fill_between(input_variables[idx, 2], avg[idx]-std[idx], avg[idx]+std[idx], color=c, alpha=0.1)
                plt.legend()
        plt.savefig(f"{plt_path}{fig_name}", bbox_inches='tight')