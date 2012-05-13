import os

for f in os.listdir('.'):
    if f.endswith('.gif'):
        os.popen('convert ' + f + ' ' + f[:-4] + ".pdf").readlines()
