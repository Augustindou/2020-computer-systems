import numpy as np
import random


min_len = 10

lines = []

print('starting')
count = 0
with open("dbdata.txt") as f:
    while count < 50000:
        line = f.readline()
        lines.append(line)    
        count += 1

lines = np.array(lines)
np.random.shuffle(lines)

regexes = []

count = 0
i = 0
while count < 100:
    line = lines[i]

    index = range(len(line[4:]))
    idx = [min_len*i + x for i, x in enumerate(sorted(random.sample(index, 2)))]
    number_of_cat = random.choices([0, 1, 2, 3, 4, 5, 6], weights=[1, 100, 90, 20, 5, 5, 1], k=1)

    data = line[4+idx[0]:4+idx[1]]
    data = data.strip()
    data = data.strip('\n')
    data = data.replace('(', '')
    data = data.replace(')', '')
    data = data.replace('.', '')
    data = data.replace('*', '')
    data = data.replace('[', '')
    data = data.replace(']', '')
    data = data.replace('{', '')
    data = data.replace('}', '') 


    i+=1
    if len(data) >= min_len:
        cats = [int(line[0])]
        while len(cats) < number_of_cat[0]:
            new_cat = random.choice([0, 1, 2, 3, 4, 5])
            if new_cat not in cats:
                cats.append(new_cat)
        
        regex = ''
        if number_of_cat[0] > 0:
            for cat in cats:
                regex+=','+str(cat)
        regex = regex.strip(',')
        regex += ';'
        regex += data
        regex += '\n'
        regexes.append(regex)
        count += 1

with open('easy-requests-100.txt', 'w+') as f:
    f.writelines(regexes)

print('done')



