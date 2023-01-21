

def main(pgm, killed, total, equivalent, stotal):
    alive_pers = (total - killed) * 100.0/total
    remaining_mortal_pers = alive_pers - (equivalent*100.0)/stotal
    killable = killed*100.0/total + remaining_mortal_pers 
    print(pgm, "killed:", "%.2f" % (killed*100.0/total), "killable:", "%.2f" % killable)

import sys
main(sys.argv[1], *[int(i) for i in sys.argv[2:]])
