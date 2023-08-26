package de.paulr.markdown;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MarkdownPage {

	private List<TopLevelMdElement> elements;

	public MarkdownPage(List<TopLevelMdElement> elements) {
		this.elements = elements;
	}

	public List<TopLevelMdElement> getElements() {
		return elements;
	}

	public void setElements(List<TopLevelMdElement> elements) {
		this.elements = elements;
	}

	public void collectReferences(Consumer<String> consumer) {
		for (var el : elements) {
			el.collectReferences(consumer);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(elements);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarkdownPage other = (MarkdownPage) obj;
		return Objects.equals(elements, other.elements);
	}

	@Override
	public String toString() {
		return "MarkdownPage [elements=" + elements + "]";
	}

}
