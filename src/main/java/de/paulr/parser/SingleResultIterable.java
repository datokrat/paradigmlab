package de.paulr.parser;

import de.paulr.parser.context.ParsingContext;

public class SingleResultIterable<T> implements IParser.IResultIterator<T> {

	private boolean hasResult;
	private T result;
	private int position;
	private ParsingContext context;

	private SingleResultIterable(boolean hasResult, T result, int position, ParsingContext context) {
		this.hasResult = hasResult;
		this.result = result;
		this.position = position;
		this.context = context;
	}

	public static <T> SingleResultIterable<T> ok(T result, int position, ParsingContext context) {
		return new SingleResultIterable<T>(true, result, position, context);
	}

	public static <T> SingleResultIterable<T> fail() {
		return new SingleResultIterable<T>(false, null, -1, null);
	}

	@Override
	public boolean hasResult() {
		return hasResult;
	}

	@Override
	public int getNewPosition() {
		return position;
	}

	@Override
	public T getResult() {
		return result;
	}

	@Override
	public void next() {
		result = null;
		hasResult = false;
	}

	@Override
	public ParsingContext getContext() {
		return context;
	}

}
