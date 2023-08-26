package de.paulr.markdown;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class MdListElement implements TopLevelMdElement {

	private List<MdListItem> items;

	public MdListElement(List<MdListItem> items) {
		this.items = items;
	}

	public List<MdListItem> getItems() {
		return items;
	}

	@Override
	public int hashCode() {
		return Objects.hash(items);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MdListElement other = (MdListElement) obj;
		return Objects.equals(items, other.items);
	}

	@Override
	public String toString() {
		return "MdListElement [items=" + items + "]";
	}

	@Override
	public void collectReferences(Consumer<String> consumer) {
		for (var item : items) {
			item.collectReferences(consumer);
		}
	}

}
