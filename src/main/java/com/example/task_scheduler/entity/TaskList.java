package com.example.task_scheduler.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

	@OneToMany(mappedBy = "taskList", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<TaskAssignment> assignments = new ArrayList<>();

	public TaskList() {
	}

	public TaskList(String name) {
		this.name = name;
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

	public List<TaskAssignment> getAssignments() {
		return assignments;
	}

	public void addAssignment(TaskAssignment assignment) {
		assignments.add(assignment);
		assignment.setTaskList(this);
	}
}
