.DEFAULT_GOAL := esem2024-chaos.pdf
DRAW=Rscript ./bin/draw_sep_suites_charts.R -H 1.5 -W 3.0
all: esem2024-chaos.pdf
model=abundance

all: clean esem2024-chaos.pdf

esem2024-chaos.pdf: esem2024-chaos.tex esem2024-chaos.bib
	# make summarycharts
	latexmk -pdf esem2024-chaos.tex
	-killall qlmanage

summarycharts: charts-test/.summary

charts-test/.summary: data/summary.csv
	mkdir -p data/all
	python3 ./bin/add_manual_columns.py data/summary.csv > data/all/summary.csv
	mkdir -p charts-test/organic
	mkdir -p charts-test/evosuite
	mkdir -p charts-test/randoop
	$(DRAW) -M $(model) -t organic -s ./data/all/summary.csv -o ./charts-test/organic
	$(DRAW) -M $(model) -t evosuite -s ./data/all/summary.csv -o ./charts-test/evosuite
	$(DRAW) -M $(model) -t randoop -s ./data/all/summary.csv -o ./charts-test/randoop
	touch $@

# incidence is chao2
# ACE - abundance Coverage
# ICE - incidence Coverage

clean:
	latexmk -C
	# rm -rf charts-test

pull:
	git pull --rebase ox master --autostash

push:
	git push ox master
