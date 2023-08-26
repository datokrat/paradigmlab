package de.paulr.parser;

import de.paulr.parser.context.ParsingContext;

public class EndParser implements IParser<Object> {

	@Override
	public IResultIterator<Object> parse(String text, int position, ParsingContext context) {
		if (position == text.length()) {
			return SingleResultIterable.ok(null, position, context);
		} else {
			return SingleResultIterable.fail();
		}
	}

}
