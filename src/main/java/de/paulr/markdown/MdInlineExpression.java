package de.paulr.markdown;

import java.util.function.Consumer;

public sealed interface MdInlineExpression
	permits MdTextExpression, MdLinkExpression, MdMathExpression {

	Type getType();

	void collectReferences(Consumer<String> consumer);

	String getTextualContent();

	public enum Type {
		Text, Link, Math
	}

}
