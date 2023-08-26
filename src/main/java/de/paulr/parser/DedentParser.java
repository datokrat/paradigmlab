package de.paulr.parser;

import java.util.Optional;

import de.paulr.parser.context.ParsingContext;

public class DedentParser implements IParser<Object> {

	@Override
	public IResultIterator<Object> parse(String text, int position, ParsingContext context) {
		Optional<ParsingContext> newStack = context.dedent();
		if (newStack.isPresent()) {
			return SingleResultIterable.ok(null, position, newStack.get());
		} else {
			return SingleResultIterable.fail();
		}
	}

}
