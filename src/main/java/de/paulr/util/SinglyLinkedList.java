package de.paulr.util;

import java.util.Optional;

public sealed class SinglyLinkedList<T> permits SinglyLinkedList.Nil, SinglyLinkedList.Cons {

	public static <T> SinglyLinkedList<T> of(Iterable<T> list) {
		SinglyLinkedList<T> result = new SinglyLinkedList<>();
		for (var item : list) {
			result = result.push(item);
		}
		return result;
	}

	public boolean isEmpty() {
		return this instanceof Nil<T>;
	}

	public T getHead() {
		return ((Cons<T>) this).head;
	}

	public SinglyLinkedList<T> getTail() {
		return ((Cons<T>) this).tail;
	}

	public Optional<Cons<T>> tryGetCons() {
		if (this instanceof Cons<T> cons) {
			return Optional.of(cons);
		} else {
			return Optional.empty();
		}
	}

	public Optional<SinglyLinkedList<T>> tryGetTail() {
		if (this instanceof Cons<T>) {
			return Optional.of(getTail());
		} else {
			return Optional.empty();
		}
	}

	public static <T> SinglyLinkedList<T> empty() {
		return new Nil<>();
	}

	public SinglyLinkedList<T> push(T head) {
		return new Cons<>(head, this);
	}

	public static final class Nil<T> extends SinglyLinkedList<T> {
	}

	public static final class Cons<T> extends SinglyLinkedList<T> {

		private T head;
		private SinglyLinkedList<T> tail;

		public Cons(T head, SinglyLinkedList<T> tail) {
			this.head = head;
			this.tail = tail;
		}

	}

}
