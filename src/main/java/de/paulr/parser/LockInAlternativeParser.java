package de.paulr.parser;

import static de.paulr.parser.Parsers.alts;
import static de.paulr.parser.Parsers.exact;

import java.util.List;
import java.util.function.Function;

import de.paulr.parser.context.ParsingContext;

public class LockInAlternativeParser<T, U> implements IParser<T> {

	private IParser<U> lockInParser;
	private IParser<T> followUpParser;
	private IParser<T> fallbackParser;

	public LockInAlternativeParser(IParser<U> lockInParser, IParser<T> followUpParser,
		IParser<T> fallbackParser) {
		this.lockInParser = lockInParser;
		this.followUpParser = followUpParser;
		this.fallbackParser = fallbackParser;
	}

	@Override
	public IResultIterator<T> parse(String text, int position, ParsingContext context) {
		var lockInParserContext = lockInParser.parse(text, position, context);
		if (!lockInParserContext.hasResult()) {
			return fallbackParser.parse(text, position, context);
		} else {
			return followUpParser.parse(text, position, context);
		}
	}

	public static class LockInAlternativeParserBuilder<T> {

		private Function<IParser<T>, IParser<T>> orDefault = Function.identity();

		public <U> LockInAlternativeParserBuilder<T> or(String lockInPrefix,
			IParser<T> followUpParser) {
			return or(exact(lockInPrefix), followUpParser);
		}

		public <U> LockInAlternativeParserBuilder<T> or(IParser<U> lockInParser,
			IParser<T> followUpParser) {
			Function<IParser<T>, IParser<T>> f = orDefault;
			orDefault = fallbackParser -> f.apply(
				new LockInAlternativeParser<T, U>(lockInParser, followUpParser, fallbackParser));
			return this;
		}

		public LockInAlternativeParserBuilder<T> orElse(IParser<T> parser) {
			Function<IParser<T>, IParser<T>> f = orDefault;
			orDefault = fallbackParser -> f.apply(parser.or(fallbackParser));
			return this;
		}

		public IParser<T> done() {
			return orDefault.apply(alts(List.of()));
		}

	}

}
