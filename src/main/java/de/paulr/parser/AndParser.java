package de.paulr.parser;

import de.paulr.parser.context.ParsingContext;

public class AndParser<T, U> implements IParser<U> {

	private IParser<T> silentLeft;
	private IParser<U> right;

	public AndParser(IParser<T> silentLeft, IParser<U> right) {
		this.silentLeft = silentLeft;
		this.right = right;
	}

	@Override
	public IResultIterator<U> parse(String text, int position, ParsingContext context) {
		if (!silentLeft.parse(text, position, context).hasResult()) {
			return SingleResultIterable.fail();
		}

		return right.parse(text, position, context);
	}

}
