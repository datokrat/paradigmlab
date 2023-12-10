package de.paulr.parser;

import static de.paulr.parser.Parsers.exact;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import de.paulr.parser.context.ParsingContext;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

public interface IParser<T> {

	IResultIterator<T> parse(String text, int position, ParsingContext context);

	default Optional<T> parseMaybe(String text, int position) {
		var result = parse(text, position);
		if (result.hasResult()) {
			return Optional.of(result.getResult());
		} else {
			return Optional.empty();
		}
	}

	default T parseOne(String text) {
		return parse(text, 0).getResult();
	}

	default IResultIterator<T> parse(String text, int position) {
		return parse(text, position, new ParsingContext(false));
	}

	default IResultIterator<T> debug(String text, int position) {
		return parse(text, position, new ParsingContext(true));
	}

	default <U> IParser<U> inputTo(IParser<Function<T, U>> dependentParser) {
		return this.then(dependentParser).map(Pair.fn((first, second) -> second.apply(first)));
	}

	default IParser<T> indented(String prefix) {
		return Parsers.indented(prefix, this);
	}

	default IParser<Rope<T>> star() {
		return Parsers.star(this);
	}

	default IParser<Rope<T>> plus() {
		return this.then(star()).map(Rope::addLeft);
	}

	default IParser<Optional<T>> optional() {
		return map(Optional::of).or(Parsers.exact("").mapTo(Optional.empty()));
	}

	default IParser<T> or(IParser<T> other) {
		return Parsers.fstAlt(List.of(this, other));
	}

	default IParser<Void> mapToNull() {
		return mapTo(null);
	}

	default <U> IParser<U> mapTo(U result) {
		return map(__ -> result);
	}

	default <U> IParser<Pair<T, U>> annotate(U annotation) {
		return map(Pair.annotate(annotation));
	}

	default <U> IParser<U> map(Function<T, U> fn) {
		return Parsers.pp(this, fn);
	}

	default <U> IParser<Pair<T, U>> then(IParser<U> parser) {
		return Parsers.pair(this, parser);
	}

	default IParser<T> thenSilently(String string) {
		return this.thenSilently(exact(string));
	}

	default <U> IParser<T> thenSilently(IParser<U> parser) {
		return this.then(parser).map(Pair::first);
	}

	default <U> IParser<U> silentlyThen(IParser<U> parser) {
		return this.then(parser).map(Pair::second);
	}

	default IParser<Rope<T>> plus(String delimiter) {
		return plus(exact(delimiter));
	}

	default <U> IParser<Rope<T>> plus(IParser<U> delimiter) {
		return Parsers.join(this, delimiter);
	}

	default IParser<Rope<T>> plusUsing(IParser<T> delimiter) {
		return Parsers.join2(this, delimiter);
	}

	default IParser<Rope<T>> star(String delimiter) {
		return star(exact(delimiter));
	}

	default <U> IParser<Rope<T>> star(IParser<U> delimiter) {
		return Parsers.join(this, delimiter).optional().map(result -> result.orElseGet(Rope::empty));
	}

	default IParser<T> end() {
		return thenSilently(new EndParser());
	}

	default <U> IParser<U> and(IParser<U> other) {
		return new AndParser<>(this, other);
	}

	default IParser<Void> not() {
		return new NotParser<>(this);
	}

	default <U> IResultIterator<U> descend(IParser<U> parser, String text, int position, ParsingContext context) {
		return parser.parse(text, position, context);
	}

	default void logStackUp(String text, int position) {
		CharSequence excerptLeft = text.subSequence(Math.max(0, position - 10), position);

		CharSequence excerptRight = text.subSequence(position, Math.min(position + 10, text.length()));
		System.out.println(getClass().getSimpleName() + "[" + position + ", '" + excerptLeft + "|" + excerptRight
				+ "']: ]: stack up");
	}

	default void logStackDown(String text, int position) {
		System.out.println(getClass().getSimpleName() + "[" + position + "]: stack down");
	}

	public interface IResultIterator<T> {

		boolean hasResult();

		int getNewPosition();

		T getResult();

		DebugTree getDebugTree();

		ParsingContext getContext();

		void next();

	}

	public record DebugTree(String label, Object result, String text, int begin, int end, List<DebugTree> children) {

		public CharSequence coveredText() {
			return text.subSequence(begin, end);
		}

		public DebugTree withResult(Object result) {
			return new DebugTree(label, result, text, begin, end, children);
		}

		@Override
		public String toString() {
			CharSequence truncatedText = coveredText().length() <= 20 ? coveredText()
					: coveredText().subSequence(0, 10) + "..."
							+ coveredText().subSequence(coveredText().length() - 7, coveredText().length());
			return "DebugTree " + label + "[result=" + result + ", text=" + truncatedText + "]";
		}

	}

}
