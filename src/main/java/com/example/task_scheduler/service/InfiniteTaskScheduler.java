package com.example.task_scheduler.service;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * Fair infinite rotation over leaf tasks: virtual time advances by {@code duration / weight} per
 * occurrence, matching the Python {@code infinite_scheduler} heap behaviour (including periodic
 * virtual-time renormalisation every 1000 steps).
 */
public final class InfiniteTaskScheduler {

	private final PriorityQueue<HeapEntry> queue = new PriorityQueue<>();
	private final Map<UUID, Double> globalWeights;
	private final Map<UUID, Double> minDurations;
	private int iterationCount;

	public InfiniteTaskScheduler(Map<UUID, Double> globalWeights, Map<UUID, Double> minDurations) {
		this.globalWeights = Map.copyOf(globalWeights);
		this.minDurations = Map.copyOf(minDurations);
		for (Map.Entry<UUID, Double> e : this.globalWeights.entrySet()) {
			if (e.getValue() != null && e.getValue() > 0) {
				queue.add(new HeapEntry(0.0, e.getKey()));
			}
		}
	}

	public record Step(UUID taskId, double duration) {
	}

	public Step next() {
		HeapEntry head = queue.poll();
		if (head == null) {
			throw new IllegalStateException("No tasks with positive global weight");
		}
		UUID taskId = head.taskId();
		double virtualTime = head.virtualTime();

		double duration = minDurations.getOrDefault(taskId, 0.0);
		double weight = globalWeights.get(taskId);
		double timeIncrement = duration / weight;
		queue.add(new HeapEntry(virtualTime + timeIncrement, taskId));

		iterationCount++;
		if (iterationCount >= 1000 && !queue.isEmpty()) {
			double minVtime = queue.peek().virtualTime();
			PriorityQueue<HeapEntry> next = new PriorityQueue<>();
			for (HeapEntry e : queue) {
				next.add(new HeapEntry(e.virtualTime() - minVtime, e.taskId()));
			}
			queue.clear();
			queue.addAll(next);
			iterationCount = 0;
		}

		return new Step(taskId, duration);
	}

	private record HeapEntry(double virtualTime, UUID taskId) implements Comparable<HeapEntry> {
		@Override
		public int compareTo(HeapEntry o) {
			int c = Double.compare(virtualTime, o.virtualTime());
			if (c != 0) {
				return c;
			}
			return taskId.compareTo(o.taskId());
		}
	}
}
