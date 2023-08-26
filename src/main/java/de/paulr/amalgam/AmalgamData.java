package de.paulr.amalgam;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.paulr.amalgam.AmalgamData.Rule;

public record AmalgamData(Map<String, Type> features, List<Rule> rules) {

	public AmalgamData() {
		this(new LinkedHashMap<String, Type>(), new ArrayList<Rule>());
	}

	public Map<String, Rule> getDerivableFeatures(Set<String> knowns) {
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

	public List<Rule> getOrderedRulesToDerive(Set<String> knowns, Set<String> toDerive) {
		Map<String, Rule> rules = getDerivableFeatures(knowns);

		Set<String> relevantRules = new HashSet<String>();
		Deque<String> featuresToDerive = new ArrayDeque<String>(toDerive);
		while (!featuresToDerive.isEmpty()) {
			String featureToDerive = featuresToDerive.poll();
			if (knowns.contains(featureToDerive)) {
				continue;
			}

			Rule rule = rules.get(featureToDerive);
			relevantRules.add(featureToDerive);
			featuresToDerive.addAll(rule.dependencies());
		}

		return rules.values().stream() //
			.filter(rule -> relevantRules.contains(rule.feature())) //
			.toList();
	}

	public record Rule(String method, String feature, List<String> dependencies) {
	}
}
