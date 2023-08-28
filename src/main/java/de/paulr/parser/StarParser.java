package de.paulr.parser;

import de.paulr.parser.context.ParsingContext;
import de.paulr.util.Rope;

public class StarParser<T> implements IParser<Rope<T>> {

	private IParser<Rope<T>> parser;

	public StarParser(IParser<Rope<T>> parser) {
		this.parser = parser;
	}

	@Override
	public IResultIterator<Rope<T>> parse(String text, int position, ParsingContext context) {
		Rope<T> result = Rope.empty();
		int newPosition = position;
		ParsingContext newContext = context;
		while (true) {
			IResultIterator<Rope<T>> it = parser.parse(text, newPosition, newContext);
			if (it.hasResult()) {
				result = result.concat(it.getResult());
				newPosition = it.getNewPosition();
				newContext = it.getContext();
			} else {
				break;
			}
		}
		return SingleResultIterable.ok(result, newPosition, newContext);
	}

}
