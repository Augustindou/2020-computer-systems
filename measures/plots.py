import os
import numpy as np
from measures_constants import *
from matplotlib import legend, pyplot as plt
import math

output_file = [lambda f, n, d, t : f"{server_output_file(f, n, d, t)}_queue.txt".replace('\\', ''),
               lambda f, n, d, t : f"{server_output_file(f, n, d, t)}_service.txt".replace('\\', ''),
               lambda f, n, d, t : f"{client_output_file(f, n, d, t)}.txt".replace('\\', '')]

plot_types = ["Queue", "Service", "Response"]
plots = ["Number of clients [-]", "Mean Request Rate [Hz]", "Number of threads [-]"]
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
            plt.plot(1000/input_variables[idx, j] if j==1 else input_variables[idx, j], avg[idx], label=requests_type.replace('-', ' '), color=c, marker='.')
            plt.fill_between(1000/input_variables[idx, j] if j==1 else input_variables[idx, j], avg[idx]-std[idx], avg[idx]+std[idx], color=c, alpha=0.1)
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
        plt.ylabel(f"{plot_type} time [s]")
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
                plt.plot(input_variables[idx, 2], avg[idx], label=name, color=c, marker='.')
                plt.fill_between(input_variables[idx, 2], avg[idx]-std[idx], avg[idx]+std[idx], color=c, alpha=0.1)
                plt.legend()
        plt.savefig(f"{plt_path}{fig_name}", bbox_inches='tight')

    
# Plotting expected response time and real
def theoretical_time(mu, m, l):
    a = l/mu
    chi = l/(m*mu)
    pi0 = 1/(sum([(a**i)/math.factorial(i) for i in range(m)] + a**m/(math.factorial(m)*(1-chi))))
    return 1/l * (a + chi*a**m / ((1 - chi)**2 * math.factorial(m)) * pi0)

input_values_expected = variable_inputs[np.logical_and(variable_inputs[:,0] == n_clients[0], variable_inputs[:,1] == delays[0]   )]
output_file_expected = [lambda f, n, d, t : f"{client_output_file(f, n, d, t)}.txt".replace('\\', '')]
output_file_total = lambda f, n, d, t: f"{client_output_file(f, n, d, t)}.txt".replace('\\', '')
output_file_queue = lambda f, n, d, t: f"{server_output_file(f, n, d, t)}_queue.txt".replace('\\', '')
requests_type_expected = ["easy-requests"]
type_names_expected = ["Measured"]
plot_expected = ["Number of threads/service stations [-]"]
fig_names_expected = "Measured-Expected-Comparison"
plot_colors_expected = ['b', 'r']
plot_colors_theoretical = 'g'
plot_label_theoretical = 'Theoretical'

plot_types_expected = ['Response']


service_avg_list = []
for requests_type in requests_type_expected:
    avg_total = np.array([])
    avg_queue = np.array([])
    for n, d, t in input_values_expected:
        # read the corresponding file
        with open(output_file_total(requests_type, n, d, t), 'r') as f:
            # add averages to listaverage
            lines = np.array(list(map(int, f.readlines())))/1000
            avg_total = np.append(avg_total, np.average(lines))

        with open(output_file_queue(requests_type, n, d, t), 'r') as f:
            # add averages to listaverage
            lines = np.array(list(map(int, f.readlines())))/1000
            avg_queue = np.append(avg_queue, np.average(lines))
    service_avg_list.append(avg_total - avg_queue)    

# plot the different plots (n_clients, delay and threads)
plt.figure(figsize=(6,4))
# plot the 3 subplots (queue, service and total)
for i, file, plot_type in zip(range(len(output_file_expected)), output_file_expected, plot_types_expected):
    # plot variables
    plt.subplot(1, len(output_file_expected), i+1)
    plt.xlabel(plot)
    plt.ylabel(f"{plot_type} time [s]")
    # iterate over requests types (easy, netw intensive, hard)
    for requests_type, c, service_avg, name in zip(requests_type_expected, plot_colors_expected, service_avg_list, type_names_expected):
        # iterate over different datapoints
        avg = np.array([])
        for n, d, t in input_values_expected:
            # read the corresponding file
            with open(file(requests_type, n, d, t), 'r') as f:
                # add averages to listaverage
                lines = np.array(list(map(int, f.readlines())))/1000
                avg = np.append(avg, np.average(lines))
        
        # get argsort
        idx = np.argsort(input_values_expected[:, 2])
        plt.plot(input_values_expected[idx, 2], avg[idx], label=name, color=c, marker='.')

        # plot expected output
        expected_response = np.array([])
        for (n, d, t), s_avg in zip(input_values_expected, service_avg):
            expected_response = np.append(expected_response, theoretical_time(1/s_avg, t, n/d))

        plt.plot(input_values_expected[idx, 2], expected_response[idx], label=plot_label_theoretical, color=plot_colors_theoretical, marker='.')
        plt.legend()


plt.savefig(f"{plt_path}{fig_names_expected}", bbox_inches='tight')
    