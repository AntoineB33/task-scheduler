package com.example.task_scheduler;

import com.example.task_scheduler.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TaskSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskSchedulerApplication.class, args);
	}

}
