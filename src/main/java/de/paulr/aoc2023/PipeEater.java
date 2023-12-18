package de.paulr.aoc2023;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import de.paulr.aoc2023.AoCUtil.Direction;

public class PipeEater {

	private PipeState prev = new TerminalPipeState(false);
	private PipeState state = new TerminalPipeState(false);

	public void next(PipePart part) {
		prev = state;
		state = state.next(part);
	}

	public void reset() {
		prev = new TerminalPipeState(false);
		state = new TerminalPipeState(false);
	}

	public boolean lastIsInner() {
		return prev.innerTileExpected();
	}

	public boolean lastIsPipe() {
		return prev.pipeExpected();
	}

	public boolean nextIsInner() {
		return state.innerTileExpected();
	}

	public boolean nextIsPipe() {
		return state.pipeExpected();
	}

	public enum PipePart {
		NONE('.', Set.of()), //
		VERTICAL('|', Set.of(Direction.UP, Direction.DOWN)), //
		HORIZONTAL('-', Set.of(Direction.LEFT, Direction.RIGHT)), //
		TOPLEFT('F', Set.of(Direction.RIGHT, Direction.DOWN)), //
		TOPRIGHT('7', Set.of(Direction.LEFT, Direction.DOWN)), //
		BOTTOMLEFT('L', Set.of(Direction.UP, Direction.RIGHT)), //
		BOTTOMRIGHT('J', Set.of(Direction.UP, Direction.LEFT));

		private static Map<Character, PipePart> charToPart = Arrays.stream(PipePart.values()) //
			.collect(toMap(PipePart::asChar, p -> p));

		private static Map<Set<Direction>, PipePart> endsToPart = Arrays.stream(PipePart.values()) //
			.collect(toMap(PipePart::getEnds, p -> p));

		private char character;
		private Set<Direction> ends;

		PipePart(char character, Set<Direction> ends) {
			this.character = character;
			this.ends = ends;
		}

		public boolean isPipe() {
			return this != NONE;
		}

		public char asChar() {
			return character;
		}

		public static PipePart ofChar(char character) {
			if (!charToPart.containsKey(character)) {
				throw new RuntimeException("Pipe part not found");
			}
			return charToPart.get(character);
		}

		public Set<Direction> getEnds() {
			return ends;
		}

		public static PipePart ofEnds(Set<Direction> ends) {
			if (!endsToPart.containsKey(ends)) {
				throw new RuntimeException("Pipe part not found");
			}
			return endsToPart.get(ends);
		}
	}

	public abstract sealed class PipeState permits TerminalPipeState, HorizontalPipeState {

		public abstract boolean innerTileExpected();

		public abstract boolean pipeExpected();

		public abstract PipeState next(PipePart part);

	}

	public final class TerminalPipeState extends PipeState {

		private boolean rightIsInner;

		public TerminalPipeState(boolean rightIsInner) {
			this.rightIsInner = rightIsInner;
		}

		@Override
		public boolean innerTileExpected() {
			return rightIsInner;
		}

		@Override
		public PipeState next(PipePart part) {
			return switch (part.asChar()) {
			case 'F' -> new HorizontalPipeState(rightIsInner);
			case 'L' -> new HorizontalPipeState(!rightIsInner);
			case '|' -> new TerminalPipeState(!rightIsInner);
			case '.' -> new TerminalPipeState(rightIsInner);
			case '-', 'J', '7' -> throw new RuntimeException();
			default -> throw new RuntimeException();
			};
		}

		@Override
		public boolean pipeExpected() {
			return false;
		}

	}

	public final class HorizontalPipeState extends PipeState {

		private boolean upIsInner;

		public HorizontalPipeState(boolean upIsInner) {
			this.upIsInner = upIsInner;
		}

		@Override
		public boolean innerTileExpected() {
			return false;
		}

		@Override
		public PipeState next(PipePart part) {
			return switch (part.asChar()) {
			case '-' -> this;
			case 'J' -> new TerminalPipeState(!upIsInner);
			case '7' -> new TerminalPipeState(upIsInner);
			case 'F', 'L', '|', '.' -> throw new RuntimeException();
			default -> throw new RuntimeException();
			};
		}

		@Override
		public boolean pipeExpected() {
			return true;
		}

	}

}
