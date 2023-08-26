package de.paulr.markdown;

import java.util.function.Consumer;

public sealed interface TopLevelMdElement permits MdHeadingElement, MdTextElement, MdListElement {

	void collectReferences(Consumer<String> consumer);

}
