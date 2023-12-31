package de.paulr.markdown;

import java.util.Objects;
import java.util.function.Consumer;

public final class MdTextExpression implements MdInlineExpression {

	private String text;

	public MdTextExpression(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MdTextExpression other = (MdTextExpression) obj;
		return Objects.equals(text, other.text);
	}

	@Override
	public String toString() {
		return "MdTextExpression [text=" + text + "]";
	}

	@Override
	public void collectReferences(Consumer<String> consumer) {
	}

	@Override
	public String getTextualContent() {
		return getText();
	}

	@Override
	public Type getType() {
		return Type.Text;
	}

}
