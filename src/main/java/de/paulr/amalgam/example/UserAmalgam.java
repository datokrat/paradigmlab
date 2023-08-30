package de.paulr.amalgam.example;

import de.paulr.amalgam.AmalgamAnnotation;
import de.paulr.amalgam.Feat;
import de.paulr.amalgam.Tags;

@AmalgamAnnotation
public class UserAmalgam {

	@Feat("fullName")
	public static String fullName(@Feat("firstName") String firstName,
		@Feat("lastName") String lastName) {
		return (firstName + " " + lastName).trim();
	}

	@Feat("firstName")
	public static String firstName(@Feat("fullName") String fullName) {
		int indexOfFirstSpace = fullName.indexOf(' ');
		if (indexOfFirstSpace == -1) {
			return fullName;
		} else {
			return fullName.substring(0, indexOfFirstSpace);
		}
	}

	@Feat("lastName")
	public static String lastName(@Feat("fullName") String fullName) {
		int indexOfFirstSpace = fullName.indexOf(' ');
		if (indexOfFirstSpace == -1) {
			return "";
		} else {
			return fullName.substring(indexOfFirstSpace);
		}
	}

	@Feat("birthdayMessage")
	@Tags("messages")
	public static String birthdayMessage(@Feat("firstName") String firstName) {
		return "Happy birthday, " + firstName + "!";
	}

	@Feat("invoiceMessage")
	@Tags("messages")
	public static String invoiceMessage(@Feat("fullName") String fullName) {
		return "Dear " + fullName + ", please pay until tomorrow!";
	}

}
