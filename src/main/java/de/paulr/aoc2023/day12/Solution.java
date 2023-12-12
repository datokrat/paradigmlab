package de.paulr.aoc2023.day12;

import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.parser.Parsers.longNumber;
import static de.paulr.parser.Parsers.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.paulr.aoc2023.ASolution;
import de.paulr.parser.IParser;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

class Solution extends ASolution {

	public static final String FILE = "2023_12.txt";
	public static final String EXAMPLE = "2023_12s.txt";

	public Solution(String filename) {
		super(filename);
	}

	public static void main(String[] args) {
		var s = new Solution(FILE);
		prynt(s.partB());
	}

	@Override
	public Object partA() {
		long sum = 0;
		Map<Query, Long> cache = new HashMap<>();
		int i = 0;
		for (var line : lines) {
			Query query = Query.fromInput(line);
			sum += dp(query, cache);
			prynt("{} -> {}: {}", i, line, dp(query, cache));
			i++;
		}
		return sum;
	}

	@Override
	public Object partB() {
		long sum = 0;
		Map<Query, Long> cache = new HashMap<>();
		int i = 0;
		for (var line : lines) {
			Query query = Query.fromInput(line).repeat(5);
			sum += dp(query, cache);
			prynt("{} -> {}: {}", i, line, dp(query, cache));
			i++;
		}
		return sum;
	}

	public long dp(String record, List<Long> groups) {
		return dp(new Query(record, groups));
	}

	public long dp(Query query) {
		return dp(query, new HashMap<>());
	}

	public long dp(Query query, Map<Query, Long> cache) {
		String record = query.record + '.';
		long sum = 0L;
		for (int i = 0; i < record.length(); i++) {
			char c = record.charAt(i);
			Query sanitizedQuery = new Query(record, query.groups).restrict(i, record.length(), 0,
				query.groups.size());
			switch (c) {
			case '.':
				continue;
			case '#':
				return sum + dpInternalFivefold(sanitizedQuery, cache);
			case '?':
				sum += dpInternalFivefold(sanitizedQuery, cache);
				continue;
			}
		}
		// Here we may assume that all characters are either . or ?
		// and in the latter case, the # branch was already taken care of
		if (query.groups.size() == 0) {
			return sum + 1;
		} else {
			return sum;
		}
	}

	public long dpInternalFivefold(Query key, Map<Query, Long> cache) {
		if (key.groups.size() % 5 != 0 || key.groups.size() == 0) {
			throw new RuntimeException();
		}
		int step = key.groups.size() / 5;

		long sum = 0L;
		for (int sep1 = 2; sep1 + 1 < key.record.length(); sep1++) {
			Query key1 = key.restrict(0, sep1, 0, step);
			final long l1 = dpInternal(key1, cache);
			if (l1 == 0)
				continue;
			for (int sep2 = sep1 + 2; sep2 + 1 < key.record.length(); sep2++) {
				Query key2 = key.restrict(sep1, sep2, step, 2 * step);
				final long l2 = dpInternal(key2, cache);
				if (l2 == 0)
					continue;
				for (int sep3 = sep2 + 2; sep3 + 1 < key.record.length(); sep3++) {
					Query key3 = key.restrict(sep2, sep3, 2 * step, 3 * step);
					final long l3 = dpInternal(key3, cache);
					if (l3 == 0)
						continue;
					for (int sep4 = sep3 + 2; sep4 + 1 < key.record.length(); sep4++) {
						Query key4 = key.restrict(sep3, sep4, 3 * step, 4 * step);
						final long l4 = dpInternal(key4, cache);
						if (l4 == 0)
							continue;
						Query key5 = key.restrict(sep4, key.record.length(), 4 * step, key.groups.size());
						long l5 = dpInternal(key5, cache);
						if (l5 == 0)
							continue;

						sum += l1 * l2 * l3 * l4 * l5;
					}
				}
			}
		}

		return sum;
	}

	/**
	 * Case groups.size() == 1: returns the number of possibilities such that
	 * record[char1, char2) starts with `group` many '#', followed only by at least
	 * one '.'.
	 */
	public long dpInternal(Query key, Map<Query, Long> cache) {
		Long cached = cache.get(key);
		if (cached != null) {
			return cached;
		}

		String record = key.record;
		List<Long> groups = key.groups;

		if (groups.size() == 0) {
			return record.length() == 0 ? 1L : 0L;
		} else if (groups.size() == 1) {
			long group = groups.get(0);
			if (record.length() < group + 1)
				return 0L;
			for (int i = 0; i < record.length(); i++) {
				char c = record.charAt(i);
				if (i < group && c == '.')
					return 0L;
				if (i >= group && c == '#')
					return 0L;
			}
			return 1L;
		}

		long minimalLength = 0L;
		for (int i = 0; i < groups.size(); i++) {
			minimalLength += groups.get(i) + 1;
		}
		if (record.length() < minimalLength) {
			return 0L;
		}

		long sum = 0L;
		int groupMid = groups.size() / 2;
		for (int charMid = 1; charMid < record.length(); charMid++) {
			Query query1 = key.restrict(0, charMid, 0, groupMid);
			long l1 = dpInternal(query1, cache);
			if (l1 == 0L) {
				continue;
			}
			Query query2 = key.restrict(charMid, record.length(), groupMid, groups.size());
			long l2 = dpInternal(query2, cache);
			sum += l1 * l2;
		}

		cache.put(key, sum);
		return sum;
	}

	public record Query(String record, List<Long> groups) {

		public static IParser<Query> psr = regex("[#.?]*").thenSilently(" ")
			.then(longNumber.star(",").map(Rope::toList)) //
			.map(Pair.fn(Query::new));

		public static Query fromInput(String line) {
			return psr.parseOne(line);
		}

		public Query restrict(int recordBegin, int recordEnd, int groupsBegin, int groupsEnd) {
			return new Query(record.substring(recordBegin, recordEnd), groups.subList(groupsBegin, groupsEnd));
		}

		public Query repeat(int n) {
			StringBuffer nrecord = new StringBuffer(record.length() * n);
			List<Long> ngroups = new ArrayList<>(groups.size() * n);
			for (int i = 0; i < n; i++) {
				nrecord.append(record);
				ngroups.addAll(groups);
			}
			return new Query(nrecord.toString(), ngroups);
		}

	}

}
