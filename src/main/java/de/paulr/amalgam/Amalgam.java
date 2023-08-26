// AUTO-GENERATED AMALGAMATED CLASS
package de.paulr.amalgam;

import java.util.Optional;

public class Amalgam {
private java.lang.Class<?> clazz;
private java.lang.String[] tags;
private Optional<de.paulr.amalgam.AmalgamData> amalgamData = Optional.empty();
private Optional<java.lang.String> name = Optional.empty();
private Optional<java.lang.String> packageName = Optional.empty();
private Optional<java.lang.String> simpleName = Optional.empty();
public Amalgam(java.lang.Class<?> clazz, java.lang.String[] tags) {
this.clazz = clazz;
this.tags = tags;
}
@java.lang.SuppressWarnings("unused")
private Amalgam() {}
public java.lang.Class<?> getClazz() {
return clazz;
}
public java.lang.String[] getTags() {
return tags;
}
public de.paulr.amalgam.AmalgamData getAmalgamData() {
return amalgamData.orElseGet(() -> de.paulr.amalgam.AmalgamAmalgam.amalgamData(getClazz(), getTags()));
}
public java.lang.String getName() {
return name.orElseGet(() -> de.paulr.amalgam.AmalgamAmalgam.nameForClazz(getClazz()));
}
public java.lang.String getPackageName() {
return packageName.orElseGet(() -> de.paulr.amalgam.AmalgamAmalgam.packageName(getName()));
}
public java.lang.String getSimpleName() {
return simpleName.orElseGet(() -> de.paulr.amalgam.AmalgamAmalgam.simpleName(getName()));
}
}
