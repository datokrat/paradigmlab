package de.paulr.math;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;

public class InnerTriangulationExperiment {

	public static void main(String[] args) {
		var fam000 = buildFamily("011", 2000);
		// var fam011 = buildFamily("011", 1000);
		System.out.println(fam000);
		// System.out.println(fam011);
//		System.out.println(
//			fam000.stream().collect(Collectors.groupingBy(String::length, Collectors.counting())));

//		System.out.println(fam000.stream().mapToInt(String::length).max());
//		System.out.println(
//			fam000.stream().filter(x -> x.matches("(0011)*")).map(String::length).toList());

//		for (var x : fam000) {
//			if (fam011.contains(x)) {
//				System.out.println("collision");
//			}
//		}
//		System.out.println(Stream.concat(fam000.stream(), fam011.stream())
//			.sorted(Comparator.comparing(String::length).thenComparing(x -> x)).toList());
	}

	private static Set<String> buildFamily(String start, int maxSize) {
		Set<String> derivedWords = new LinkedHashSet<String>();
		PriorityQueue<String> queue = new PriorityQueue<>(
			Comparator.comparing(String::length).thenComparing(x -> x));
		queue.add(start);
		while (derivedWords.size() < maxSize && !queue.isEmpty()) {
			String word = queue.poll();
			if (derivedWords.contains(word)) {
				continue;
			}
			derivedWords.add(word);
			forEachDerivative(word, derivative -> {
				if (derivative.length() > 2) {
					queue.add(toUniqueRepresentant(derivative));
				}
			});
		}
		return derivedWords;
	}

	private static void forEachDerivative(String word, Consumer<String> consumer) {
		for (int i = 0; i < word.length(); ++i) {
			CharSequence rotated = rotate(word, i);
			if (rotated.length() >= 2) {
				consumer.accept(toggle(rotated.charAt(0)) + "0" + toggle(rotated.charAt(1))
					+ rotated.subSequence(2, rotated.length()).toString());
			}
			if (rotated.length() >= 3 && rotated.charAt(1) == '0') {
				consumer.accept(toggle(rotated.charAt(0)) + "" + toggle(rotated.charAt(2))
					+ rotated.subSequence(3, rotated.length()).toString());
				consumer.accept(toggle(rotated.charAt(0)) + "11" + toggle(rotated.charAt(2))
					+ rotated.subSequence(3, rotated.length()).toString());
			}
			if (rotated.length() >= 3 && rotated.charAt(1) == '1') {
				consumer.accept(toggle(rotated.charAt(0)) + "1" + toggle(rotated.charAt(2))
					+ rotated.subSequence(3, rotated.length()).toString());
				consumer.accept(rotated.charAt(0) + "111" + rotated.charAt(2)
					+ rotated.subSequence(3, rotated.length()).toString());
			}
		}
	}

	private static char toggle(char character) {
		if (character == '0') {
			return '1';
		} else {
			return '0';
		}
	}

	private static String toUniqueRepresentant(String input) {
		String s1 = toLeastRotation(input);
		String s2 = toLeastRotation(flip(input).toString());
		return s1.compareTo(s2) <= 0 ? s1 : s2;
	}

	private static String toLeastRotation(String input) {
		String leastRotationYet = input;
		for (int i = 1; i < input.length(); ++i) {
			String rotation = rotate(input, i).toString();
			if (rotation.compareTo(leastRotationYet) < 0) {
				leastRotationYet = rotation;
			}
		}
		return leastRotationYet;
	}

	private static CharSequence rotate(CharSequence s, int rotation) {
		rotation = rotation % s.length();
		var first = s.subSequence(0, rotation);
		var second = s.subSequence(rotation, s.length());
		return concat(second, first);
	}

	private static CharSequence flip(CharSequence s) {
		return new MirrorCharSequence(s);
	}

	private static CharSequence concat(CharSequence s1, CharSequence s2) {
		return new ConcatCharSequence(s1, s2);
	}

	private static class MirrorCharSequence implements CharSequence {

		private CharSequence input;

		public MirrorCharSequence(CharSequence input) {
			this.input = input;
		}

		@Override
		public int length() {
			return input.length();
		}

		@Override
		public char charAt(int index) {
			return input.charAt(input.length() - 1 - index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new MirrorCharSequence(
				input.subSequence(input.length() - end, input.length() - start));
		}

		@Override
		public String toString() {
			return new StringBuilder(this).toString();
		}

	}

	private static class ConcatCharSequence implements CharSequence {

		private CharSequence left;
		private CharSequence right;

		public ConcatCharSequence(CharSequence left, CharSequence right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public int length() {
			return left.length() + right.length();
		}

		@Override
		public char charAt(int index) {
			if (index < left.length()) {
				return left.charAt(index);
			} else {
				return right.charAt(index - left.length());
			}
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			if (end <= left.length()) {
				return left.subSequence(start, end);
			} else if (start >= left.length()) {
				return right.subSequence(start - left.length(), end - left.length());
			} else {
				return new ConcatCharSequence(left.subSequence(start, left.length()),
					right.subSequence(0, end - left.length()));
			}
		}

		@Override
		public String toString() {
			return left.toString() + right.toString();
		}

	}

}
