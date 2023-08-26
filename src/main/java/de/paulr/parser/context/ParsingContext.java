package de.paulr.parser.context;

import java.util.Optional;
import java.util.function.Function;

import de.paulr.parser.IParser;
import de.paulr.parser.Parsers;
import de.paulr.util.SinglyLinkedList;

public class ParsingContext {

	private SinglyLinkedList<IParser<Void>> indentStack;

	private SinglyLinkedList<IParser<Void>> endOfExpressionStack;

	public ParsingContext() {
		indentStack = SinglyLinkedList.empty();
		endOfExpressionStack = SinglyLinkedList.empty();
	}

	private ParsingContext(SinglyLinkedList<IParser<Void>> indentStack,
		SinglyLinkedList<IParser<Void>> endOfExpressionStack) {
		this.indentStack = indentStack;
		this.endOfExpressionStack = endOfExpressionStack;
	}

	public ParsingContext indent(String prefix) {
		return indentStack().update(stack -> stack.push(indentParserWithPrefix(prefix)));
	}

	public Optional<ParsingContext> dedent() {
		return indentStack().maybeUpdate(stack -> stack.tryGetTail());
	}

	public Lens<ParsingContext, SinglyLinkedList<IParser<Void>>> indentStack() {
		return Lens.startingFrom(this).zoom(ctx -> ctx.indentStack,
			stack -> new ParsingContext(stack, endOfExpressionStack));
	}

	public Lens<ParsingContext, SinglyLinkedList<IParser<Void>>> expressionStack() {
		return Lens.startingFrom(this).zoom(ctx -> ctx.endOfExpressionStack,
			stack -> new ParsingContext(indentStack, stack));
	}

	public IParser<Void> indentParser() {
		if (indentStack.isEmpty()) {
			return Parsers.EPSILON;
		} else {
			return indentStack.getHead();
		}
	}

	private IParser<Void> indentParserWithPrefix(String prefix) {
		if (indentStack.isEmpty()) {
			return Parsers.exact(prefix).mapToNull();
		} else {
			return Parsers.exact(prefix).then(indentStack.getHead()).mapToNull();
		}
	}

	public static interface Lens<T, U> {

		U get();

		T update(Function<U, U> updater);

		Optional<T> maybeUpdate(Function<U, Optional<U>> updater);

		public static <T> Lens<T, T> startingFrom(final T start) {
			return new Lens<>() {

				@Override
				public T update(Function<T, T> updater) {
					return updater.apply(start);
				}

				@Override
				public Optional<T> maybeUpdate(Function<T, Optional<T>> updater) {
					return updater.apply(start);
				}

				@Override
				public T get() {
					return start;
				}

			};
		}

		default <V> Lens<T, V> zoom(Function<U, V> zoomIn, Function<V, U> zoomOut) {
			final Lens<T, U> self = this;
			return new Lens<>() {

				@Override
				public T update(Function<V, V> fn) {
					return self.update(u -> zoomOut.apply(fn.apply(zoomIn.apply(u))));
				}

				@Override
				public Optional<T> maybeUpdate(Function<V, Optional<V>> fn) {
					return self.maybeUpdate(u -> fn.apply(zoomIn.apply(u)).map(zoomOut));
				}

				@Override
				public V get() {
					return zoomIn.apply(self.get());
				}

			};
		}

	}

}
