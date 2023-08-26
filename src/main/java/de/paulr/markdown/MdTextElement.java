package de.paulr.markdown;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class MdTextElement implements TopLevelMdElement {

	private List<MdInlineExpression> expressions;

	public MdTextElement(List<MdInlineExpression> expressions) {
		this.expressions = expressions;
	}

	public List<MdInlineExpression> getExpressions() {
		return expressions;
	}

	public void setExpressions(List<MdInlineExpression> expressions) {
		this.expressions = expressions;
	}

	public void collectReferences(Consumer<String> consumer) {
		for (var expr : expressions) {
			expr.collectReferences(consumer);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(expressions);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MdTextElement other = (MdTextElement) obj;
		return Objects.equals(expressions, other.expressions);
	}

	@Override
	public String toString() {
		return "MdTextElement [expressions=" + expressions + "]";
	}

}
