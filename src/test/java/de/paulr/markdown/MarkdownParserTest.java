package de.paulr.markdown;

import static de.paulr.parser.Parsers.exact;
import static de.paulr.parser.Parsers.regex;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import de.paulr.knowledge.KnowledgeBase;
import de.paulr.knowledge.Page;
import de.paulr.parser.Parsers;

public class MarkdownParserTest {

	@Test
	public void heading() {
		var it = MarkdownParser.heading.parse("# Hello World\n", 0);
		assertTrue(it.hasResult());
		assertThat(it.getResult(), equalTo(new MdHeadingElement("Hello World")));
	}

	@Test
	public void inlineText() {
		var it = MarkdownParser.inlineText.parse("H\\ello\n", 0);
		assertTrue(it.hasResult());
		assertThat(it.getResult(), is("Hello"));
	}

	@Test
	public void join() {
		var it = Parsers.join(regex("a|b"), exact(", ")).parse("a, b, c", 0);
		assertTrue(it.hasResult());
		assertThat(it.getResult().toList(), contains("a", "b"));
	}

	@Test
	public void listItemHead() {
		var it = MarkdownParser.listItemHead.parse("- a", 0);
		assertTrue(it.hasResult());
	}

	@Test
	public void listItem() {
		var it = MarkdownParser.listItem.parse("""
			- a
			    - aa
			""", 0);

		assertTrue(it.hasResult());
		assertThat(it.getResult(),
			is(new MdListItem("a", List.of(new MdListItem("aa", List.of())))));
	}

	@Test
	public void blocks() {
		var it = MarkdownParser.blocks.parse("""
			# A heading

			- a
			  - aa
			- b
			""", 0);
		assertTrue(it.hasResult());
		assertThat(it.getResult(),
			contains(new MdHeadingElement("A heading"),
				new MdListElement(
					List.of(new MdListItem("a", List.of(new MdListItem("aa", List.of()))),
						new MdListItem("b", List.of())))));
	}

	@Test
	public void indented1() {
		var it = MarkdownParser.listItem.indented("> ").parse("- hi", 0);

		assertFalse(it.hasResult());
	}

	@Test
	public void indented2() {
		var it = MarkdownParser.listItem.indented("> ").parse("> - hi\n", 0);

		assertTrue(it.hasResult());
		assertThat(it.getResult(), is(new MdListItem("hi", List.of())));
	}

	@Test
	public void escapedNewline() {
		var it = MarkdownParser.listItemHead.parse("""
			- \

			""", 0);

		assertTrue(it.hasResult());
	}

	@Test
	public void page() {
		KnowledgeBase kb = new KnowledgeBase(Path.of("/home/paul/Obsidian Vault"));
		Page page = Page.ofDirAndName(kb.getPageDir(), "Test.md");

		System.out.println(page.getAst());

		assertTrue(page.getAst().isPresent());
	}

}
