package com.example.task_scheduler.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "task_lists")
public class TaskList {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String name;

	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	@JsonIgnore
	private AppUser owner;

	/**
	 * When true, sibling weights are derived from {@link #decreaseFactor} and assignment order
	 * (equal split when factor is 1.0). When false, each assignment's {@link TaskAssignment#getPercentage()}
	 * is used (explicit shares).
	 */
	private boolean implicitWeights = true;

	/**
	 * Geometric ratio for implicit lists; ignored when {@link #implicitWeights} is false.
	 * When null and implicit, factor defaults to 1.0 (equal split).
	 */
	private Double decreaseFactor;

	@OneToMany(mappedBy = "taskList", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("assignmentOrder ASC")
	@JsonIgnore
	private List<TaskAssignment> assignments = new ArrayList<>();

	public TaskList() {
	}

	public TaskList(String name, AppUser owner) {
		this.name = name;
		this.owner = owner;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AppUser getOwner() {
		return owner;
	}

	public void setOwner(AppUser owner) {
		this.owner = owner;
	}

	public boolean isImplicitWeights() {
		return implicitWeights;
	}

	public void setImplicitWeights(boolean implicitWeights) {
		this.implicitWeights = implicitWeights;
	}

	public Double getDecreaseFactor() {
		return decreaseFactor;
	}

	public void setDecreaseFactor(Double decreaseFactor) {
		this.decreaseFactor = decreaseFactor;
	}

	public List<TaskAssignment> getAssignments() {
		return assignments;
	}

	public void addAssignment(TaskAssignment assignment) {
		assignment.setAssignmentOrder(assignments.size());
		assignments.add(assignment);
		assignment.setTaskList(this);
	}
}
