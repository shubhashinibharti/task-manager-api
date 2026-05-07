package com.taskmanager.task_api.service;

import com.taskmanager.task_api.dto.TaskResponse;
import com.taskmanager.task_api.entity.AppUser;
import com.taskmanager.task_api.entity.Task;
import com.taskmanager.task_api.repository.TaskRepository;
import com.taskmanager.task_api.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;


@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Spring injects both repositories via constructor (Dependency Injection)
    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    // Reads logged-in username from JWT token stored in SecurityContextHolder
    // Then fetches the full AppUser object from DB
    private AppUser getLoggedInUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    // Converts Task entity → TaskResponse DTO
    // Prevents exposing internal entity fields to client
    // Only sends what client needs: id, title, description, status, assignedTo (username only)
    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getAssignedUser().getUsername()
        );
    }

    // ─── PUBLIC SERVICE METHODS ───────────────────────────────────────────────

    // Returns only tasks belonging to the logged-in user
    // Each Task converted to TaskResponse DTO before returning
    public Page<TaskResponse> getAllTasks(int page, int size) {
        AppUser user = getLoggedInUser();
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByAssignedUser(user, pageable)
                .map(this::toResponse);
    }

    // Creates a new task and assigns it to the logged-in user automatically
    public TaskResponse createTask(Task task) {
        task.setAssignedUser(getLoggedInUser());
        return toResponse(taskRepository.save(task));
    }

    // Finds a single task by ID — throws 404 if not found
    public TaskResponse findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id));
        return toResponse(task);
    }

    // Deletes task by ID — validates existence first, returns 404 if not found
    public void deleteTask(Long id) {
        taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id));
        taskRepository.deleteById(id);
    }

    // PATCH — updates only the fields that are NOT null in the request
    // Fields the client didn't send stay unchanged in DB
    public TaskResponse patchTask(Long id, Task updateTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id));

        if (updateTask.getTitle() != null) {
            existingTask.setTitle(updateTask.getTitle());
        }
        if (updateTask.getDescription() != null) {
            existingTask.setDescription(updateTask.getDescription());
        }
        if (updateTask.getStatus() != null) {
            existingTask.setStatus(updateTask.getStatus());
        }

        return toResponse(taskRepository.save(existingTask));
    }

    // PUT — replaces ALL fields regardless of null (full replacement)
    public TaskResponse updateFullTask(Long id, Task updateTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id));

        existingTask.setTitle(updateTask.getTitle());
        existingTask.setDescription(updateTask.getDescription());
        existingTask.setStatus(updateTask.getStatus());

        return toResponse(taskRepository.save(existingTask));
    }
}
