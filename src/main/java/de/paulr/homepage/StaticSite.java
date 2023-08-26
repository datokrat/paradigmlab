// AUTO-GENERATED AMALGAMATED CLASS
package de.paulr.homepage;

import java.util.Optional;

public class StaticSite {
private java.nio.file.Path inputDir;
private java.nio.file.Path outputDir;
private de.paulr.knowledge.KnowledgeBase knowledgeBase;
private Optional<java.nio.file.Path> templatePath = Optional.empty();
private Optional<java.lang.String> template = Optional.empty();
private Optional<java.util.Map<java.lang.String, java.nio.file.Path>> categoryPrefixes = Optional.empty();
private Optional<java.util.function.Function<java.lang.String, java.util.Optional<java.lang.String>>> categoryForTitleFn = Optional.empty();
private Optional<java.util.function.Function<java.lang.String, java.nio.file.Path>> pathForTitleFn = Optional.empty();
private Optional<java.util.function.Function<java.lang.String, java.lang.String>> titleWithoutPrefixFn = Optional.empty();
private Optional<java.util.function.Function<java.lang.String, java.nio.file.Path>> pathForLinkTargetFn = Optional.empty();
private Optional<java.util.function.Function<java.lang.String, java.lang.String>> defaultCaptionForLinkTargetFn = Optional.empty();
private Optional<de.paulr.markdown.MarkdownPage> indexPage = Optional.empty();
private Optional<java.lang.String> indexHtml = Optional.empty();
private Optional<java.util.Map<java.nio.file.Path, java.lang.String>> textByPath = Optional.empty();
private Optional<java.lang.Runnable> generator = Optional.empty();
public StaticSite(java.nio.file.Path inputDir, java.nio.file.Path outputDir, de.paulr.knowledge.KnowledgeBase knowledgeBase) {
this.inputDir = inputDir;
this.outputDir = outputDir;
this.knowledgeBase = knowledgeBase;
}
@java.lang.SuppressWarnings("unused")
private StaticSite() {}
public java.nio.file.Path getInputDir() {
return inputDir;
}
public java.nio.file.Path getOutputDir() {
return outputDir;
}
public de.paulr.knowledge.KnowledgeBase getKnowledgeBase() {
return knowledgeBase;
}
public java.nio.file.Path getTemplatePath() {
templatePath = Optional.of(templatePath.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.templatePath(getInputDir())));
return templatePath.get();
}
public java.lang.String getTemplate() {
template = Optional.of(template.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.template(getTemplatePath())));
return template.get();
}
public java.util.Map<java.lang.String, java.nio.file.Path> getCategoryPrefixes() {
categoryPrefixes = Optional.of(categoryPrefixes.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.categoryPrefixes()));
return categoryPrefixes.get();
}
public java.util.function.Function<java.lang.String, java.util.Optional<java.lang.String>> getCategoryForTitleFn() {
categoryForTitleFn = Optional.of(categoryForTitleFn.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.categoryForTitleFn(getCategoryPrefixes())));
return categoryForTitleFn.get();
}
public java.util.function.Function<java.lang.String, java.nio.file.Path> getPathForTitleFn() {
pathForTitleFn = Optional.of(pathForTitleFn.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.pathForTitleFn(getCategoryPrefixes(), getCategoryForTitleFn())));
return pathForTitleFn.get();
}
public java.util.function.Function<java.lang.String, java.lang.String> getTitleWithoutPrefixFn() {
titleWithoutPrefixFn = Optional.of(titleWithoutPrefixFn.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.titleWithoutPrefixFn(getCategoryPrefixes(), getCategoryForTitleFn())));
return titleWithoutPrefixFn.get();
}
public java.util.function.Function<java.lang.String, java.nio.file.Path> getPathForLinkTargetFn() {
pathForLinkTargetFn = Optional.of(pathForLinkTargetFn.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.pathForLinkTargetFn(getPathForTitleFn())));
return pathForLinkTargetFn.get();
}
public java.util.function.Function<java.lang.String, java.lang.String> getDefaultCaptionForLinkTargetFn() {
defaultCaptionForLinkTargetFn = Optional.of(defaultCaptionForLinkTargetFn.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.defaultCaptionForLinkTargetFn(getTitleWithoutPrefixFn())));
return defaultCaptionForLinkTargetFn.get();
}
public de.paulr.markdown.MarkdownPage getIndexPage() {
indexPage = Optional.of(indexPage.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.indexPage(getKnowledgeBase(), getCategoryForTitleFn(), getCategoryPrefixes())));
return indexPage.get();
}
public java.lang.String getIndexHtml() {
indexHtml = Optional.of(indexHtml.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.indexHtml(getIndexPage(), getKnowledgeBase(), getPathForLinkTargetFn(), getTemplate(), getDefaultCaptionForLinkTargetFn())));
return indexHtml.get();
}
public java.util.Map<java.nio.file.Path, java.lang.String> getTextByPath() {
textByPath = Optional.of(textByPath.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.htmlByPath(getKnowledgeBase(), getTemplate(), getPathForLinkTargetFn(), getDefaultCaptionForLinkTargetFn(), getIndexHtml())));
return textByPath.get();
}
public java.lang.Runnable getGenerator() {
generator = Optional.of(generator.orElseGet(() -> de.paulr.homepage.StaticSiteAmalgam.generator(getTextByPath(), getInputDir(), getOutputDir(), getCategoryPrefixes(), getKnowledgeBase())));
return generator.get();
}
}
