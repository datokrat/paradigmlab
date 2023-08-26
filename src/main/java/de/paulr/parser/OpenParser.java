package de.paulr.parser;

import de.paulr.parser.context.ParsingContext;

public class OpenParser implements IParser<Void> {

	private IParser<Void> closeParser;

	public OpenParser(IParser<Void> closeParser) {
		this.closeParser = closeParser;
	}

	@Override
	public IResultIterator<Void> parse(String text, int position, ParsingContext context) {
		return SingleResultIterable.ok(null, position,
			context.expressionStack().update(stack -> stack.push(closeParser)));
	}

}
