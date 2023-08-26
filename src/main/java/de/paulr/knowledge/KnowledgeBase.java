// AUTO-GENERATED AMALGAMATED CLASS
package de.paulr.knowledge;

import java.util.Optional;

public class KnowledgeBase {
private java.nio.file.Path basePath;
private Optional<java.nio.file.Path> pageDir = Optional.empty();
private Optional<java.util.List<java.nio.file.Path>> pagePaths = Optional.empty();
private Optional<java.util.List<de.paulr.knowledge.Page>> pages = Optional.empty();
private Optional<java.util.Map<java.lang.String, de.paulr.knowledge.Page>> pagesByTitle = Optional.empty();
private Optional<java.nio.file.Path> attachmentDir = Optional.empty();
private Optional<java.util.Map<java.lang.String, java.nio.file.Path>> attachmentPaths = Optional.empty();
private Optional<java.lang.String> parseabilitySummary = Optional.empty();
private Optional<java.util.List<java.lang.String>> publicQuotes = Optional.empty();
private Optional<java.util.Map<java.nio.file.Path, java.lang.String>> publishedPagesByPath = Optional.empty();
private Optional<java.util.Map<java.lang.String, de.paulr.knowledge.ContextualizedPage>> contextualizedPages = Optional.empty();
public KnowledgeBase(java.nio.file.Path basePath) {
this.basePath = basePath;
}
@java.lang.SuppressWarnings("unused")
private KnowledgeBase() {}
public java.nio.file.Path getBasePath() {
return basePath;
}
public java.nio.file.Path getPageDir() {
pageDir = Optional.of(pageDir.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.pageDir(getBasePath())));
return pageDir.get();
}
public java.util.List<java.nio.file.Path> getPagePaths() {
pagePaths = Optional.of(pagePaths.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.pagePaths(getPageDir())));
return pagePaths.get();
}
public java.util.List<de.paulr.knowledge.Page> getPages() {
pages = Optional.of(pages.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.pages(getPagePaths())));
return pages.get();
}
public java.util.Map<java.lang.String, de.paulr.knowledge.Page> getPagesByTitle() {
pagesByTitle = Optional.of(pagesByTitle.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.pagesByTitle(getPages())));
return pagesByTitle.get();
}
public java.nio.file.Path getAttachmentDir() {
attachmentDir = Optional.of(attachmentDir.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.attachmentDir(getBasePath())));
return attachmentDir.get();
}
public java.util.Map<java.lang.String, java.nio.file.Path> getAttachmentPaths() {
attachmentPaths = Optional.of(attachmentPaths.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.attachmentPaths(getAttachmentDir())));
return attachmentPaths.get();
}
public java.lang.String getParseabilitySummary() {
parseabilitySummary = Optional.of(parseabilitySummary.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.parseabilitySummary(getPages())));
return parseabilitySummary.get();
}
public java.util.List<java.lang.String> getPublicQuotes() {
publicQuotes = Optional.of(publicQuotes.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.quotes(getPages())));
return publicQuotes.get();
}
public java.util.Map<java.nio.file.Path, java.lang.String> getPublishedPagesByPath() {
publishedPagesByPath = Optional.of(publishedPagesByPath.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.publishedPagesByPath(getPages())));
return publishedPagesByPath.get();
}
public java.util.Map<java.lang.String, de.paulr.knowledge.ContextualizedPage> getContextualizedPages() {
contextualizedPages = Optional.of(contextualizedPages.orElseGet(() -> de.paulr.knowledge.KnowledgeBaseAmalgam.contextualizedPages(getPages())));
return contextualizedPages.get();
}
}
