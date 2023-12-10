package de.paulr.util;

public interface Lens<T, U> {

	T get(U complexObject);

	U withNewValue(U complexObject, T newValue);

}
