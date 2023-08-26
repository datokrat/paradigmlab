// AUTO-GENERATED AMALGAMATED CLASS
package de.paulr.knowledge;

import java.util.Optional;

public class ContextualizedPage {
private java.util.Set<java.lang.String> incomingReferences;
de.paulr.knowledge.Page knownClass_de_paulr_knowledge_Page;
public ContextualizedPage(de.paulr.knowledge.Page knownClass_de_paulr_knowledge_Page, java.util.Set<java.lang.String> incomingReferences) {
this.incomingReferences = incomingReferences;
this.knownClass_de_paulr_knowledge_Page = knownClass_de_paulr_knowledge_Page;
}
@java.lang.SuppressWarnings("unused")
private ContextualizedPage() {}
public java.util.Set<java.lang.String> getIncomingReferences() {
return incomingReferences;
}
public java.nio.file.Path getFilePath() {
return knownClass_de_paulr_knowledge_Page.getFilePath();
}
public java.lang.String getFileName() {
return knownClass_de_paulr_knowledge_Page.getFileName();
}
public java.lang.String getMarkdown() {
return knownClass_de_paulr_knowledge_Page.getMarkdown();
}
public java.lang.String getTitle() {
return knownClass_de_paulr_knowledge_Page.getTitle();
}
public java.util.Optional<de.paulr.markdown.MarkdownPage> getAst() {
return knownClass_de_paulr_knowledge_Page.getAst();
}
public java.util.Set<java.lang.String> getReferences() {
return knownClass_de_paulr_knowledge_Page.getReferences();
}
}
