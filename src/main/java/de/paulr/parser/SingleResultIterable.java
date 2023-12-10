package de.paulr.parser;

import java.util.List;

import de.paulr.parser.IParser.DebugTree;
import de.paulr.parser.context.ParsingContext;

public class SingleResultIterable<T> implements IParser.IResultIterator<T> {

	private boolean hasResult;
	private T result;
	private int position;
	private int initialPosition;
	private String text;
	private ParsingContext context;
	private String label;
	private List<DebugTree> children;

	private SingleResultIterable(String label, boolean hasResult, T result, String text, int initialPosition,
			int position, ParsingContext context, List<DebugTree> children) {
		this.hasResult = hasResult;
		this.result = result;
		this.position = position;
		this.text = text;
		this.initialPosition = initialPosition;
		this.context = context;
		this.label = label;
		this.children = children;
	}

	public static <T> SingleResultIterable<T> ok(String label, T result, String text, int initialPosition, int position,
			ParsingContext context) {
		return new SingleResultIterable<>(label, true, result, text, initialPosition, position, context, List.of());
	}

	public static <T> SingleResultIterable<T> ok(String label, T result, String text, int initialPosition, int position,
			ParsingContext context, List<DebugTree> children) {
		return new SingleResultIterable<>(label, true, result, text, initialPosition, position, context, children);
	}

	public static <T> SingleResultIterable<T> fail() {
		return new SingleResultIterable<>(null, false, null, null, -1, -1, null, null);
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
	public DebugTree getDebugTree() {
		return new DebugTree(label, result, text, initialPosition, position, List.of());
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
