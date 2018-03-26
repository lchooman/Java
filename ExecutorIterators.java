import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.time.Instant;
import java.time.Duration;
import java.util.Random;
import java.util.function.Function;

/*
    A program to process an iterator using multiple threads in a thread safe manner.
    Copyright (C) 2018 Lewis Tat Fong Choo Man

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses/gpl.html.
 */

public class ExecutorIterators {

	private static final int THREADS = 4;
	private static final int MAX_ELEMENTS = 15;

	private ExecutorService executorService = Executors.newFixedThreadPool(THREADS);

	private List<Callable<Long>> callableList;

	private List<Long> elementList = new ArrayList<>();

	public Long execute(Iterator<Long> elements) {
		elements.forEachRemaining(e -> {
			try {
				Thread.sleep(200);
				System.out.printf("I am %1$d in thread %2$s, thread id: %3$d!\n", e, Thread.currentThread().getName(), Thread.currentThread().getId());
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		});
		return Thread.currentThread().getId();
	}

	private class IteratorValue<T> {
		private T value;
		private Boolean hasNext;

		public T getValue() {
			return value;
		}

		public Boolean getHasNext() {
			return hasNext;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public void setHasNext(Boolean hasNext) {
			this.hasNext = hasNext;
		}
	}

	public class SynchronizedIterator<T> implements Iterator<T> {
		private Iterator<T> sourceIterator;
		private InheritableThreadLocal<IteratorValue<T>> iteratorValue = new InheritableThreadLocal<IteratorValue<T>>() {
			protected IteratorValue<T> initialValue() {
				IteratorValue<T> iteratorValue = new IteratorValue<>();
				iteratorValue.setHasNext(null);
				iteratorValue.setValue(null);
				return iteratorValue;
			}
		};

		public SynchronizedIterator(Iterator<T> sourceIterator) {
			this.sourceIterator = sourceIterator;
		}

		@Override
		public boolean hasNext() {
			if (iteratorValue.get().getHasNext() == null) {
				synchronized(this) {
					if (sourceIterator.hasNext()) {
						iteratorValue.get().setValue(sourceIterator.next());
						iteratorValue.get().setHasNext(true);
					} else {
						iteratorValue.get().setValue(null);
						iteratorValue.get().setHasNext(false);
					}
				}
			}

			return iteratorValue.get().getHasNext();
		}

		@Override
		public T next() {
			if (hasNext()) {
				iteratorValue.get().setHasNext(null);
				return iteratorValue.get().getValue();
			} else {
				throw new NoSuchElementException("No element left");
			}
		}
	}

	public <T, R> List<Callable<R>> getCallables(int numThreads, List<T> sourceList, Function<Iterator<T>, R> func) {
		List<Callable<R>> callables = new ArrayList<>();
		Iterator<T> sourceIterator = new SynchronizedIterator<>(sourceList.iterator());

		IntStream.range(0, numThreads).forEach(i -> {
			callables.add(() -> {
				return func.apply(sourceIterator);
			});
		});
		return callables;
	}

	public ExecutorIterators() {
		Random random = new Random();
		IntStream.range(0, MAX_ELEMENTS).forEach(i -> {
			elementList.add((long) random.nextInt());
		});
		callableList = getCallables(THREADS, elementList, this::execute);
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public List<Callable<Long>> getCallableList() {
		return callableList;
	}

	public static void main (String [] params) throws Exception {
		ExecutorIterators executorIterators = new ExecutorIterators();
		ExecutorService executorService = executorIterators.getExecutorService();
		List<Callable<Long>> callableList = executorIterators.getCallableList();
		List<Future<Long>> futureList = new ArrayList<>();
		Instant start, end;

		start = Instant.now();
		try {
			futureList = executorService.invokeAll(callableList);

			futureList.forEach(future -> {
				try {
					System.out.printf("Future thread id: %1$d\n", future.get());
				} catch (ExecutionException | InterruptedException ex) {
					ex.printStackTrace();
				}
			});
			end = Instant.now();
		} finally {
			executorService.shutdown();
			executorService.awaitTermination(1, TimeUnit.SECONDS);
		}
		System.out.printf("Execution time (millis): %1$d\n", Duration.between(start, end).toMillis());
	}
}
