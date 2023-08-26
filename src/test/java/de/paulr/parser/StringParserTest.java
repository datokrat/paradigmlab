package de.paulr.parser;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringParserTest {

	@Test
	public void parsing_SHOULD_yieldOneResult() {
		StringParser parser = new StringParser("Hello");
		var it = parser.parse("Hello World", 0);
		assertTrue(it.hasResult());
		assertThat(it.getNewPosition(), equalTo(5));

		it.next();

		assertFalse(it.hasResult());
	}

	@Test
	public void parsing_SHOULD_fail() {
		StringParser parser = new StringParser("Hello");
		var it = parser.parse("Hello World", 1);
		assertFalse(it.hasResult());
	}

}
