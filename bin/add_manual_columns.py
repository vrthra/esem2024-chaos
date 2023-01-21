#!/usr/bin/env python3

import csv
import sys
ROWS = []
with open(sys.argv[1], newline='') as csvfile:
    myreader = csv.DictReader(csvfile, delimiter=',', quotechar='"')
    for row in myreader:
        ROWS.append(row)

subjects = {row['program']: [row[c] for c in ["estimate","CILower","CIUpper"]] for row in ROWS if row['estimator'] == 'Manual'}

for row in ROWS:
    row['MEstimate'] = subjects[row['program']][0]
    row['MLower'] =  subjects[row['program']][1]
    row['MUpper'] =  subjects[row['program']][2]

header = ["model","kind","testsuite","program","total_mutants","Sn","C","estimator","estimate","CILower","CIUpper", "MEstimate", "MLower", "MUpper"]

print(",".join(header))
for row in ROWS:
    print(','.join([row[k] for k in header]))


#for s in subjects:
#    print(s, subjects[s])

