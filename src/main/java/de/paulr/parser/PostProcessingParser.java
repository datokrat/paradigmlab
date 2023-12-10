package de.paulr.parser;

import java.util.function.Function;

import de.paulr.parser.context.ParsingContext;

public class PostProcessingParser<T, U> implements IParser<U> {

	private IParser<T> parser;
	private Function<T, U> fn;

	public PostProcessingParser(IParser<T> parser, Function<T, U> fn) {
		this.parser = parser;
		this.fn = fn;
	}

	@Override
	public IResultIterator<U> parse(String text, int position, ParsingContext context) {
		return new ResultIterator(text, position, context);
	}

	private class ResultIterator implements IParser.IResultIterator<U> {
		private IResultIterator<T> it;

		public ResultIterator(String text, int position, ParsingContext context) {
			this.it = parser.parse(text, position, context);
		}

		@Override
		public boolean hasResult() {
			return it.hasResult();
		}

		@Override
		public int getNewPosition() {
			return it.getNewPosition();
		}

		@Override
		public U getResult() {
			return fn.apply(it.getResult());
		}

		@Override
		public DebugTree getDebugTree() {
			return it.getDebugTree().withResult(getResult());
		}

		@Override
		public void next() {
			it.next();
		}

		@Override
		public ParsingContext getContext() {
			return it.getContext();
		}
	}

}
