package de.paulr.knowledge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.paulr.amalgam.AmalgamAnnotation;
import de.paulr.amalgam.Feat;
import de.paulr.markdown.MdInlineExpression;
import de.paulr.markdown.MdTextElement;

@AmalgamAnnotation
public class KnowledgeBaseAmalgam {

	public static final String basePath = "basePath";

	public static final String pageDir = "pageDir";
	public static final String pagePaths = "pagePaths";
	public static final String pages = "pages";
	public static final String pagesByTitle = "pagesByTitle";
	private static final String contextualizedPages = "contextualizedPages";

	private static final String attachmentDir = "attachmentDir";
	private static final String attachmentPaths = "attachmentPaths";

	public static final String parseabilitySummary = "parseabilitySummary";

	public static final String staticPageDir = "staticPageDir";
	public static final String publishedPagesByPath = "publishedPagesByPath";

	public static final String publicQuotes = "publicQuotes";

	@Feat(pageDir)
	public static Path pageDir(@Feat(basePath) Path basePath) {
		return basePath.resolve("pages");
	}

	@Feat(pagePaths)
	public static List<Path> pagePaths(@Feat(pageDir) Path pageDir) {
		try {
			return Files.list(pageDir) //
				.filter(KnowledgeBaseAmalgam::isPathToMarkdownFile) //
				.toList();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Feat(pages)
	public static List<Page> pages(@Feat(pagePaths) List<Path> pagePaths) {
		return pagePaths.stream().map(Page::ofFilePath).toList();
	}

	@Feat(pagesByTitle)
	public static Map<String, Page> pagesByTitle(@Feat(pages) List<Page> pages) {
		return pages.stream().collect(Collectors.toMap(Page::getTitle, p -> p));
	}

	@Feat(contextualizedPages)
	public static Map<String, ContextualizedPage> contextualizedPages(
		@Feat(pages) List<Page> pages) {
		Map<String, Set<String>> incomingReferences = new HashMap<>();

		for (var page : pages) {
			for (var reference : page.getReferences()) {
				var set = incomingReferences.computeIfAbsent(reference,
					__ -> new LinkedHashSet<>());
				set.add(page.getTitle());
			}
		}

		return pages.stream() //
			.map(page -> new ContextualizedPage(page,
				incomingReferences.computeIfAbsent(page.getTitle(), __ -> new LinkedHashSet<>()))) //
			.collect(Collectors.toMap(ContextualizedPage::getTitle, page -> page));
	}

	@Feat(attachmentDir)
	public static Path attachmentDir(@Feat(basePath) Path basePath) {
		return basePath.resolve("attachments");
	}

	@Feat(attachmentPaths)
	public static Map<String, Path> attachmentPaths(@Feat(attachmentDir) Path attachmentDir) {
		try {
			return Files.list(attachmentDir) //
				.filter(path -> path.toFile().isFile()) //
				.collect(Collectors.toMap(path -> path.getFileName().toString(), path -> path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Feat(parseabilitySummary)
	public static String parseabilitySummary(@Feat(pages) List<Page> pages) {
		long totalPages = pages.size();
		long parseablePages = pages.stream().filter(page -> page.getAst().isPresent()).count();
		String pageExample = pages.stream().filter(page -> page.getAst().isEmpty()).findFirst()
			.map(Page::getTitle).orElse(null);
		return String.format(
			"%d out of %d pages are parseable.\n" + "For example, %s is not parseable.",
			parseablePages, totalPages, pageExample);
	}

	@Feat(publishedPagesByPath)
	public static Map<Path, String> publishedPagesByPath(@Feat(pages) List<Page> pages) {
		Map<Path, String> pagesByPath = new LinkedHashMap<>();
		for (var page : pages) {
			if (!page.getIsPublished()) {
				continue;
			}

			String webName = page.getTitle().replaceAll("\\W", "_").toLowerCase() + ".html";
			Path relativeWebPath = Path.of(webName);
			pagesByPath.put(relativeWebPath, page.getTitle());
		}
		return pagesByPath;
	}

	@Feat(publicQuotes)
	public static List<String> quotes(@Feat(pages) List<Page> pages) {
		return pages.stream().flatMap(page -> page.getTextElements().stream())
			.filter(KnowledgeBaseAmalgam::isPublicQuote)
			.map(KnowledgeBaseAmalgam::quoteTextElementToString) //
			.toList();
	}

	private static boolean isPublicQuote(MdTextElement textElement) {
		List<String> relevantReferences = new ArrayList<>();
		textElement.collectReferences(ref -> {
			if (ref.equals("PubZitat")) {
				relevantReferences.add(ref);
			}
		});
		return !relevantReferences.isEmpty();
	}

	private static String quoteTextElementToString(MdTextElement textElement) {
		return textElement.getExpressions().stream() //
			.map(MdInlineExpression::getTextualContent) //
			.filter(Predicate.not(List.of("Zitat", "PubZitat")::contains)) //
			.collect(Collectors.joining());
	}

	private static boolean isPathToMarkdownFile(Path path) {
		return path.getFileName().toString().endsWith(".md");
	}

}
