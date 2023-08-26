package de.paulr.markdown;

import java.util.function.Consumer;

public final class MdMathExpression implements MdInlineExpression {

	private String latex;

	public MdMathExpression(String latex) {
		this.latex = latex;
	}

	@Override
	public void collectReferences(Consumer<String> consumer) {
	}

	public String getLatex() {
		return latex;
	}

	@Override
	public String getTextualContent() {
		return latex;
	}

	@Override
	public Type getType() {
		return Type.Math;
	}

}
