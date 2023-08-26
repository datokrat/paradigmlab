// AUTO-GENERATED AMALGAMATED CLASS
package de.paulr.amalgam;

import java.util.Optional;

public class AmalgamatedClass {
private java.lang.String name;
private Optional<java.lang.String> packageName = Optional.empty();
private Optional<java.lang.String> simpleName = Optional.empty();
private Optional<java.nio.file.Path> classPath = Optional.empty();
private Optional<java.nio.file.Path> declarationPath = Optional.empty();
private Optional<de.paulr.amalgam.AmalgamatedClassDeclaration> declaration = Optional.empty();
private Optional<java.util.Set<java.lang.String>> knowns = Optional.empty();
private Optional<de.paulr.amalgam.Amalgam> amalgam = Optional.empty();
private Optional<java.util.Set<de.paulr.amalgam.AmalgamatedClass>> knownClasses = Optional.empty();
private Optional<de.paulr.util.Rope<java.lang.String>> generatedHeader = Optional.empty();
private Optional<java.util.Map<java.lang.String, de.paulr.amalgam.AmalgamatedClass>> outsourcedFeatures = Optional.empty();
private Optional<de.paulr.util.Rope<java.lang.String>> generatedConstructorDeclarations = Optional.empty();
private Optional<java.util.Map<java.lang.String, de.paulr.amalgam.AmalgamData.Rule>> derivableFeatures = Optional.empty();
private Optional<java.util.Set<java.lang.String>> knownOrDerivableFeatures = Optional.empty();
private Optional<de.paulr.util.Rope<java.lang.String>> generatedFieldDeclarations = Optional.empty();
private Optional<de.paulr.util.Rope<java.lang.String>> generatedGetters = Optional.empty();
private Optional<de.paulr.util.Rope<java.lang.String>> generatedFactoryMethods = Optional.empty();
private Optional<de.paulr.util.Rope<java.lang.String>> generatedJava = Optional.empty();
public AmalgamatedClass(java.lang.String name) {
this.name = name;
}
@java.lang.SuppressWarnings("unused")
private AmalgamatedClass() {}
public java.lang.String getName() {
return name;
}
public java.lang.String getPackageName() {
return packageName.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.packageName(getName()));
}
public java.lang.String getSimpleName() {
return simpleName.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.simpleName(getName()));
}
public java.nio.file.Path getClassPath() {
return classPath.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.classPath(getName()));
}
public java.nio.file.Path getDeclarationPath() {
return declarationPath.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.declarationPath(getName()));
}
public de.paulr.amalgam.AmalgamatedClassDeclaration getDeclaration() {
return declaration.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.declaration(getDeclarationPath()));
}
public java.util.Set<java.lang.String> getKnowns() {
return knowns.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.knowns(getDeclaration()));
}
public de.paulr.amalgam.Amalgam getAmalgam() {
return amalgam.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.amalgam(getDeclaration()));
}
public java.util.Set<de.paulr.amalgam.AmalgamatedClass> getKnownClasses() {
return knownClasses.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.knownClasses(getDeclaration()));
}
public de.paulr.util.Rope<java.lang.String> getGeneratedHeader() {
return generatedHeader.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.generatedHeader(getPackageName()));
}
public java.util.Map<java.lang.String, de.paulr.amalgam.AmalgamatedClass> getOutsourcedFeatures() {
return outsourcedFeatures.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.outsourcedFeaturesAndSource(getKnownClasses()));
}
public de.paulr.util.Rope<java.lang.String> getGeneratedConstructorDeclarations() {
return generatedConstructorDeclarations.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.generatedConstructorDeclaration(getKnowns(), getKnownClasses(), getSimpleName(), getAmalgam()));
}
public java.util.Map<java.lang.String, de.paulr.amalgam.AmalgamData.Rule> getDerivableFeatures() {
return derivableFeatures.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.derivableFeatures(getKnowns(), getOutsourcedFeatures(), getAmalgam()));
}
public java.util.Set<java.lang.String> getKnownOrDerivableFeatures() {
return knownOrDerivableFeatures.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.knownOrDerivableFeatures(getKnowns(), getDerivableFeatures()));
}
public de.paulr.util.Rope<java.lang.String> getGeneratedFieldDeclarations() {
return generatedFieldDeclarations.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.generatedFieldDeclarations(getKnowns(), getKnownClasses(), getKnownOrDerivableFeatures(), getOutsourcedFeatures(), getAmalgam()));
}
public de.paulr.util.Rope<java.lang.String> getGeneratedGetters() {
return generatedGetters.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.generatedGetters(getKnowns(), getOutsourcedFeatures(), getDeclaration(), getDerivableFeatures(), getAmalgam()));
}
public de.paulr.util.Rope<java.lang.String> getGeneratedFactoryMethods() {
return generatedFactoryMethods.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.generatedFactoryMethods(getDeclaration(), getKnowns(), getSimpleName(), getKnownClasses(), getOutsourcedFeatures(), getAmalgam(), getDerivableFeatures()));
}
public de.paulr.util.Rope<java.lang.String> getGeneratedJava() {
return generatedJava.orElseGet(() -> de.paulr.amalgam.AmalgamatedClassAmalgam.generatedJava(getSimpleName(), getGeneratedHeader(), getGeneratedFieldDeclarations(), getGeneratedConstructorDeclarations(), getGeneratedFactoryMethods(), getGeneratedGetters()));
}
}
