package de.paulr.amalgam;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.persistence.jaxb.JAXBContextFactory;

import de.paulr.amalgam.AmalgamData.Rule;
import de.paulr.util.Rope;
import jakarta.xml.bind.JAXBException;

@AmalgamAnnotation
public class AmalgamatedClassAmalgam {

	public static final String name = "name";
	public static final String simpleName = "simpleName";
	public static final String packageName = "packageName";
	public static final String declarationPath = "declarationPath";
	public static final String classPath = "classPath";
	public static final String declaration = "declaration";
	public static final String amalgam = "amalgam";
	public static final String knowns = "knowns";
	public static final String knownClasses = "knownClasses";
	public static final String outsourcedFeatures = "outsourcedFeatures";
	public static final String derivableFeatures = "derivableFeatures";
	public static final String knownOrDerivableFeatures = "knownOrDerivableFeatures";

	public static final String generatedHeader = "generatedHeader";
	public static final String generatedFieldDeclarations = "generatedFieldDeclarations";
	public static final String generatedConstructorDeclarations = "generatedConstructorDeclarations";
	public static final String generatedFactoryMethods = "generatedFactoryMethods";
	public static final String generatedGetters = "generatedGetters";
	public static final String generatedJava = "generatedJava";

	@Feat(simpleName)
	public static String simpleName(@Feat(name) String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

	@Feat(packageName)
	public static String packageName(@Feat(name) String name) {
		return name.substring(0, name.lastIndexOf('.'));
	}

	@Feat(name)
	public static String name(@Feat(packageName) String packageName,
		@Feat(simpleName) String simpleName) {
		return packageName + "." + simpleName;
	}

	@Feat(declarationPath)
	public static Path declarationPath(@Feat(name) String name) {
		return Path.of("./src/main/resources/" + name.replaceAll("\\.", "/") + ".mlgm");
	}

	@Feat(classPath)
	public static Path classPath(@Feat(name) String name) {
		return Path.of("./src/main/java/" + name.replaceAll("\\.", "/") + ".java");
	}

	@Feat(declaration)
	public static AmalgamatedClassDeclaration declaration(
		@Feat(declarationPath) Path declarationPath) {
		try {
			var context = JAXBContextFactory
				.createContext(new Class<?>[] { AmalgamatedClassDeclaration.class }, null);
			var unmarshaller = context.createUnmarshaller();
			return (AmalgamatedClassDeclaration) unmarshaller.unmarshal(declarationPath.toFile());
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Feat(amalgam)
	public static Amalgam amalgam(@Feat(declaration) AmalgamatedClassDeclaration declaration) {
		return new Amalgam(declaration.getAmalgam(), declaration.getTags());
	}

	@Feat(knowns)
	public static Set<String> knowns(@Feat(declaration) AmalgamatedClassDeclaration declaration) {
		return new LinkedHashSet<>(List.of(declaration.getKnowns()));
	}

	@Feat(knownClasses)
	public static Set<AmalgamatedClass> knownClasses(
		@Feat(declaration) AmalgamatedClassDeclaration declaration) {
		return Arrays.stream(declaration.getKnownClasses()) //
			.map(AmalgamatedClass::new) //
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Feat(outsourcedFeatures)
	public static Map<String, AmalgamatedClass> outsourcedFeaturesAndSource(
		@Feat(knownClasses) Set<AmalgamatedClass> knownClasses) {
		Map<String, AmalgamatedClass> sourceByFeature = new LinkedHashMap<>();
		for (var clazz : knownClasses) {
			for (var feature : clazz.getKnownOrDerivableFeatures()) {
				sourceByFeature.put(feature, clazz);
			}
		}
		return sourceByFeature;
	}

	@Feat(derivableFeatures)
	public static Map<String, Rule> derivableFeatures(@Feat(knowns) Set<String> knowns,
		@Feat(outsourcedFeatures) Map<String, AmalgamatedClass> outsourcedFeatures,
		@Feat(amalgam) Amalgam amalgam) {
		Set<String> featuresToDeriveFrom = new LinkedHashSet<>(knowns);
		featuresToDeriveFrom.addAll(outsourcedFeatures.keySet());
		return getDerivableFeatures(featuresToDeriveFrom, amalgam.getAmalgamData().rules());
	}

	// TODO: update fields and derivation logic (also factory methods)

	@Feat(knownOrDerivableFeatures)
	public static Set<String> knownOrDerivableFeatures(@Feat(knowns) Set<String> knowns,
		@Feat(derivableFeatures) Map<String, Rule> derivableFeatures) {
		Set<String> result = new LinkedHashSet<>(knowns);
		result.addAll(derivableFeatures.keySet());
		return result;
	}

	@Feat(generatedHeader)
	public static Rope<String> generatedHeader(@Feat(packageName) String packageName) {
		return Rope.of(//
			"// AUTO-GENERATED AMALGAMATED CLASS", //
			"package " + packageName + ";", //
			"", //
			"import java.util.Optional;");
	}

	@Feat(generatedFieldDeclarations)
	public static Rope<String> generatedFieldDeclarations(@Feat(knowns) Set<String> knowns,
		@Feat(knownClasses) Set<AmalgamatedClass> knownClasses,
		@Feat(knownOrDerivableFeatures) Set<String> knownOrDerivableFeatures,
		@Feat(outsourcedFeatures) Map<String, AmalgamatedClass> outsourcedFeatures,
		@Feat(amalgam) Amalgam amalgam) {
		Rope<String> result = Rope.empty();

		for (var feature : knownOrDerivableFeatures) {
			if (outsourcedFeatures.containsKey(feature) && !knowns.contains(feature)) {
				continue;
			}
			String modifiers = "private ";
			String rest = ";";
			if (!knowns.contains(feature)) {
				rest = " = " + renderOptionalClass(amalgam.getAmalgamData().features().get(feature))
					+ ".empty()" + rest;
			}
			result = result
				.addRight(modifiers + getFeatureDeclaration(amalgam, feature, knowns) + rest);
		}

		for (var clazz : knownClasses) {
			result = result.addRight(clazz.getName() + " " + getKnownClassFieldName(clazz) + ";");
		}

		return result;
	}

	@Feat(generatedConstructorDeclarations)
	public static Rope<String> generatedConstructorDeclaration(@Feat(knowns) Set<String> knowns,
		@Feat(knownClasses) Set<AmalgamatedClass> knownClasses, @Feat(simpleName) String simpleName,
		@Feat(amalgam) Amalgam amalgam) {
		Rope<String> result = Rope.empty();

		Stream<String> variableDeclarations = Stream.concat( //
			knownClasses.stream() //
				.map(clazz -> getKnownClassDeclaration(clazz)), //
			knowns.stream() //
				.map(feature -> getFeatureDeclaration(amalgam, feature, knowns)));

		result = result.addRight("public " + simpleName + "("
			+ String.join(", ", variableDeclarations.toList()) + ") {");
		for (var known : knowns) {
			result = result.addRight("this." + known + " = " + known + ";");
		}
		for (var clazz : knownClasses) {
			result = result.addRight("this." + getKnownClassFieldName(clazz) + " = "
				+ getKnownClassFieldName(clazz) + ";");
		}
		result = result.addRight("}");

		result = result.addRight("@java.lang.SuppressWarnings(\"unused\")");
		result = result.addRight("private " + simpleName + "() {}");
		return result;
	}

	@Feat(generatedFactoryMethods)
	public static Rope<String> generatedFactoryMethods(
		@Feat(declaration) AmalgamatedClassDeclaration declaration,
		@Feat(knowns) Set<String> knowns, @Feat(simpleName) String simpleName,
		@Feat(knownClasses) Set<AmalgamatedClass> knownClasses,
		@Feat(outsourcedFeatures) Map<String, AmalgamatedClass> outsourcedFeatures,
		@Feat(amalgam) Amalgam amalgam,
		@Feat(derivableFeatures) Map<String, Rule> derivableFeatures) {

		Rope<String> result = Rope.empty();

		Set<String> featuresToDerive = Stream
			.concat(knowns.stream(),
				knownClasses.stream().flatMap(clazz -> clazz.getKnowns().stream()))
			.collect(Collectors.toCollection(LinkedHashSet::new));

		for (var ctor : declaration.getConstructors()) {
			List<Rule> rulesToEvaluate = amalgam.getAmalgamData()
				.getOrderedRulesToDerive(Set.of(ctor.getParameters()), featuresToDerive);

			List<String> parameterDeclarations = List.of(ctor.getParameters()).stream() //
				.map(parameter -> renderType(amalgam.getAmalgamData().features().get(parameter))
					+ " feat" + parameter) //
				.toList();

			result = result.concat(Rope.of( //
				"@java.lang.SuppressWarnings(\"unused\")", //
				"public static " + simpleName + " " + ctor.getName() + "("
					+ String.join(", ", parameterDeclarations) + ") {", //
				"var obj = new " + simpleName + "();"));

			for (var parameter : ctor.getParameters()) {
				result = result.addRight("var tmp" + parameter + " = feat" + parameter + ";");
				if (knowns.contains(parameter)) {
					result = result.addRight("obj." + parameter + " = tmp" + parameter + ";");
				} else if (derivableFeatures.containsKey(parameter)
					&& !outsourcedFeatures.containsKey(parameter)) {
					result = result.addRight("obj." + parameter + " = "
						+ renderOptionalClass(amalgam.getAmalgamData().features().get(parameter))
						+ ".of(tmp" + parameter + ");");
				}
			}

			for (var rule : rulesToEvaluate) {
				List<String> depValues = rule.dependencies().stream() //
					.map(dep -> {
						return "tmp" + dep;
					}) //
					.toList();
				String rhs = declaration.getAmalgam().getName() + "." + rule.method() + "("
					+ String.join(", ", depValues) + ")";
				result = result.addRight("var tmp" + rule.feature() + " = " + rhs + ";");
				if (knowns.contains(rule.feature())) {
					result = result
						.addRight("obj." + rule.feature() + " = tmp" + rule.feature() + ";");
				} else if (derivableFeatures.containsKey(rule.feature())
					&& !outsourcedFeatures.containsKey(rule.feature())) {
					result = result.addRight("obj." + rule.feature() + " = "
						+ renderOptionalClass(
							amalgam.getAmalgamData().features().get(rule.feature()))
						+ ".of(tmp" + rule.feature() + ");");
				}
			}

			for (var clazz : knownClasses) {
				String constructorArguments = clazz.getKnowns().stream() //
					.map(feature -> "tmp" + feature) //
					.collect(Collectors.joining(", "));

				result = result.addRight("obj." + getKnownClassFieldName(clazz) + " = new "
					+ clazz.getName() + "(" + constructorArguments + ");");
			}

			result = result.addRight("return obj;");
			result = result.addRight("}");
		}

		return result;
	}

	@Feat(generatedGetters)
	public static Rope<String> generatedGetters(@Feat(knowns) Set<String> knowns,
		@Feat(outsourcedFeatures) Map<String, AmalgamatedClass> outsourcedFeatures,
		@Feat(declaration) AmalgamatedClassDeclaration declaration,
		@Feat(derivableFeatures) Map<String, Rule> derivableFeatures,
		@Feat(amalgam) Amalgam amalgam) {
		Rope<String> result = Rope.empty();

		for (var known : knowns) {
			result = result.concat(Rope.of( //
				"public " + renderType(amalgam.getAmalgamData().features().get(known)) + " "
					+ getFeatureMethodName(known) + "() {", //
				"return " + known + ";", //
				"}"));
		}

		for (var rule : derivableFeatures.values()) {
			if (outsourcedFeatures.containsKey(rule.feature())) {
				continue;
			}
			String feature = rule.feature();
			result = result.concat(Rope.of( //
				"public " + renderType(amalgam.getAmalgamData().features().get(feature)) + " "
					+ getFeatureMethodName(feature) + "() {", //
				feature + " = "
					+ renderOptionalClass(amalgam.getAmalgamData().features().get(feature)) + ".of("
					+ feature + ".orElseGet(() -> " + getRuleEvaluation(rule, declaration) + "));", //
				"return " + feature + "."
					+ renderOptionalGetMethod(amalgam.getAmalgamData().features().get(feature))
					+ "();", //
				"}"));
		}

		for (var outsourcedFeature : outsourcedFeatures.keySet()) {
			AmalgamatedClass clazz = outsourcedFeatures.get(outsourcedFeature);
			result = result.concat(Rope.of( //
				"public " + renderType(amalgam.getAmalgamData().features().get(outsourcedFeature))
					+ " " + getFeatureMethodName(outsourcedFeature) + "() {", //
				"return " + getKnownClassFieldName(clazz) + "."
					+ getFeatureMethodName(outsourcedFeature) + "();",
				"}"));
		}

		return result;
	}

	@Feat(generatedJava)
	public static Rope<String> generatedJava(@Feat(simpleName) String simpleName,
		@Feat(generatedHeader) Rope<String> generatedHeader,
		@Feat(generatedFieldDeclarations) Rope<String> generatedFieldDeclarations,
		@Feat(generatedConstructorDeclarations) Rope<String> generatedConstructorDeclarations,
		@Feat(generatedFactoryMethods) Rope<String> generatedFactoryMethods,
		@Feat(generatedGetters) Rope<String> generatedGetters) {
		return generatedHeader //
			.addRight("").addRight("public class " + simpleName + " {") //
			.concat(generatedFieldDeclarations) //
			.concat(generatedConstructorDeclarations) //
			.concat(generatedFactoryMethods) //
			.concat(generatedGetters) //
			.addRight("}");
	}

	private static String getRuleEvaluation(Rule rule, AmalgamatedClassDeclaration declaration) {
		List<String> recursiveCalls = rule.dependencies().stream() //
			.map(dep -> getFeatureMethodName(dep) + "()") //
			.toList();
		return declaration.getAmalgam().getName() + "." + rule.method() + "("
			+ String.join(", ", recursiveCalls) + ")";
	}

	private static String getFeatureMethodName(String feature) {
		return "get" + Character.toUpperCase(feature.charAt(0)) + feature.substring(1);
	}

	private static String getKnownClassDeclaration(AmalgamatedClass clazz) {
		return clazz.getName() + " " + getKnownClassFieldName(clazz);
	}

	private static String getKnownClassFieldName(AmalgamatedClass clazz) {
		return "knownClass_" + clazz.getName().replaceAll("\\.", "_");
	}

	private static String getFeatureDeclaration(Amalgam amalgam, String feature,
		Set<String> knowns) {
		if (knowns.contains(feature)) {
			return getKnownFeatureDeclaration(amalgam, feature);
		} else {
			return getDerivableFeatureDeclaration(amalgam, feature);
		}
	}

	private static String getDerivableFeatureDeclaration(Amalgam amalgam, String feature) {
		return renderOptionalType(amalgam.getAmalgamData().features().get(feature)) + " " + feature;
	}

	private static String getKnownFeatureDeclaration(Amalgam amalgam, String feature) {
		return renderType(amalgam.getAmalgamData().features().get(feature)) + " " + feature;
	}

	private static String renderType(Type type) {
		return type.getTypeName().replaceAll("\\$", "."); // Handle inner classes correctly
	}

	private static String renderOptionalType(Type type) {
		if (!(type instanceof Class<?> clazz && clazz.isPrimitive())) {
			return "Optional<" + renderType(type) + ">";
		} else if (clazz.equals(int.class)) {
			return "java.util.OptionalInt";
		} else if (clazz.equals(boolean.class)) {
			return "java.util.Optional<Boolean>";
		} else {
			throw new RuntimeException("unsupported primitive type " + clazz);
		}
	}

	private static String renderOptionalClass(Type type) {
		if (!(type instanceof Class<?> clazz && clazz.isPrimitive())) {
			return "Optional";
		} else if (clazz.equals(int.class)) {
			return "java.util.OptionalInt";
		} else if (clazz.equals(boolean.class)) {
			return "java.util.Optional";
		} else {
			throw new RuntimeException("unsupported primitive type " + clazz);
		}
	}

	private static String renderOptionalGetMethod(Type type) {
		if (!(type instanceof Class<?> clazz && clazz.isPrimitive())) {
			return "get";
		} else if (clazz.equals(int.class)) {
			return "getAsInt";
		} else if (clazz.equals(boolean.class)) {
			return "get";
		} else {
			throw new RuntimeException("unsupported primitive type " + clazz);
		}
	}

	private static Map<String, Rule> getDerivableFeatures(Set<String> knowns,
		List<AmalgamData.Rule> rules) {
		Set<String> derivables = new LinkedHashSet<String>();
		derivables.addAll(knowns);
		Map<String, Rule> derivableToRule = new LinkedHashMap<String, Rule>();
		boolean loop = true;
		while (loop) {
			loop = false;
			for (var rule : rules) {
				if (!derivables.contains(rule.feature())
					&& derivables.containsAll(rule.dependencies())) {
					derivables.add(rule.feature());
					derivableToRule.put(rule.feature(), rule);
					loop = true;
				}
			}
		}
		return derivableToRule;
	}

}
