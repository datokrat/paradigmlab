package de.paulr.aoc2023.day25;

import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.parser.Parsers.regex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.paulr.aoc2023.ASolution;
import de.paulr.parser.IParser;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

class Solution extends ASolution {

	public static IParser<String> alphanumPsr = regex("\\w+");
	public static IParser<Pair<String, Set<String>>> wirePsr = alphanumPsr.thenSilently(": ")
		.then(alphanumPsr.plus(" ").map(Rope::toSet));

	public List<Pair<String, Set<String>>> wires;
	public Map<String, Set<String>> out;

	public Solution() {
		super(2023, 25, "");
	}

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partA());
	}

	@Override
	public Object partA() {
		parseInput();
		Map<Pair<String, String>, Long> capacities = new HashMap<>();
		Set<String> nodes = new HashSet<>();
		for (var wire : wires) {
			var source = wire.first();
			nodes.add(source);
			for (var target : wire.second()) {
				nodes.add(target);
				assert !capacities.containsKey(Pair.of(source, target));
				assert !capacities.containsKey(Pair.of(target, source));
				capacities.put(Pair.of(source, target), 1L);
				capacities.put(Pair.of(target, source), 1L);
			}
		}
		FlowNetwork<String> network = new FlowNetwork<>(capacities);
		String chosenNode = nodes.stream().findFirst().get();
		for (var node : nodes) {
			if (node.equals(chosenNode)) {
				continue;
			}
			var cut = new Flow<>(network).findResidualMinCut(chosenNode, node);
			if (network.getCutSize(cut) <= 3) {
				return cut.size() * (nodes.size() - cut.size());
			}
		}
		return "Meh, nothing found";
	}

	@Override
	public Object partB() {
		// TODO Auto-generated method stub
		return null;
	}

	public void parseInput() {
		wires = lines.stream().map(wirePsr::parseOne).toList();
	}

}
