package de.paulr.parser;

import java.util.OptionalInt;

import de.paulr.parser.context.ParsingContext;

public class StringParser implements IParser<String> {

	private String string;

	public StringParser(String string) {
		this.string = string;
	}

	@Override
	public IResultIterator<String> parse(String text, int position, ParsingContext context) {
		if (position + string.length() > text.length()) {
			return new ResultIterator(OptionalInt.empty(), null);
		}

		for (int i = 0; i < string.length(); ++i) {
			if (text.charAt(position + i) != string.charAt(i)) {
				return new ResultIterator(OptionalInt.empty(), null);
			}
		}

		return new ResultIterator(OptionalInt.of(position + string.length()), context);
	}

	private class ResultIterator implements IResultIterator<String> {

		private OptionalInt newPosition;
		private ParsingContext context;

		public ResultIterator(OptionalInt newPosition, ParsingContext context) {
			this.newPosition = newPosition;
			this.context = context;
		}

		@Override
		public boolean hasResult() {
			return newPosition.isPresent();
		}

		@Override
		public int getNewPosition() {
			return newPosition.orElseThrow();
		}

		@Override
		public String getResult() {
			newPosition.orElseThrow();
			return string;
		}

		@Override
		public void next() {
			newPosition = OptionalInt.empty();
		}

		@Override
		public ParsingContext getContext() {
			return context;
		}

	}

}
