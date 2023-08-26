package de.paulr.homepage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringSubstitutor;

import de.paulr.amalgam.AmalgamAnnotation;
import de.paulr.amalgam.Feat;
import de.paulr.knowledge.KnowledgeBase;
import de.paulr.knowledge.Page;
import de.paulr.markdown.MarkdownPage;
import de.paulr.markdown.MdHeadingElement;
import de.paulr.markdown.MdLinkExpression;
import de.paulr.markdown.MdLinkExpression.LinkPlacement;
import de.paulr.markdown.MdLinkExpression.LinkType;
import de.paulr.markdown.MdListElement;
import de.paulr.markdown.MdListItem;
import de.paulr.markdown.MdTextElement;
import de.paulr.markdown.TopLevelMdElement;

@AmalgamAnnotation
public class StaticSiteAmalgam {

	private static final String knowledgeBase = "knowledgeBase";
	private static final String htmlByPath = "textByPath";
	private static final String outputDir = "outputDir";
	private static final String inputDir = "inputDir";
	private static final String templatePath = "templatePath";
	private static final String template = "template";
	private static final String categoryPrefixes = "categoryPrefixes";
	private static final String categoryForTitleFn = "categoryForTitleFn";
	private static final String pathForTitleFn = "pathForTitleFn";
	private static final String titleWithoutPrefixFn = "titleWithoutPrefixFn";
	private static final String pathForLinkTargetFn = "pathForLinkTargetFn";
	private static final String defaultCaptionForLinkTargetFn = "defaultCaptionForLinkTargetFn";
	private static final String indexPage = "indexPage";
	private static final String indexHtml = "indexHtml";
	private static final String generator = "generator";

	@Feat(templatePath)
	public static Path templatePath(@Feat(inputDir) Path inputDir) {
		return inputDir.resolve("template.html");
	}

	@Feat(template)
	public static String template(@Feat(templatePath) Path templatePath) {
		try {
			return Files.readString(templatePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Feat(htmlByPath)
	public static Map<Path, String> htmlByPath(@Feat(knowledgeBase) KnowledgeBase knowledgeBase,
		@Feat(template) String template,
		@Feat(pathForLinkTargetFn) Function<String, Path> pathForLinkTargetFn,
		@Feat(defaultCaptionForLinkTargetFn) Function<String, String> defaultCaptionForLinkTargetFn,
		@Feat(indexHtml) String indexHtml) {
		Map<Path, String> map = new HashMap<>();

		System.out.println("Start rendering.");

		for (var page : knowledgeBase.getPages()) {
			if (!page.getIsPublished()) {
				continue;
			}

			System.out.println("Rendering page " + page.getTitle() + "...");

			map.put(pathForLinkTargetFn.apply(page.getTitle()), generateHtml(page, template,
				pathForLinkTargetFn, defaultCaptionForLinkTargetFn, knowledgeBase));
		}

		map.put(Path.of("index.html"), indexHtml);

		System.out.println("Done rendering.");

		return map;
	}

	@Feat(generator)
	public static Runnable generator(@Feat(htmlByPath) Map<Path, String> htmlByPath,
		@Feat(inputDir) Path inputDir, @Feat(outputDir) Path outputDir,
		@Feat(categoryPrefixes) Map<String, Path> categoryPrefixes,
		@Feat(knowledgeBase) KnowledgeBase knowledgeBase) {

		return () -> {
			try {
				if (outputDir.toAbsolutePath().toString().length() <= 15) {
					throw new RuntimeException("path is too short; danger of deleting too much");
				}
				try (Stream<Path> files = Files.walk(outputDir)) {
					if (files.count() >= 300) {
						throw new RuntimeException("more than 300 files would be deleted!");
					}
				}
				FileUtils.deleteDirectory(outputDir.toFile());
				Files.createDirectory(outputDir);
				Files.createDirectory(outputDir.resolve("attachments"));
				Files.createDirectory(outputDir.resolve("notes"));
				for (var path : categoryPrefixes.values()) {
					Files.createDirectory(outputDir.resolve("notes").resolve(path));
				}

				FileUtils.copyDirectory(inputDir.resolve("assets/styles").toFile(),
					outputDir.resolve("styles").toFile());
				FileUtils.copyDirectory(inputDir.resolve("assets/fonts").toFile(),
					outputDir.resolve("fonts").toFile());
				FileUtils.copyDirectory(inputDir.resolve("assets/lib").toFile(),
					outputDir.resolve("lib").toFile());

				for (var entry : htmlByPath.entrySet()) {
					var relativePath = entry.getKey();
					var html = entry.getValue();
					Files.writeString(outputDir.resolve(relativePath), html);
				}

				for (var entry : knowledgeBase.getAttachmentPaths().entrySet()) {
					var attachmentName = entry.getKey();
					var attachmentPath = entry.getValue();
					if (!attachmentName.startsWith("public-")) {
						continue;
					}
					Files.copy(attachmentPath,
						outputDir.resolve("attachments").resolve(attachmentName));
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Feat(categoryPrefixes)
	public static Map<String, Path> categoryPrefixes() {
		return Map.of("Buch_ ", Path.of("books"), "Fiktion_ ", Path.of("fiction"), "Math_ ",
			Path.of("math"));
	}

	@Feat(categoryForTitleFn)
	public static Function<String, Optional<String>> categoryForTitleFn(
		@Feat(categoryPrefixes) Map<String, Path> categoryPrefixes) {
		return title -> categoryPrefixes.keySet().stream() //
			.filter(title::startsWith) //
			.findFirst();
	}

	@Feat(titleWithoutPrefixFn)
	public static Function<String, String> titleWithoutPrefixFn(
		@Feat(categoryPrefixes) Map<String, Path> categoryPrefixes,
		@Feat(categoryForTitleFn) Function<String, Optional<String>> categoryForTitleFn) {
		return title -> categoryForTitleFn.apply(title) //
			.map(prefix -> title.substring(prefix.length())) //
			.orElse(title);
	}

	@Feat(pathForTitleFn)
	public static Function<String, Path> pathForTitleFn(
		@Feat(categoryPrefixes) Map<String, Path> categoryPrefixes,
		@Feat(categoryForTitleFn) Function<String, Optional<String>> categoryForTitleFn) {

		Path pagesDir = Path.of("notes");

		return title -> {
			Path path = categoryForTitleFn.apply(title)
				.map(prefix -> categoryPrefixes.get(prefix)
					.resolve(toFilename(title.substring(prefix.length())))) //
				.orElse(Path.of(toFilename(title)));

			return pagesDir.resolve(path);
		};
	}

	@Feat(pathForLinkTargetFn)
	public static Function<String, Path> pathForLinkTargetFn(
		@Feat(pathForTitleFn) Function<String, Path> pathForTitleFn) {
		final String attachmentPrefix = "../attachments/";
		return title -> {
			if (title.startsWith(attachmentPrefix)) {
				return Path.of("attachments", title.substring(attachmentPrefix.length()));
			} else {
				return pathForTitleFn.apply(title);
			}
		};
	}

	@Feat(defaultCaptionForLinkTargetFn)
	public static Function<String, String> defaultCaptionForLinkTargetFn(
		@Feat(titleWithoutPrefixFn) Function<String, String> titleWithoutPrefixFn) {
		final String attachmentPrefix = "../attachments/";
		return title -> {
			if (title.startsWith(attachmentPrefix)) {
				return title.substring(attachmentPrefix.length());
			} else {
				return titleWithoutPrefixFn.apply(title);
			}
		};
	}

	@Feat(indexPage)
	public static MarkdownPage indexPage(@Feat(knowledgeBase) KnowledgeBase knowledgeBase,
		@Feat(categoryForTitleFn) Function<String, Optional<String>> categoryForTitleFn,
		@Feat(categoryPrefixes) Map<String, Path> categoryPrefixes) {
		Map<Optional<String>, List<Page>> publishedPagesByCategory = knowledgeBase.getPages()
			.stream() //
			.filter(Page::getIsPublished) //
			.collect(Collectors.groupingBy(page -> categoryForTitleFn.apply(page.getTitle())));

		List<TopLevelMdElement> blocks = new ArrayList<>();
		for (var category : publishedPagesByCategory.keySet()) {
			TopLevelMdElement heading = new MdHeadingElement(
				categoryDescription(category, categoryPrefixes));
			TopLevelMdElement pageList = new MdListElement(
				publishedPagesByCategory.get(category).stream() //
					.map(page -> new MdListItem(
						new MdTextElement(List.of(new MdLinkExpression(Optional.empty(),
							page.getTitle(), LinkType.Internal, false, LinkPlacement.Inline))),
						List.of()))
					.toList());

			blocks.add(heading);
			blocks.add(pageList);
		}

		return new MarkdownPage(blocks);
	}

	private static String categoryDescription(Optional<String> prefix,
		Map<String, Path> categoryPrefixes) {
		if (prefix.isEmpty()) {
			return "(miscellaneous)/";
		}

		return categoryPrefixes.get(prefix.get()).toString() + "/";
	}

	@Feat(indexHtml)
	public static String indexHtml(@Feat(indexPage) MarkdownPage indexPage,
		@Feat(knowledgeBase) KnowledgeBase knowledgeBase,
		@Feat(pathForLinkTargetFn) Function<String, Path> pathForLinkTargetFn,
		@Feat(template) String template,
		@Feat(defaultCaptionForLinkTargetFn) Function<String, String> defaultCaptionForLinkTargetFn) {
		return generateHtml(indexPage, "Index of Pages", Optional.empty(), template,
			pathForLinkTargetFn, defaultCaptionForLinkTargetFn, knowledgeBase, Path.of("."));
	}

	private static String logophrase() {
		Random rand = new Random();
		StringBuilder sb = new StringBuilder();
		int length = rand.nextInt(5, 25);
		for (int i = 0; i < length; ++i) {
			int charIndex = rand.nextInt(97, 123);
			char randChar = (char) charIndex;
			sb.append(randChar);
		}
		return sb.toString();
	}

	private static String randQuote(KnowledgeBase knowledgeBase) {
		if (knowledgeBase.getPublicQuotes().isEmpty()) {
			return "";
		}
		return knowledgeBase.getPublicQuotes()
			.get(new Random().nextInt(knowledgeBase.getPublicQuotes().size()));
	}

	private static String toFilename(String title) {
		return title.replaceAll("ä", "ae").replaceAll("ö", "oe").replaceAll("ü", "ue")
			.replaceAll("ß", "ss").replaceAll("\\W", "_").toLowerCase() + ".html";
	}

	private static String generateHtml(MarkdownPage page, String pageTitle,
		Optional<LocalDate> createdAt, String template, Function<String, Path> pathForLinkTargetFn,
		Function<String, String> defaultCaptionForLinkTargetFn, KnowledgeBase knowledgeBase,
		Path thisDir) {
		Path rootPath = thisDir.relativize(Path.of(".")).resolve(".");
		Function<String, Path> relativePathForTitleFn = title -> thisDir
			.relativize(pathForLinkTargetFn.apply(title));
		String htmlContent = new MdToHtml(link -> relativePathForTitleFn.apply(link).toString(),
			defaultCaptionForLinkTargetFn).renderPageToString(page);
		String createdAtStr = createdAt.map(date -> "Created at " + date.toString()).orElse("");
		Map<String, String> substitutions = new HashMap<>();
		substitutions.put("title", defaultCaptionForLinkTargetFn.apply(pageTitle));
		substitutions.put("createdAt", createdAtStr);
		substitutions.put("content", htmlContent);
		substitutions.put("root", rootPath.toString());
		substitutions.put("quote", randQuote(knowledgeBase));
		substitutions.put("logophrase", logophrase());
		StringSubstitutor substitutor = new StringSubstitutor(substitutions, "{{", "}}");

		return substitutor.replace(template);
	}

	private static String generateHtml(Page page, String template,
		Function<String, Path> pathForLinkTargetFn,
		Function<String, String> defaultCaptionForLinkTargetFn, KnowledgeBase knowledgeBase) {
		Path thisDir = pathForLinkTargetFn.apply(page.getTitle()).getParent();
		return generateHtml(page.getAstWithoutMetadata().get(), page.getTitle(),
			page.getCreatedAt(), template, pathForLinkTargetFn, defaultCaptionForLinkTargetFn,
			knowledgeBase, thisDir);
	}

}
