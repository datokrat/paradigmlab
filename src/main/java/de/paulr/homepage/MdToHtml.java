package de.paulr.homepage;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;

import de.paulr.markdown.MarkdownPage;
import de.paulr.markdown.MdHeadingElement;
import de.paulr.markdown.MdInlineExpression;
import de.paulr.markdown.MdLinkExpression;
import de.paulr.markdown.MdLinkExpression.LinkType;
import de.paulr.markdown.MdListElement;
import de.paulr.markdown.MdListItem;
import de.paulr.markdown.MdMathExpression;
import de.paulr.markdown.MdTextElement;
import de.paulr.markdown.MdTextExpression;
import de.paulr.markdown.TopLevelMdElement;
import de.paulr.util.Rope;

public class MdToHtml {

	private Function<String, String> pathByLink;
	private Function<String, String> captionByLink;

	private BatchLatexToHtml latexToHtml;

	public MdToHtml(Function<String, String> pathByLink, Function<String, String> captionByLink) {
		this.pathByLink = pathByLink;
		this.captionByLink = captionByLink;
	}

	public String renderPageToString(MarkdownPage page) {
		latexToHtml = new BatchLatexToHtml();
		latexToHtml.start();
		StringBuilder sb = new StringBuilder();
		renderPage(page).forEach(sb::append);
		String result = sb.toString();
		latexToHtml.stop();
		return result;
	}

	public Rope<String> renderPage(MarkdownPage page) {
		Rope<String> result = Rope.empty();
		for (var block : page.getElements()) {
			result = result.concat(renderBlock(block).addRight("\n\n"));
		}
		return result;
	}

	public Rope<String> renderBlock(TopLevelMdElement block) {
		if (block instanceof MdListElement listElement) {
			return renderListElement(listElement);
		}
		if (block instanceof MdTextElement textElement) {
			return Rope.of("<p>", renderTextElement(textElement), "</p>");
		}
		if (block instanceof MdHeadingElement headingElement) {
			return Rope.of("<h2>", StringEscapeUtils.escapeHtml4(headingElement.getText()),
				"</h2>");
		}
		throw new IllegalStateException("unknown block type");
	}

	public Rope<String> renderListElement(MdListElement list) {
		Rope<String> result = Rope.singleton("<ul>\n");
		for (var item : list.getItems()) {
			result = result.concat(renderListItem(item));
		}
		result = result.addRight("</ul>");
		return result;
	}

	public Rope<String> renderListItem(MdListItem item) {
		Rope<String> result = Rope.of("<li>", renderTextElement(item.getText()), "\n<ul>");
		for (var child : item.getChildren()) {
			result = result.concat(renderListItem(child));
		}
		result = result.addRight("</ul></li>");
		return result;
	}

	public String renderTextElement(MdTextElement textElement) {
		return textElement.getExpressions().stream().map(this::renderExpression)
			.collect(Collectors.joining());
	}

	public String renderExpression(MdInlineExpression expression) {
		return switch (expression.getType()) {
		case Link -> renderLink((MdLinkExpression) expression);
		case Text -> renderInlineText((MdTextExpression) expression);
		case Math -> latexToHtml.render(((MdMathExpression) expression).getLatex());
		};
	}

	public String renderLink(MdLinkExpression linkExpression) {
		String target = linkExpression.getLinkType() == LinkType.Internal //
			? pathByLink.apply(linkExpression.getTarget()) //
			: linkExpression.getTarget();

		String caption;
		if (linkExpression.getLinkType() == LinkType.Internal) {
			caption = linkExpression.getCaption()
				.orElse(captionByLink.apply(linkExpression.getTarget()));
		} else {
			caption = linkExpression.getCaption().orElse(linkExpression.getTarget());
		}

		if (!linkExpression.isPreview()) {
			return "<a href=\"" + escapeHtmlAttribute(target) + "\">" + escapeHtmlText(caption)
				+ "</a>";
		}

		String prefix = switch (linkExpression.getPlacement()) {
		case Inline -> "<img class=\"midsized\" ";
		case FloatRight -> "<img class=\"float-right\" ";
		case FullWidth -> "<img class=\"full-width\" ";
		};

		return prefix + "src=\"" + escapeHtmlAttribute(target) + "\" alt=\""
			+ escapeHtmlAttribute(caption) + "\">";
	}

	public String renderInlineText(MdTextExpression textExpression) {
		return "<span>" + escapeHtmlText(textExpression.getText()) + "<span>";
	}

	private String escapeHtmlText(String text) {
		return StringEscapeUtils.escapeHtml4(text);
	}

	private String escapeHtmlAttribute(String value) {
		return StringEscapeUtils.escapeHtml4(value);
	}

}
