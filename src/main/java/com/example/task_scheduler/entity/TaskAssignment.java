package com.example.task_scheduler.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "task_assignments")
public class TaskAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "task_list_id")
	private TaskList taskList;

	@ManyToOne(optional = false)
	@JoinColumn(name = "task_id")
	private Task task;

	/** Share of the parent list when explicit mode is used, e.g. 50.0 means 50%. */
	private double percentage;

	/** Ordering within the parent list (implicit geometric / equal modes rely on order). */
	private int assignmentOrder;

	public TaskAssignment() {
	}

	public TaskAssignment(Task task, double percentage) {
		this.task = task;
		this.percentage = percentage;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public TaskList getTaskList() {
		return taskList;
	}

	public void setTaskList(TaskList taskList) {
		this.taskList = taskList;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	public int getAssignmentOrder() {
		return assignmentOrder;
	}

	public void setAssignmentOrder(int assignmentOrder) {
		this.assignmentOrder = assignmentOrder;
	}
}
