package de.paulr.aoc2023;

import static de.paulr.aoc2023.AoCUtil.input;
import static de.paulr.aoc2023.AoCUtil.inputAsString;

import java.util.List;

public abstract class ASolution {

	public List<String> lines;
	public String file;

	public ASolution(String filename) {
		lines = input(filename);
	}

	public void readInput(String filename) {
		lines = input(filename);
		file = inputAsString(filename);
	}

	public abstract Object partA();

	public abstract Object partB();

}
