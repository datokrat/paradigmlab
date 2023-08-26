package de.paulr.amalgam;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;

public class AmalgamCompiler {

	public void run(String classToAmalgamate) throws IOException {
		StringWriter writer = new StringWriter();

		AmalgamatedClass amalgamatedClass = new AmalgamatedClass(classToAmalgamate);
		System.out.println("Generating " + amalgamatedClass.getClassPath());

		amalgamatedClass.getGeneratedJava().forEach(line -> writer.write(line + "\n"));

		try (BufferedWriter bufferedWriter = Files
			.newBufferedWriter(amalgamatedClass.getClassPath())) {
			bufferedWriter.write(writer.toString());
		}
	}

}
