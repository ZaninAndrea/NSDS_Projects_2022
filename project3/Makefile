.PHONY: start visualize

start: simulation
	mpirun -np 4 ./simulation

visualize:
	python visualize.py

simulation: src/*
	mpicxx -std=c++20 -Wall src/Individual.cpp src/Simulation.cpp -o simulation; 