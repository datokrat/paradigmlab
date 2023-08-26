// AUTO-GENERATED AMALGAMATED CLASS
package de.paulr.knowledge;

import java.util.Optional;

public class Page {
private java.nio.file.Path filePath;
private Optional<java.lang.String> markdown = Optional.empty();
private Optional<java.lang.String> fileName = Optional.empty();
private Optional<java.lang.String> title = Optional.empty();
private Optional<java.util.Optional<de.paulr.markdown.MarkdownPage>> ast = Optional.empty();
private Optional<java.util.Set<java.lang.String>> references = Optional.empty();
private Optional<java.util.Map<java.lang.String, java.util.List<java.lang.String>>> metadataEntries = Optional.empty();
private Optional<java.util.Optional<de.paulr.markdown.MarkdownPage>> astWithoutMetadata = Optional.empty();
private java.util.Optional<Boolean> isPublished = java.util.Optional.empty();
private Optional<java.util.Optional<java.time.LocalDate>> createdAt = Optional.empty();
private Optional<java.util.List<de.paulr.markdown.MdTextElement>> textElements = Optional.empty();
public Page(java.nio.file.Path filePath) {
this.filePath = filePath;
}
@java.lang.SuppressWarnings("unused")
private Page() {}
@java.lang.SuppressWarnings("unused")
public static Page ofFilePath(java.nio.file.Path featfilePath) {
var obj = new Page();
var tmpfilePath = featfilePath;
obj.filePath = tmpfilePath;
return obj;
}
@java.lang.SuppressWarnings("unused")
public static Page ofDirAndName(java.nio.file.Path featfileDir, java.lang.String featfileName) {
var obj = new Page();
var tmpfileDir = featfileDir;
var tmpfileName = featfileName;
obj.fileName = Optional.of(tmpfileName);
var tmpfilePath = de.paulr.knowledge.MarkdownPageAmalgam.filePathByDirAndName(tmpfileDir, tmpfileName);
obj.filePath = tmpfilePath;
return obj;
}
public java.nio.file.Path getFilePath() {
return filePath;
}
public java.lang.String getMarkdown() {
markdown = Optional.of(markdown.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.markdown(getFilePath())));
return markdown.get();
}
public java.lang.String getFileName() {
fileName = Optional.of(fileName.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.fileName(getFilePath())));
return fileName.get();
}
public java.lang.String getTitle() {
title = Optional.of(title.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.title(getFileName())));
return title.get();
}
public java.util.Optional<de.paulr.markdown.MarkdownPage> getAst() {
ast = Optional.of(ast.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.ast(getMarkdown(), getFileName())));
return ast.get();
}
public java.util.Set<java.lang.String> getReferences() {
references = Optional.of(references.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.references(getAst())));
return references.get();
}
public java.util.Map<java.lang.String, java.util.List<java.lang.String>> getMetadataEntries() {
metadataEntries = Optional.of(metadataEntries.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.metadataEntries(getAst())));
return metadataEntries.get();
}
public java.util.Optional<de.paulr.markdown.MarkdownPage> getAstWithoutMetadata() {
astWithoutMetadata = Optional.of(astWithoutMetadata.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.astWithoutMetadata(getAst())));
return astWithoutMetadata.get();
}
public boolean getIsPublished() {
isPublished = java.util.Optional.of(isPublished.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.isPublished(getMetadataEntries())));
return isPublished.get();
}
public java.util.Optional<java.time.LocalDate> getCreatedAt() {
createdAt = Optional.of(createdAt.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.createdAt(getMetadataEntries())));
return createdAt.get();
}
public java.util.List<de.paulr.markdown.MdTextElement> getTextElements() {
textElements = Optional.of(textElements.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.textElements(getAst())));
return textElements.get();
}
}
