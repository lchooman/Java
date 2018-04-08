import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.time.Instant;
import java.time.Duration;
import java.util.Random;

/*
    A program to process a list in parallel using Java 8 ForkJoinPool.
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

public class ExecutorForkJoin extends RecursiveTask<Set<Long>> {

	private static final int THREADS = 4;
	private static final int MAX_ELEMENTS = 15;

	private static ForkJoinPool forkJoinPool = new ForkJoinPool(THREADS);

	private List<Long> elementList = new ArrayList<>();

	private Long execute(List<Long> elements) {
		elements.forEach(e -> {
			try {
				Thread.sleep(200);
				System.out.printf("I am %1$d in thread %2$s, thread id: %3$d!\n", e, Thread.currentThread().getName(), Thread.currentThread().getId());
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		});
		return Thread.currentThread().getId();
	}

	@Override
	protected Set<Long> compute() {
		Set<Long> resultSet = new HashSet<>();
		if (this.elementList.size() <= (MAX_ELEMENTS + THREADS - 1) / THREADS) {
			resultSet.add(execute(elementList));
		} else {
			int elementListLen = elementList.size(), splitIndex = (elementListLen + 1) / 2;
			Collection<ForkJoinTask<Set<Long>>> forkJoinTasks;
			forkJoinTasks = invokeAll(Arrays.asList(new ExecutorForkJoin(elementList.subList(0, splitIndex)), new ExecutorForkJoin(elementList.subList(splitIndex, elementListLen))));
			forkJoinTasks.forEach(forkJoinTask -> resultSet.addAll(forkJoinTask.join()));
		}
		return resultSet;
	}

	public ExecutorForkJoin(List<Long> elementList) {
		this.elementList.addAll(elementList);
	}

	public ForkJoinPool getForkJoinPool() {
		return forkJoinPool;
	}

	public static void main (String [] params) throws Exception {
		Instant start, end;
		Random random = new Random();
		ExecutorForkJoin executorForkJoin;

		List<Long> elementList = new ArrayList<>();
		Set<Long> resultSet;
		IntStream.range(0, MAX_ELEMENTS).forEach(i -> {
			elementList.add((long) random.nextInt());
		});
		executorForkJoin = new ExecutorForkJoin(elementList);

		try {
			start = Instant.now();
			resultSet = executorForkJoin.getForkJoinPool().invoke(executorForkJoin);
			end = Instant.now();
		} finally {
			executorForkJoin.getForkJoinPool().shutdown();
			executorForkJoin.getForkJoinPool().awaitTermination(1, TimeUnit.SECONDS);
		}
		resultSet.forEach(l -> {
			System.out.printf("Future thread id: %1$d\n", l);
		});
		System.out.printf("Execution time (millis): %1$d\n", Duration.between(start, end).toMillis());
	}
}
