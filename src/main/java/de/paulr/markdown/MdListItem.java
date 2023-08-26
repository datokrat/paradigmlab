package de.paulr.markdown;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MdListItem {

	private MdTextElement text;
	private List<MdListItem> children;

	public MdListItem(MdTextElement text, List<MdListItem> children) {
		this.text = text;
		this.children = children;
	}

	/**
	 * for testing
	 */
	public MdListItem(String text, List<MdListItem> children) {
		this(new MdTextElement(List.of(new MdTextExpression(text))), children);
	}

	public MdTextElement getText() {
		return text;
	}

	public List<MdListItem> getChildren() {
		return children;
	}

	@Override
	public int hashCode() {
		return Objects.hash(children, text);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MdListItem other = (MdListItem) obj;
		return Objects.equals(children, other.children) && Objects.equals(text, other.text);
	}

	@Override
	public String toString() {
		return "MdListItem [text=" + text + ", children=" + children + "]";
	}

	public void collectReferences(Consumer<String> consumer) {
		text.collectReferences(consumer);
		for (var child : children) {
			child.collectReferences(consumer);
		}
	}

}
