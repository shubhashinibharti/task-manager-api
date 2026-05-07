package com.taskmanager.task_api.repository;

import com.taskmanager.task_api.entity.AppUser;
import com.taskmanager.task_api.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByAssignedUser(AppUser user, Pageable pageable);
    List<Task> findByAssignedUser(AppUser user);
}
