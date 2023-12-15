package de.paulr.aoc2023.day15;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.paulr.aoc2023.ASolution;
import de.paulr.util.Pair;

class Solution extends ASolution {

	public Solution() {
		super(2023, 15);
	}

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partB());
	}

	public Solution(int year, int day) {
		super(year, day);
		// TODO Auto-generated constructor stub
	}

	public Solution(int year, int day, String suffix) {
		super(year, day, suffix);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object partA() {
		String[] seq = file.replaceAll("\\n|\\r", "").split(Pattern.quote(","));
		long sum = 0L;
		for (var s : seq) {
			sum += HASH(s);
		}
		return sum;
	}

	@Override
	public Object partB() {
		String[] seq = file.replaceAll("\\n|\\r", "").split(Pattern.quote(","));
		HASHMAP map = new HASHMAP(new HashMap<>());
		for (var s : seq) {
			if (s.contains("=")) {
				String[] split = s.split(Pattern.quote("="));
				map.put(split[0], Integer.parseInt(split[1]));
			} else {
				String[] split = s.split(Pattern.quote("-"));
				map.remove(split[0]);
			}
		}
		return map.focusingPowers();
	}

	public record HASHMAP(Map<Integer, List<Pair<String, Integer>>> map) {

		public long focusingPowers() {
			long sum = 0L;
			for (var entry : map.entrySet()) {
				int hash = entry.getKey();
				for (int i = 0; i < entry.getValue().size(); i++) {
					var p = entry.getValue().get(i);
					sum += (hash + 1) * (i + 1) * p.second();
				}
			}
			return sum;
		}

		public void put(String key, int value) {
			int hash = HASH(key);
			var l = map.computeIfAbsent(hash, __ -> new ArrayList<>());
			for (int i = 0; i < l.size(); i++) {
				if (l.get(i).first().equals(key)) {
					l.set(i, Pair.of(key, value));
					return;
				}
			}
			l.add(Pair.of(key, value));
		}

		public void remove(String key) {
			int hash = HASH(key);
			var l = map.computeIfAbsent(hash, __ -> new ArrayList<>());
			for (int i = 0; i < l.size(); i++) {
				if (l.get(i).first().equals(key)) {
					l.remove(i);
				}
			}
		}

	}

	public static int asciiCode(char c) {
		return c;
	}

	public static int HASH(String s) {
		int hash = 0;
		for (int i = 0; i < s.length(); i++) {
			hash += asciiCode(s.charAt(i));
			hash *= 17;
			hash %= 256;
		}
		return hash;
	}

}
