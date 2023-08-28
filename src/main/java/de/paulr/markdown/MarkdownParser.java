package de.paulr.markdown;

import static de.paulr.parser.Parsers.align;
import static de.paulr.parser.Parsers.exact;
import static de.paulr.parser.Parsers.lockInAlternatives;
import static de.paulr.parser.Parsers.regex;

import java.util.List;
import java.util.Optional;

import de.paulr.markdown.MdLinkExpression.LinkPlacement;
import de.paulr.markdown.MdLinkExpression.LinkType;
import de.paulr.parser.AlignParser;
import de.paulr.parser.IParser;
import de.paulr.parser.ImitationParser;
import de.paulr.parser.Parsers;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

public class MarkdownParser {

	public static IParser<String> nonBackslashNewline = regex("[^\\n\\\\]");

	public static IParser<String> leftDoubleBracket = exact("[[");
	public static IParser<String> rightDoubleBracket = exact("]]");

	public static IParser<String> newline = exact("\n");

	public static IParser<String> escapedNewline = exact("\\").silentlyThen(newline)
		.thenSilently(exact(" ".repeat(2))).thenSilently(new AlignParser());

	public static IParser<String> escapedCharacter = exact("\\").silentlyThen(regex("."));

	public static IParser<String> character = leftDoubleBracket.not()
		.and(nonBackslashNewline.or(escapedNewline).or(escapedCharacter));

	public static IParser<String> innerLinkCharacter = rightDoubleBracket.not().and(character);
	public static IParser<String> link = leftDoubleBracket.silentlyThen(innerLinkCharacter.plus())
		.thenSilently(rightDoubleBracket).map(chars -> String.join("", chars));

	public static IParser<String> weblinkCaption = exact("[")
		.silentlyThen(exact("]").not().and(regex(".")).plus().map(chars -> String.join("", chars)))
		.thenSilently(exact("]"));
	public static IParser<String> weblinkTarget = exact("(")
		.silentlyThen(exact(")").not().and(regex(".")).plus().map(chars -> String.join("", chars)))
		.thenSilently(exact(")"));
	public static IParser<String> internalLinkTarget = exact("[")
		.silentlyThen(exact("]").not().and(regex(".")).plus().map(chars -> String.join("", chars)))
		.thenSilently(exact("]"));
	public static IParser<MdInlineExpression> weblinkExpression = weblinkCaption.then(weblinkTarget)
		.map(Pair.fn((caption, target) -> new MdLinkExpression(Optional.of(caption), target,
			LinkType.Web, false, LinkPlacement.Inline)));
	public static IParser<MdInlineExpression> linkWithCaption = weblinkCaption
		.then(lockInAlternatives("(", weblinkTarget.annotate(LinkType.Web)) //
			.or("[", internalLinkTarget.annotate(LinkType.Internal)) //
			.done())
		.map(Pair.fn((caption, target) -> new MdLinkExpression(Optional.of(caption), target.first(),
			target.second(), false, LinkPlacement.Inline)));

	public static IParser<String> inlineText = exact("$").not() //
		.and(exact("[").not()) //
		.and(exact("![").not()) //
		.and(exact("(float-right)![").not()) //
		.and(exact("(full-width)![").not()) //
		.and(character)
		.plus(newline.thenSilently(align())
			.thenSilently((exact(" ".repeat(4)).optional().then(exact("- ")).not())).optional()) //
		.map(chars -> String.join("", chars));

	public static IParser<String> inlineMath = exact("$")
		.silentlyThen(exact("$").not().and(regex(".")).plus()).thenSilently(exact("$"))
		.map(chars -> String.join("", chars));

	public static IParser<MdInlineExpression> inlineTextExpression = inlineText
		.map(MdTextExpression::new);
	public static IParser<MdInlineExpression> inlineLinkExpression = link
		.map(link -> new MdLinkExpression(Optional.empty(), link, LinkType.Internal, false,
			LinkPlacement.Inline));
	public static IParser<MdInlineExpression> imageLinkExpression = exact("!").silentlyThen(link)
		.map(link -> new MdLinkExpression(Optional.empty(), link, LinkType.Internal, true,
			LinkPlacement.Inline));
	public static IParser<MdInlineExpression> floatingImageLinkExpression = exact("(float-right)!")
		.silentlyThen(link).map(link -> new MdLinkExpression(Optional.empty(), link,
			LinkType.Internal, true, LinkPlacement.FloatRight));
	public static IParser<MdInlineExpression> fullWidthImageLinkExpression = exact("(full-width)!")
		.silentlyThen(link).map(link -> new MdLinkExpression(Optional.empty(), link,
			LinkType.Internal, true, LinkPlacement.FullWidth));
	public static IParser<MdInlineExpression> inlineMathExpression = inlineMath
		.map(MdMathExpression::new);
	public static IParser<MdInlineExpression> inlineExpression = imageLinkExpression //
		.or(inlineLinkExpression) //
		.or(floatingImageLinkExpression) //
		.or(fullWidthImageLinkExpression) //
		.or(linkWithCaption) //
		.or(inlineMathExpression) //
		.or(inlineTextExpression);

	public static IParser<MdTextElement> textElement = inlineExpression.star().map(Rope::toList)
		.map(MdTextElement::new);

	public static IParser<MdTextElement> textBlock = exact("- ").not().and(exact("# ").not())
		.and(textElement);

	public static IParser<MdHeadingElement> heading = exact("# ").silentlyThen(inlineText)
		.map(MdHeadingElement::new);

	public static IParser<MdTextElement> listItemHead = exact("- ").silentlyThen(textElement);

	private static ImitationParser<MdListItem> iListItem = new ImitationParser<>();
	public static IParser<MdListItem> listItem = Parsers
		.align(listItemHead
			.then(newline.silentlyThen(iListItem).star().indented(" ".repeat(4)).map(Rope::toList)))
		.map(p -> new MdListItem(p.first(), p.second()));
	static {
		iListItem.imitate(listItem);
	}

	public static IParser<MdListElement> list = listItem.plus(newline)
		.map(rope -> new MdListElement(rope.toList()));

	// public static IParser<List<String>> textBlock = pp(join(inline, newline),
	// Rope::toList);

	public static IParser<TopLevelMdElement> block = heading.<TopLevelMdElement>map(x -> x) //
		.or(list.map(x -> x)) //
		.or(textBlock.map(x -> x));

	public static IParser<Void> blockSeparatingNewlines = newline.then(newline.plus()).mapToNull();

	public static IParser<List<TopLevelMdElement>> blocks = newline.star()
		.silentlyThen(block.star(blockSeparatingNewlines)).thenSilently(newline.star())
		.map(Rope::toList);

	public static IParser<MarkdownPage> page = blocks.end().map(MarkdownPage::new);

}
