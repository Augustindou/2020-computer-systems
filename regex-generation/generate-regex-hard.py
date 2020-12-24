import random
import string

# resulting regexes will be in O(regex_complexity^2 * len(string to match))
regex_complexity = 15

regexes = []
ends = []
while len(regexes) < 100:
    if (random.choices([True, False], weights=[25, 75], k=1)[0]) and len(regexes) > 1:
        regexes.append(random.choice(regexes))
    else:
        end_of_regex = ''.join(random.choice(string.ascii_lowercase) for _ in range(5))
        if end_of_regex not in ends:
            ends.append(end_of_regex)
            number_of_cat = 2
            cats = random.sample(['0', '1', '2', '3', '4', '5'], k=number_of_cat)
            regex = ','.join(cats) + ';'+'.{0,'+str(regex_complexity)+'}'+end_of_regex+'\n'
            regexes.append(regex)

with open('hard-requests-100.txt', 'w+') as f:
    f.writelines(regexes)


