// AUTO-GENERATED AMALGAMATED CLASS
package de.paulr.amalgam.example;

import java.util.Optional;

public class UserWithMessages {
private Optional<java.lang.String> birthdayMessage = Optional.empty();
private Optional<java.lang.String> invoiceMessage = Optional.empty();
de.paulr.amalgam.example.User knownClass_de_paulr_amalgam_example_User;
public UserWithMessages(de.paulr.amalgam.example.User knownClass_de_paulr_amalgam_example_User) {
this.knownClass_de_paulr_amalgam_example_User = knownClass_de_paulr_amalgam_example_User;
}
@java.lang.SuppressWarnings("unused")
private UserWithMessages() {}
@java.lang.SuppressWarnings("unused")
public static UserWithMessages ofFullName(java.lang.String featfullName) {
var obj = new UserWithMessages();
var tmpfullName = featfullName;
var tmpfirstName = de.paulr.amalgam.example.UserAmalgam.firstName(tmpfullName);
var tmplastName = de.paulr.amalgam.example.UserAmalgam.lastName(tmpfullName);
obj.knownClass_de_paulr_amalgam_example_User = new de.paulr.amalgam.example.User(tmpfirstName, tmplastName);
return obj;
}
public java.lang.String getBirthdayMessage() {
birthdayMessage = Optional.of(birthdayMessage.orElseGet(() -> de.paulr.amalgam.example.UserAmalgam.birthdayMessage(getFirstName())));
return birthdayMessage.get();
}
public java.lang.String getInvoiceMessage() {
invoiceMessage = Optional.of(invoiceMessage.orElseGet(() -> de.paulr.amalgam.example.UserAmalgam.invoiceMessage(getFullName())));
return invoiceMessage.get();
}
public java.lang.String getFirstName() {
return knownClass_de_paulr_amalgam_example_User.getFirstName();
}
public java.lang.String getLastName() {
return knownClass_de_paulr_amalgam_example_User.getLastName();
}
public java.lang.String getFullName() {
return knownClass_de_paulr_amalgam_example_User.getFullName();
}
}
