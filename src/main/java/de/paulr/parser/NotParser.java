package de.paulr.parser;

import de.paulr.parser.context.ParsingContext;

public class NotParser<T> implements IParser<Void> {

	private IParser<T> parser;

	public NotParser(IParser<T> parser) {
		this.parser = parser;
	}

	@Override
	public IResultIterator<Void> parse(String text, int position, ParsingContext context) {
		if (parser.parse(text, position, context).hasResult()) {
			return SingleResultIterable.fail();
		} else {
			return SingleResultIterable.ok(null, position, context);
		}
	}

}
