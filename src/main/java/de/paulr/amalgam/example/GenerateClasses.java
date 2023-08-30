package de.paulr.amalgam.example;

import java.io.IOException;

import de.paulr.amalgam.AmalgamCompiler;

public class GenerateClasses {

	public static void main(String[] args) throws IOException {
		new AmalgamCompiler().run("de.paulr.amalgam.example.User");
		new AmalgamCompiler().run("de.paulr.amalgam.example.UserWithMessages");
	}

}
