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

	public ASolution(int year, int day) {
		this(Integer.toString(year) + "_" + Integer.toString(day) + ".txt");
	}

	public ASolution(int year, int day, String suffix) {
		this(Integer.toString(year) + "_" + Integer.toString(day) + suffix + ".txt");
	}

	public void readInput(String filename) {
		lines = input(filename);
		file = inputAsString(filename);
	}

	public abstract Object partA();

	public abstract Object partB();

}
