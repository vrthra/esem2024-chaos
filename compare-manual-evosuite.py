import sys
import statistics
estimators=[
 "ICE_all_rare"
,"Zelterman"
,"Chao_Bunge"
,"Jackknife"
,"Chao"
,"Chao_bias_corrected"
,"improved_Chao"
,"ICE"
,"improved_ICE"
,"Unpmle"
,"Bootstrap"
,"Pnpmle"
,"PCG"]

def between(A, B, C):
    assert B <= C
    return (A >= B and A <= C)

def process(lines, estimator):
    arr = [l for l in lines if l['estimator'] == estimator]
    est = []
    t = []
    s = 0
    for subject in arr:
        total_mutants = int(subject['total_mutants'])
        #killed = int(subject['killed'])
        #per_killed = killed * 1.0 / total_mutants * 100.0
        mestimate = float(subject['MEstimate'])
        estimation_ = subject['estimation']
        if not estimation_: continue
        estimation = float(estimation_)
        if estimation == float('inf'): continue
        if estimation < 0: continue

        CIL_ = subject['CILower']
        if not CIL_: continue
        CIL = float(CIL_)
        CIU = float(subject['CIUpper'])
        ML = float(subject['MLower'])
        MU = float(subject['MUpper'])

        if CIL < 0: CIL = 0
        if CIU < 0: continue
        s += 1

        if between(CIL, ML, MU) or between(CIU, ML, MU) or between(ML, CIL, CIU) or between(MU, CIL, CIU):
            t.append(subject['subject'])
        v = abs(estimation - mestimate) / mestimate * 100.0
        est.append(v)
    print("%s\t&\t%s\t&\t%s\t&%.1f\t&\t%.1f\\\\" % (estimator, s,len(t),statistics.mean(est), statistics.stdev(est)))
    #print('overlaps:',', '.join(t))



import csv
with open(sys.argv[1], "r" ) as theFile:
    reader = csv.DictReader(theFile)
    lines = []
    for line in reader:
        if line['testsuite'] not in [sys.argv[2]]: continue
        if line['type'] not in [sys.argv[3]]: continue
        lines.append(line)
    print("\\begin{tabular}{|l|r|r|r|r|r|}\n\\hline")
    print('Estimator\t&Valid Estimates\t&CI Overlaps\t&Mean\t&Stdev\t\\\\')
    print("\\hline")

    for estimator in estimators:
        process(lines, estimator)
    print("\\hline")
    print("\\end{tabular}")

