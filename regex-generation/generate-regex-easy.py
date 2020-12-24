import numpy as np
import random


min_len = 10

lines = []

count = 0
with open("dbdata.txt") as f:
    while count < 50000:
        line = f.readline()
        lines.append(line)    
        count += 1

lines = np.array(lines)
np.random.shuffle(lines)

regexes = []

i = 0
while len(regexes) < 100:
    if (random.choices([True, False], weights=[25, 75], k=1)[0]) and len(regexes) > 1:
        regexes.append(random.choice(regexes))
    else:
        line = lines[i]

        number_of_cat = 2

        data = line[4:]
        data = data.strip('\n')

        i+=1
        if all([x not in data for x in ['(', ')', '{', '}', '*', '.', '\n']]):
            cats = [int(line[0])]
            while len(cats) < number_of_cat:
                new_cat = random.choice([0, 1, 2, 3, 4, 5])
                if new_cat not in cats:
                    cats.append(new_cat)
            
            regex = ''
            if number_of_cat > 0:
                for cat in cats:
                    regex+=','+str(cat)
            regex = regex.strip(',')
            regex += ';'
            regex += '^'+data+'$'
            regex += '\n'
            regexes.append(regex)

with open('easy-requests-100.txt', 'w+') as f:
    f.writelines(regexes)
