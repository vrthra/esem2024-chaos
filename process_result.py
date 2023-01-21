#!/usr/bin/env python3
import json
"""
{
"commons-csv.mutations.xml.matrix":
{
  "dataset": "/scratch/rahul/tars/Immortals/results/commons-csv.mutations.xml.matrix",
  "n.mutants": 635,
  "n.samples": 306,
  "n.killed": 496,
  "estimators": [
    { "estimator": "chao_lower", "value": 527.0083, "lowci": 499.9161, "upci": 741.5293 },
    { "estimator": "bootstrap", "value": 555.8993, "lowci": 536.9024, "upci": 574.8961 },
    { "estimator": "sample_coverage", "value": 645.2466, "lowci": 598.8802, "upci": 691.613 },
    { "estimator": "chao_upperbound", "value": 527.0083, "lowci": 505.2556, "upci": 592.823 },
    { "estimator": "chao2_our", "value": 527.0083, "lowci": 506.4438, "upci": 547.5728 },
    { "estimator": "jack_wiqid", "value": 553.5875, "lowci": 511.9512, "upci": 703.9042 },
    { "estimator": "jack_our", "value": 556.8007, "lowci": 535.2048, "upci": 578.3965 }
  ]
},
"""
# Manual classification
MANUAL = {
        "commons-csv": 92.24,
        "commons-collections": 99.00,
        "commons-compress": 99.00,
        "commons-configuration": 96.00,
        "commons-dbcp": 99.00,
        "commons-imaging": 100.0,
        "commons-io": 95.00,
        "commons-lang": 88.00,
        "commons-math": 92.00,
        "commons-net": 99.00
        }

def main(fn):
    obj = None
    with open(fn) as f:
        obj = json.load(fp=f)
    
    header = "Project\t& Mutants\t& Killed\t& Chao2Lower\t& Chao2Upper\t& Chao2\t& Jackknife\t& Bootstrap\t& SampleCoverage\\\\"

    h = header.split('&')
    print('|r|' + '|'.join(['l' for l in h][1:]) + '|')

    print(header)
    print('\\hline')
    for k in obj:
        project = k.replace('.mutations.xml.matrix', '')
        if project not in MANUAL: continue
#    Project               & Mutants & Killed & Bootstrap              & Jackknife              & Chao2 \\
#    \hline
#    commons-csv           & 635     & 496    & 555.9 (536.9-574.8)    & 556.8 (535.2-578.4)    & 527.0 (506.4-547.6) \\
#    commons-collections   & 8306    & 3462   & 4276.6 (4193.0-4360.1) & 6017.8 (5613.1-6422.4) & 4866.9948 (4649.9-5084.1) \\

        mutants = obj[k]['n.mutants'] 
        killed = obj[k]['n.killed'] 

        pmutants = mutants * MANUAL[project]/100.0

        chao_lower = obj[k]['estimators'][0]
        p = '+' if chao_lower['value'] > mutants else ''
        q = '+' if chao_lower['value'] > pmutants else ''
        chao_lowerv = (p + q + '{:0.1f}').format(chao_lower['value'])

        bootstrap = obj[k]['estimators'][1]
        p = '+' if bootstrap['value'] > mutants else ''
        q = '+' if bootstrap['value'] > pmutants else ''
        bootstrapv = (p+ q  +'{:0.1f}').format(bootstrap['value'])

        sample_coverage = obj[k]['estimators'][2]
        p = '+' if sample_coverage['value'] > mutants else ''
        q = '+' if sample_coverage['value'] > pmutants else ''
        sample_coveragev= (p+ q  +'{:0.1f}').format(sample_coverage['value'])

        chao_upper = obj[k]['estimators'][3]
        p = '+' if chao_upper['value'] > mutants else ''
        q = '+' if chao_upper['value'] > pmutants else ''
        chao_upperv= (p+ q +'{:0.1f}').format(chao_upper['value'])

        chao2 = obj[k]['estimators'][4]
        p = '+' if chao2['value'] > mutants else ''
        q = '+' if chao2['value'] > pmutants else ''
        chao2v = (p+ q +'{:0.1f}').format(chao2['value'])

        jackknife = obj[k]['estimators'][6]
        p = '+' if jackknife['value'] > mutants else ''
        q = '+' if jackknife['value'] > pmutants else ''
        jackknifev= (p+ q +'{:0.1f}').format(jackknife['value'])
        print("{project}\t& {mutants}\t& {killed}\t& {clv}\t& {cuv}\t& {chao2v}\t& {jackknifev}\t& {bootstrapv}\t& {scv}\\\\".format(
            project= project,
            mutants=mutants,
            killed=killed,
            clv=chao_lowerv,
            cuv=chao_upperv,
            chao2v=chao2v,
            jackknifev=jackknifev,
            bootstrapv=bootstrapv,
            scv=sample_coveragev,
            ))


import sys
main(sys.argv[1])
