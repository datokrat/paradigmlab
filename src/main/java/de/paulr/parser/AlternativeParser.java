package de.paulr.parser;

import java.util.ArrayList;
import java.util.List;

import de.paulr.parser.context.ParsingContext;

public class AlternativeParser<T> implements IParser<T> {

	private final ArrayList<IParser<T>> alternatives;
	private final boolean greedy;

	public AlternativeParser(List<IParser<T>> alternatives, boolean greedy) {
		this.alternatives = new ArrayList<>(alternatives);
		this.greedy = greedy;
	}

	@Override
	public IResultIterator<T> parse(String text, int position, ParsingContext context) {
		return new ResultIterator(text, position, context);
	}

	private class ResultIterator implements IResultIterator<T> {

		private int currentAlternative;
		private IResultIterator<T> currentIt;
		private String text;
		private int position;
		private ParsingContext context;

		public ResultIterator(String text, int position, ParsingContext context) {
			this.text = text;
			this.position = position;
			this.context = context;

			currentAlternative = -1;
			tryNextAlternative();
		}

		private void tryNextAlternative() {
			do {
				currentAlternative++;
				if (currentAlternative >= alternatives.size()) {
					return;
				}
				currentIt = alternatives.get(currentAlternative).parse(text, position, context);
			} while (!currentIt.hasResult());
		}

		@Override
		public boolean hasResult() {
			return currentAlternative < alternatives.size() && currentIt.hasResult();
		}

		@Override
		public int getNewPosition() {
			return currentIt.getNewPosition();
		}

		@Override
		public T getResult() {
			return currentIt.getResult();
		}

		@Override
		public void next() {
			if (greedy) {
				currentAlternative = alternatives.size();
				return;
			}
			currentIt.next();
			if (!currentIt.hasResult()) {
				tryNextAlternative();
			}
		}

		@Override
		public ParsingContext getContext() {
			return context;
		}

	}

}
