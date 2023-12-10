package de.paulr.util;

import static java.util.stream.Collectors.summarizingLong;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

public class Stopwatch {

	private long referenceTime = System.currentTimeMillis();
	private List<Long> statistics = new ArrayList<>();

	public LongSummaryStatistics summarize() {
		return statistics.stream().collect(summarizingLong(x -> x));
	}

	public long recordAndReset() {
		long elapsed = elapsedMillis();
		statistics.add(elapsed);
		reset();
		return elapsed;
	}

	public long elapsedMillis() {
		return System.currentTimeMillis() - referenceTime;
	}

	public void reset() {
		referenceTime = System.currentTimeMillis();
	}

}
