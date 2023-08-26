package de.paulr.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.paulr.parser.context.ParsingContext;

public class RegexParser implements IParser<String> {

	private Pattern regex;

	public RegexParser(Pattern regex) {
		this.regex = regex;
	}

	@Override
	public IResultIterator<String> parse(String text, int position, ParsingContext context) {
		final Matcher matcher = regex.matcher(text);
		matcher.region(position, text.length());

		return new IResultIterator<String>() {

			boolean hasResult = matcher.lookingAt();

			@Override
			public void next() {
				hasResult = false;
			}

			@Override
			public boolean hasResult() {
				return hasResult;
			}

			@Override
			public String getResult() {
				return text.substring(matcher.start(), matcher.end());
			}

			@Override
			public int getNewPosition() {
				return matcher.end();
			}

			@Override
			public ParsingContext getContext() {
				return context;
			}
		};
	}

}
