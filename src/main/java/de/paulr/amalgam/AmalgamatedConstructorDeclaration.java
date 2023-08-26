package de.paulr.amalgam;

import java.util.Arrays;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "constructor")
public class AmalgamatedConstructorDeclaration {

	@XmlAttribute(name = "name")
	private String name;

	@XmlElement(name = "parameter")
	private String[] parameters;

	public AmalgamatedConstructorDeclaration() {
	}

	public AmalgamatedConstructorDeclaration(String name, String[] parameters) {
		this.name = name;
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "AmalgamatedConstructorDeclaration [name=" + name + ", parameters=" + Arrays.toString(parameters) + "]";
	}

}
