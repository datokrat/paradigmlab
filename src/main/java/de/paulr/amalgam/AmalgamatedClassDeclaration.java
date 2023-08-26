package de.paulr.amalgam;

import java.util.Arrays;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "amalgamatedClass")
public class AmalgamatedClassDeclaration {

	@XmlAttribute(name = "amalgam")
	private Class<?> amalgam;

	@XmlElement(name = "known")
	private String[] knowns;

	@XmlElement(name = "knownClass")
	private String[] knownClasses;

	@XmlElement(name = "tag")
	private String[] tags;

	@XmlElement(name = "constructor")
	private AmalgamatedConstructorDeclaration[] constructors;

	public Class<?> getAmalgam() {
		return amalgam;
	}

	public void setAmalgam(Class<?> amalgam) {
		this.amalgam = amalgam;
	}

	public String[] getKnowns() {
		return knowns != null ? knowns : new String[] {};
	}

	public void setKnowns(String[] knowns) {
		this.knowns = knowns;
	}

	public String[] getKnownClasses() {
		return knownClasses != null ? knownClasses : new String[] {};
	}

	public void setKnownClasses(String[] knownClasses) {
		this.knownClasses = knownClasses;
	}

	public AmalgamatedConstructorDeclaration[] getConstructors() {
		return constructors != null ? constructors : new AmalgamatedConstructorDeclaration[] {};
	}

	public void setConstructors(AmalgamatedConstructorDeclaration[] constructors) {
		this.constructors = constructors;
	}

	public String[] getTags() {
		return tags != null ? tags : new String[] {};
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "AmalgamatedClassDeclaration [amalgam=" + amalgam + ", knowns="
			+ Arrays.toString(knowns) + ", tags=" + Arrays.toString(tags) + ", constructors="
			+ Arrays.toString(constructors) + "]";
	}

}
