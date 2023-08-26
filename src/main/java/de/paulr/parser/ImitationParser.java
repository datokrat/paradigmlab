package de.paulr.parser;

import de.paulr.parser.context.ParsingContext;

public class ImitationParser<T> implements IParser<T> {

	private IParser<T> parser;

	public void imitate(IParser<T> parser) {
		this.parser = parser;
	}

	@Override
	public IResultIterator<T> parse(String text, int position, ParsingContext context) {
		return parser.parse(text, position, context);
	}

}
