// AUTO-GENERATED AMALGAMATED CLASS
package de.paulr.amalgam.example;

import java.util.Optional;

public class User {
private java.lang.String firstName;
private java.lang.String lastName;
private Optional<java.lang.String> fullName = Optional.empty();
public User(java.lang.String firstName, java.lang.String lastName) {
this.firstName = firstName;
this.lastName = lastName;
}
@java.lang.SuppressWarnings("unused")
private User() {}
@java.lang.SuppressWarnings("unused")
public static User ofFullName(java.lang.String featfullName) {
var obj = new User();
var tmpfullName = featfullName;
obj.fullName = Optional.of(tmpfullName);
var tmpfirstName = de.paulr.amalgam.example.UserAmalgam.firstName(tmpfullName);
obj.firstName = tmpfirstName;
var tmplastName = de.paulr.amalgam.example.UserAmalgam.lastName(tmpfullName);
obj.lastName = tmplastName;
return obj;
}
public java.lang.String getFirstName() {
return firstName;
}
public java.lang.String getLastName() {
return lastName;
}
public java.lang.String getFullName() {
fullName = Optional.of(fullName.orElseGet(() -> de.paulr.amalgam.example.UserAmalgam.fullName(getFirstName(), getLastName())));
return fullName.get();
}
}
