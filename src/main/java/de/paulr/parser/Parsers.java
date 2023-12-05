package de.paulr.parser;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

import de.paulr.parser.LockInAlternativeParser.LockInAlternativeParserBuilder;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

public abstract class Parsers {

	public static final IParser<Void> EPSILON = Parsers.exact("").mapToNull();

	public static <T> IParser<T> indented(String prefix, IParser<T> parser) {
		return new IndentParser(prefix).then(parser).then(new DedentParser())
			.map(x -> x.first().second());
	}

	public static IParser<String> exact(String s) {
		return new StringParser(s);
	}

	public static IParser<String> regex(String regex) {
		return regex(Pattern.compile(regex));
	}

	public static IParser<String> regex(Pattern regex) {
		return new RegexParser(regex);
	}

	public static <T, U> IParser<Pair<T, U>> pair(IParser<T> first, IParser<U> second) {
		return new SequenceParser<T, U>(first, second);
	}

	public static <T> IParser<Rope<T>> seq(List<IParser<T>> parsers) {
		return seq(0, parsers.size(), parsers);
	}

	private static <T> IParser<Rope<T>> seq(int begin, int end, List<IParser<T>> parsers) {
		if (begin >= end) {
			return exact("").mapTo(Rope.empty());
		} else if (begin == end - 1) {
			return parsers.get(begin).map(Rope::singleton);
		} else {
			return seq(begin, end - 1, parsers).then(parsers.get(end - 1)).map(Rope::addRight);
		}
	}

	public static <T, U> IParser<U> pp(IParser<T> parser, Function<T, U> fn) {
		return new PostProcessingParser<T, U>(parser, fn);
	}

	public static <T> IParser<T> fstAlt(List<IParser<T>> alternatives) {
		return new AlternativeParser<T>(alternatives, true);
	}

	public static <T> IParser<T> alts(List<IParser<T>> alternatives) {
		return new AlternativeParser<T>(alternatives, false);
	}

	public static <T> IParser<Rope<T>> flatStar(IParser<Rope<T>> parser) {
		return new StarParser<T>(parser);
	}

	public static <T> IParser<Rope<T>> star(IParser<T> parser) {
		return new StarParser<T>(parser.map(Rope::singleton));
	}

	public static <T> IParser<Rope<T>> starDeprecated(IParser<T> parser) {
		ImitationParser<Rope<T>> iStarParser = new ImitationParser<>();
		IParser<Rope<T>> starParser = fstAlt(
			List.of(pp(pair(parser, iStarParser), pair -> Rope.addLeft(pair)),
				pp(exact(""), __ -> Rope.empty())));
		iStarParser.imitate(starParser);
		return starParser;
	}

	public static <T, U> IParser<Rope<T>> join(IParser<T> parser, IParser<U> delimiter) {
		return pp(pair(parser, star(pp(pair(delimiter, parser), Pair::second))), Rope::addLeft);
	}

	public static <T> IParser<Rope<T>> join2(IParser<T> parser, IParser<T> delimiter) {
		return pp(pair(parser, flatStar(pp(pair(delimiter, parser), Rope::ofPair))), Rope::addLeft);
	}

	public static IParser<Void> align() {
		return new AlignParser();
	}

	public static <T> IParser<T> align(IParser<T> parser) {
		return new AlignParser().then(parser).map(Pair::second);
	}

	public static <T, U> LockInAlternativeParserBuilder<T> lockInAlternatives(
		IParser<U> lockInParser, IParser<T> followUpParser) {
		return new LockInAlternativeParserBuilder<T>().or(lockInParser, followUpParser);
	}

	public static <T> LockInAlternativeParserBuilder<T> lockInAlternatives(String lockInPrefix,
		IParser<T> followUpParser) {
		return new LockInAlternativeParserBuilder<T>().or(exact(lockInPrefix), followUpParser);
	}

	public static <T, U> LockInAlternativeParserBuilder<Function<T, U>> depLockInAlts(
		String lockInPrefix, IParser<Function<T, U>> followUpParser) {
		return lockInAlternatives(lockInPrefix, followUpParser);
	}

	public static <T> LockInAlternativeParserBuilder<T> lockInAlternatives() {
		return new LockInAlternativeParserBuilder<>();
	}

	public static <T, U, V> Function<T, Function<U, V>> curry(BiFunction<T, U, V> fn) {
		return x -> y -> fn.apply(x, y);
	}

	public static IParser<Long> longNumber() {
		return regex("-?\\d+").map(Long::parseLong);
	}

	public static IParser<Long> unsignedLongNumber() {
		return regex("\\d+").map(Long::parseLong);
	}

}
