package de.paulr.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SequenceParserTest {

	@Test
	public void parsing_SHOULD_yieldOneResult() {
		SequenceParser<String, String> parser = new SequenceParser<>(new StringParser("Hello "),
				new StringParser("World"));
		var it = parser.parse("Hello World", 0);

		assertTrue(it.hasResult());

		it.next();

		assertFalse(it.hasResult());
	}

	@Test
	public void parsing_SHOULD_fail() {
		SequenceParser<String, String> parser = new SequenceParser<>(new StringParser("Hello "),
				new StringParser("World"));
		var it = parser.parse("Hello World", 1);

		assertFalse(it.hasResult());
	}

}
