package de.paulr.parser;

import de.paulr.parser.context.ParsingContext;

public class AlignParser implements IParser<Void> {

	@Override
	public IResultIterator<Void> parse(String text, int position, ParsingContext context) {
		return context.indentParser().parse(text, position, context);
	}

}
