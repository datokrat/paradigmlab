// AUTO-GENERATED AMALGAMATED CLASS
package de.paulr.knowledge;

import java.util.Optional;

public class JournalPage {
private java.lang.String markdown;
private java.lang.String title;
private Optional<java.time.LocalDate> journalDate = Optional.empty();
private Optional<java.lang.String> fileName = Optional.empty();
private Optional<java.util.Optional<de.paulr.markdown.MarkdownPage>> ast = Optional.empty();
private Optional<java.util.Set<java.lang.String>> references = Optional.empty();
private Optional<java.util.Map<java.lang.String, java.util.List<java.lang.String>>> metadataEntries = Optional.empty();
private Optional<java.util.Optional<de.paulr.markdown.MarkdownPage>> astWithoutMetadata = Optional.empty();
private java.util.Optional<Boolean> isPublished = java.util.Optional.empty();
private Optional<java.util.Optional<java.time.LocalDate>> createdAt = Optional.empty();
private Optional<java.util.List<de.paulr.markdown.MdTextElement>> textElements = Optional.empty();
public JournalPage(java.lang.String markdown, java.lang.String title) {
this.markdown = markdown;
this.title = title;
}
@java.lang.SuppressWarnings("unused")
private JournalPage() {}
@java.lang.SuppressWarnings("unused")
public static JournalPage ofFilePath(java.nio.file.Path featfilePath) {
var obj = new JournalPage();
var tmpfilePath = featfilePath;
var tmpmarkdown = de.paulr.knowledge.MarkdownPageAmalgam.markdown(tmpfilePath);
obj.markdown = tmpmarkdown;
var tmpfileName = de.paulr.knowledge.MarkdownPageAmalgam.fileName(tmpfilePath);
obj.fileName = Optional.of(tmpfileName);
var tmptitle = de.paulr.knowledge.MarkdownPageAmalgam.title(tmpfileName);
obj.title = tmptitle;
return obj;
}
@java.lang.SuppressWarnings("unused")
public static JournalPage ofDirAndDate(java.nio.file.Path featfileDir, java.time.LocalDate featjournalDate) {
var obj = new JournalPage();
var tmpfileDir = featfileDir;
var tmpjournalDate = featjournalDate;
obj.journalDate = Optional.of(tmpjournalDate);
var tmpfileName = de.paulr.knowledge.MarkdownPageAmalgam.fileNameFromData(tmpjournalDate);
obj.fileName = Optional.of(tmpfileName);
var tmpfilePath = de.paulr.knowledge.MarkdownPageAmalgam.filePathByDirAndName(tmpfileDir, tmpfileName);
var tmpmarkdown = de.paulr.knowledge.MarkdownPageAmalgam.markdown(tmpfilePath);
obj.markdown = tmpmarkdown;
var tmptitle = de.paulr.knowledge.MarkdownPageAmalgam.title(tmpfileName);
obj.title = tmptitle;
return obj;
}
public java.lang.String getMarkdown() {
return markdown;
}
public java.lang.String getTitle() {
return title;
}
public java.time.LocalDate getJournalDate() {
journalDate = Optional.of(journalDate.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.journalDate(getTitle())));
return journalDate.get();
}
public java.lang.String getFileName() {
fileName = Optional.of(fileName.orElseGet(() -> de.paulr.knowledge.MarkdownPageAmalgam.fileNameFromData(getJournalDate())));
return fileName.get();
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
