package de.paulr.parser;

import java.util.List;

import de.paulr.parser.context.ParsingContext;
import de.paulr.util.Pair;

public class SequenceParser<T, U> implements IParser<Pair<T, U>> {

	private final IParser<T> first;
	private final IParser<U> second;

	public SequenceParser(IParser<T> first, IParser<U> second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public IResultIterator<Pair<T, U>> parse(String text, int position, ParsingContext context) {
		return new ResultIterator(text, position, context);
	}

	private class ResultIterator implements IParser.IResultIterator<Pair<T, U>> {

		private String text;
		private IResultIterator<T> firstIt;
		private IResultIterator<U> secondIt;
		private final int position;

		public ResultIterator(String text, int position, ParsingContext context) {
			this.text = text;
			this.position = position;
			firstIt = descend(first, text, position, context);
			handleChangeOfFirstIterator();
		}

		private void handleChangeOfFirstIterator() {
			while (firstIt.hasResult()) {
				secondIt = descend(second, text, firstIt.getNewPosition(), firstIt.getContext());
				if (secondIt.hasResult()) {
					break;
				}
				firstIt.next();
			}
		}

		@Override
		public boolean hasResult() {
			return firstIt.hasResult();
		}

		@Override
		public int getNewPosition() {
			return secondIt.getNewPosition();
		}

		@Override
		public Pair<T, U> getResult() {
			return new Pair<>(firstIt.getResult(), secondIt.getResult());
		}

		@Override
		public DebugTree getDebugTree() {
			return new DebugTree("sequence", getResult(), text, position, getNewPosition(),
					List.of(firstIt.getDebugTree(), secondIt.getDebugTree()));
		}

		@Override
		public void next() {
			secondIt.next();
			if (!secondIt.hasResult()) {
				firstIt.next();
				handleChangeOfFirstIterator();
			}
		}

		@Override
		public ParsingContext getContext() {
			return secondIt.getContext();
		}

	}
}
