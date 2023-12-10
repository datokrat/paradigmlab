package de.paulr.parser;

import java.util.ArrayList;
import java.util.List;

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
		List<DebugTree> children = context.isDebug() ? new ArrayList<>() : null;
		while (true) {
			IResultIterator<Rope<T>> it = parser.parse(text, newPosition, newContext);
			if (it.hasResult()) {
				result = result.concat(it.getResult());
				if (context.isDebug()) {
					children.add(it.getDebugTree());
				}
				newPosition = it.getNewPosition();
				newContext = it.getContext();
			} else {
				break;
			}
		}
		return SingleResultIterable.ok("star", result, text, position, newPosition, newContext, children);
	}

}
