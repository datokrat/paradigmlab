package de.paulr.parser;

import java.util.List;
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
			return new ResultIterator(text, position, OptionalInt.empty(), null);
		}

		for (int i = 0; i < string.length(); ++i) {
			if (text.charAt(position + i) != string.charAt(i)) {
				return new ResultIterator(text, position, OptionalInt.empty(), null);
			}
		}

		return new ResultIterator(text, position, OptionalInt.of(position + string.length()), context);
	}

	private class ResultIterator implements IResultIterator<String> {

		private OptionalInt newPosition;
		private ParsingContext context;
		private int initialPosition;
		private String text;

		public ResultIterator(String text, int initialPosition, OptionalInt newPosition, ParsingContext context) {
			this.text = text;
			this.initialPosition = initialPosition;
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
		public DebugTree getDebugTree() {
			return new DebugTree("string", string, text, initialPosition, getNewPosition(), List.of());
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
