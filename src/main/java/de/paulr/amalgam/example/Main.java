package de.paulr.amalgam.example;

import java.util.List;

public class Main {

	public static void main(String[] args) {
		// Create instance with factory method configured in User.mlgm
		// and derive the first name
		User john = User.ofFullName("John Doe");
		System.out.println(
			"The first name of " + john.getFullName() + " is " + john.getFirstName() + ".");

		// Create instance by passing known features defined in User.mlgm
		// and derive the full name
		User jane = new User("Jane", "Doe");
		System.out.println("His sister is " + jane.getFullName());

		// Create instance of UserWithMessages by passing a User
		// and derive the birthday message
		// (possible because tag "messages" was set in UserWithMessages.mlgm)
		UserWithMessages johnWithMessages = new UserWithMessages(john);
		System.out.println(johnWithMessages.getBirthdayMessage());

		// Property-based testing with amalgams
		List<User> exampleUsers = List.of( //
			new User("John", "Doe"), new User("Jane", "Doe"), //
			new User("Vincent", "van Gogh"), new User("Vincent Wilhelm", "van Gogh"), //
			new User("Antonín", "Dvořák"));
		for (User user : exampleUsers) {
			assertEquals(UserAmalgam.firstName(user.getFullName()), user.getFirstName(),
				"First name determined by full name is wrong");
		}
	}

	private static void assertEquals(String first, String second, String message) {
		if (!first.equals(second)) {
			System.out.println(message + ": '" + first + "', '" + second + "'");
		}
	}

}
