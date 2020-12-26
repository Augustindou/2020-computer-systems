import random
import string

starting_chars = ['B', 'C', 'D', 'F', 'G', 'H', 'L', 'M', 'N', 'P', 'R', 'S']
lowercase_chars = ['a', 'e', 'i', 'o', 'u']

random.shuffle(starting_chars)
random.shuffle(lowercase_chars)

regexes=[]
for i in range(25):
    if (random.choices([True, False], weights=[25, 75], k=1)[0]) and len(regexes) > 1:
        regexes.append(random.choice(regexes))
    else:
        number_of_cat = 2
        cats = random.sample(['0', '1', '2', '3', '4', '5'], k=number_of_cat)
        regex = ','.join(cats) + ';'+'^'+starting_chars[i % len(starting_chars)] + lowercase_chars[i % len(lowercase_chars)] +'\n'
        regexes.append(regex)


with open('network-intensive-requests.txt', 'w+') as f:
    f.writelines(regexes)