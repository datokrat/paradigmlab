package de.paulr.amalgam;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.paulr.amalgam.AmalgamData.Rule;

@AmalgamAnnotation
public class AmalgamAmalgam {

	public static final String name = "name";
	public static final String simpleName = "simpleName";
	public static final String packageName = "packageName";
	public static final String clazz = "clazz";
	public static final String tags = "tags";
	public static final String amalgamData = "amalgamData";

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

	@Feat(clazz)
	public static Class<?> clazz(@Feat(name) String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Feat(name)
	public static String nameForClazz(@Feat(clazz) Class<?> clazz) {
		return clazz.getName();
	}

	@Feat(amalgamData)
	public static AmalgamData amalgamData(@Feat(clazz) Class<?> clazz,
		@Feat(tags) String[] amalgamationTags) {
		if (!clazz.isAnnotationPresent(AmalgamAnnotation.class)) {
			throw new RuntimeException("not annotated");
		}

		AmalgamData amalgam = new AmalgamData();

		Set<String> amalgamationTagSet = Set.of(amalgamationTags);

		for (var method : clazz.getDeclaredMethods()) {
			var annotation = method.getAnnotation(Feat.class);
			if (annotation == null || !isPublicStaticMethod(method)) {
				continue;
			}

			Set<String> tags = Optional.ofNullable(method.getAnnotation(Tags.class))
				.map(Tags::value).stream().flatMap(Arrays::stream).collect(Collectors.toSet());

			if (!tags.stream().allMatch(amalgamationTagSet::contains)) {
				continue;
			}

			learnFeature(amalgam, annotation.value(), method.getGenericReturnType());
			List<String> parameterNames = new ArrayList<>();
			for (var parameter : method.getParameters()) {

				String parameterName = Optional.ofNullable(parameter.getAnnotation(Feat.class)) //
					.map(Feat::value) //
					.orElse(parameter.getName());
				parameterNames.add(parameterName);
				learnFeature(amalgam, parameterName, parameter.getParameterizedType());
			}

			learnRule(amalgam, new Rule(method.getName(), annotation.value(), parameterNames));
		}
		return amalgam;
	}

	private static boolean isPublicStaticMethod(Method method) {
		return method.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC);
	}

	private static void learnFeature(AmalgamData amalgam, String name, Type type) {
		if (amalgam.features().get(name) != null && !type.equals(amalgam.features().get(name))) {
			throw new RuntimeException("inconsistent feature type for feature " + name);
		}
		amalgam.features().put(name, type);
	}

	private static void learnRule(AmalgamData amalgam, Rule rule) {
		amalgam.rules().add(rule);
	}

}
