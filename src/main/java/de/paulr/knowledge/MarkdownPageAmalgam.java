package de.paulr.knowledge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import de.paulr.amalgam.AmalgamAnnotation;
import de.paulr.amalgam.Feat;
import de.paulr.amalgam.Tags;
import de.paulr.markdown.MarkdownPage;
import de.paulr.markdown.MarkdownParser;
import de.paulr.markdown.MdListElement;
import de.paulr.markdown.MdListItem;
import de.paulr.markdown.MdTextElement;
import de.paulr.markdown.MdTextExpression;
import de.paulr.markdown.TopLevelMdElement;

@AmalgamAnnotation
public class MarkdownPageAmalgam {

	private static final String markdown = "markdown";
	private static final String filePath = "filePath";
	private static final String fileName = "fileName";
	private static final String fileDir = "fileDir";
	private static final String title = "title";
	private static final String ast = "ast";
	private static final String references = "references";
	private static final String metadataEntries = "metadataEntries";
	private static final String astWithoutMetadata = "astWithoutMetadata";

	@Feat(markdown)
	public static String markdown(@Feat(filePath) Path filePath) {
		try {
			return Files.readString(filePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Feat(fileName)
	public static String fileName(@Feat(filePath) Path filePath) {
		return filePath.getFileName().toString();
	}

	@Feat(filePath)
	public static Path filePathByDirAndName(@Feat(fileDir) Path fileDir,
		@Feat("fileName") String fileName) {
		return fileDir.resolve(fileName);
	}

	@Feat(title)
	public static String title(@Feat(fileName) String fileName) {
		if (!fileName.endsWith(".md")) {
			throw new RuntimeException("fileName does not end with .md");
		}
		return fileName.substring(0, fileName.length() - 3);
	}

	@Feat(ast)
	public static Optional<MarkdownPage> ast(@Feat(markdown) String markdown,
		@Feat(fileName) String fileName) {
		var it = MarkdownParser.page.parse(markdown, 0);
		if (it.hasResult()) {
			return Optional.of(it.getResult());
		} else {
			System.out.println("Parsing of " + fileName + " failed!");
			return Optional.empty();
		}
	}

	@Feat(references)
	public static Set<String> references(@Feat(ast) Optional<MarkdownPage> ast) {
		if (ast.isEmpty()) {
			return Set.of();
		}

		Set<String> references = new LinkedHashSet<>();
		ast.get().collectReferences(references::add);
		return references;
	}

	@Feat(metadataEntries)
	public static Map<String, List<String>> metadataEntries(@Feat(ast) Optional<MarkdownPage> ast) {
		if (ast.isEmpty() || ast.get().getElements().isEmpty()) {
			return Map.of();
		}

		var firstBlock = ast.get().getElements().get(0);
		if (!(firstBlock instanceof MdListElement firstBlockList)) {
			return Map.of();
		}

		if (firstBlockList.getItems().isEmpty() || !firstBlockList.getItems().get(0).getText()
			.getExpressions().equals(List.of(new MdTextExpression("<meta>")))) {
			return Map.of();
		}

		Map<String, List<String>> metadata = new HashMap<>();
		for (var listItem : firstBlockList.getItems().get(0).getChildren()) {
			Optional<String> key = getTextExpression(listItem);
			if (key.isEmpty()) {
				continue;
			}
			var values = listItem.getChildren().stream() //
				.flatMap(child -> getTextExpression(child).stream()) //
				.toList();
			metadata.put(key.get(), values);
		}

		return metadata;
	}

	@Feat(astWithoutMetadata)
	public static Optional<MarkdownPage> astWithoutMetadata(@Feat(ast) Optional<MarkdownPage> ast) {
		if (ast.isEmpty() || ast.get().getElements().isEmpty()) {
			return ast;
		}

		var blocks = ast.get().getElements();

		var firstBlock = blocks.get(0);
		if (!(firstBlock instanceof MdListElement firstBlockList)) {
			return ast;
		}

		if (firstBlockList.getItems().isEmpty() || !firstBlockList.getItems().get(0).getText()
			.getExpressions().equals(List.of(new MdTextExpression("<meta>")))) {
			return ast;
		}

		List<TopLevelMdElement> blocksWithoutMetadata = blocks.subList(1, blocks.size());
		return Optional.of(new MarkdownPage(blocksWithoutMetadata));
	}

	@Feat("isPublished")
	public static boolean isPublished(
		@Feat("metadataEntries") Map<String, List<String>> metadataEntries) {
		return metadataEntries.containsKey("webpublish");
	}

	@Feat("createdAt")
	public static Optional<LocalDate> createdAt(
		@Feat("metadataEntries") Map<String, List<String>> metadataEntries) {
		final String createdAt = "created at";

		if (!metadataEntries.containsKey(createdAt) || metadataEntries.get(createdAt).size() != 1) {
			return Optional.empty();
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
		String value = metadataEntries.get(createdAt).get(0);
		try {
			return Optional.of(LocalDate.parse(value, formatter));
		} catch (DateTimeParseException e) {
			System.out.println("invalid date format in created-at metadata: " + value);
			return Optional.empty();
		}
	}

	@Feat("textElements")
	public static List<MdTextElement> textElements(@Feat("ast") Optional<MarkdownPage> ast) {
		if (ast.isEmpty()) {
			return List.of();
		}

		List<MdTextElement> elements = new ArrayList<>();
		forEachTextElement(ast.get(), elements::add);
		return elements;
	}

	@Feat("journalDate")
	@Tags("journal")
	public static LocalDate journalDate(@Feat("title") String title) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
		return LocalDate.parse(title, formatter);
	}

	@Feat("fileName")
	@Tags("journal")
	public static String fileNameFromData(@Feat("journalDate") LocalDate date) {
		return date.format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + ".md";
	}

	private static void forEachTextElement(MarkdownPage page, Consumer<MdTextElement> consumer) {
		page.getElements()
			.forEach(topLevelElement -> forEachTextElement(topLevelElement, consumer));
	}

	private static void forEachTextElement(TopLevelMdElement topLevelElement,
		Consumer<MdTextElement> consumer) {
		if (topLevelElement instanceof MdTextElement textElement) {
			consumer.accept(textElement);
		} else if (topLevelElement instanceof MdListElement listElement) {
			listElement.getItems().forEach(item -> forEachTextElement(item, consumer));
		}
	}

	private static void forEachTextElement(MdListItem listItem, Consumer<MdTextElement> consumer) {
		consumer.accept(listItem.getText());
		listItem.getChildren().forEach(child -> forEachTextElement(child, consumer));
	}

	private static Optional<String> getTextExpression(MdListItem listItem) {
		if (listItem.getText().getExpressions().size() != 1) {
			return Optional.empty();
		}
		var expr = listItem.getText().getExpressions().get(0);
		if (!(expr instanceof MdTextExpression textExpr)) {
			return Optional.empty();
		}

		return Optional.of(textExpr.getText());
	}

}
