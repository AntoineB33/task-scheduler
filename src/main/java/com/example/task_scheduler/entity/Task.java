package com.example.task_scheduler.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String name;

	private double minDuration;

	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	@JsonIgnore
	private AppUser owner;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "child_list_id")
	@JsonIgnore
	private TaskList childList;

	public Task() {
	}

	public Task(String name, double minDuration, AppUser owner) {
		this.name = name;
		this.minDuration = minDuration;
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

	public double getMinDuration() {
		return minDuration;
	}

	public void setMinDuration(double minDuration) {
		this.minDuration = minDuration;
	}

	public AppUser getOwner() {
		return owner;
	}

	public void setOwner(AppUser owner) {
		this.owner = owner;
	}

	public TaskList getChildList() {
		return childList;
	}

	public void setChildList(TaskList childList) {
		this.childList = childList;
	}
}
