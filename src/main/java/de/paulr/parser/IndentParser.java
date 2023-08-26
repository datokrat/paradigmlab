package de.paulr.parser;

import de.paulr.parser.context.ParsingContext;

public class IndentParser implements IParser<Object> {

	private String prefix;

	public IndentParser(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public IResultIterator<Object> parse(String text, int position, ParsingContext context) {
		return SingleResultIterable.ok(null, position, context.indent(prefix));
	}

}
