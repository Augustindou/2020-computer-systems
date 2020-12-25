import random
import string


regexes = []
ends = []
while len(regexes) < 25:
    if (random.choices([True, False], weights=[25, 75], k=1)[0]) and len(regexes) > 1:
        regexes.append(random.choice(regexes))
    else:
        end_of_regex = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(100))
        if end_of_regex not in ends:
            ends.append(end_of_regex)
            number_of_cat = 2
            cats = random.sample(['0', '1', '2', '3', '4', '5'], k=number_of_cat)
            regex = ','.join(cats) + ';'+'.'+end_of_regex+'\n'
            regexes.append(regex)

with open('hard-requests.txt', 'w+') as f:
    f.writelines(regexes)


