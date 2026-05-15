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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
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
        AppUser assignedUser = task.getAssignedUser();
        String userName = "Unknown";
        if (assignedUser != null){
            userName = assignedUser.getUsername();
        }
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                userName
        );
    }

    // ─── PUBLIC SERVICE METHODS ───────────────────────────────────────────────

    // Returns only tasks belonging to the logged-in user
    // Each Task converted to TaskResponse DTO before returning
    public Page<TaskResponse> getAllTasks(int page, int size) {
        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        PageRequest pageRequest=PageRequest.of(page, size);
        if(isAdmin){
            Page<TaskResponse> result =  taskRepository.findAll(pageRequest)
                    .map(this::toResponse);
            log.info("Admin fetched all {} tasks", result.getTotalElements());
            return result;
        }
        else {
            AppUser user = getLoggedInUser();
            Page<TaskResponse> result = taskRepository.findByAssignedUser(user, pageRequest)
                    .map(this::toResponse);
            log.info("Fetched {} tasks for user: {}", result.getTotalElements(), user.getUsername());
            return result;
        }
    }

    // Creates a new task and assigns it to the logged-in user automatically
    public TaskResponse createTask(Task task) {
        AppUser user = getLoggedInUser();
        task.setAssignedUser(user);
        TaskResponse response = toResponse(taskRepository.save(task));
        log.info("Task created: '{}' by user: {}", task.getTitle(), user.getUsername());
        return response;
    }

    // Finds a single task by ID — throws 404 if not found
    public TaskResponse findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found: id={}", id);
                    return new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id);
                });
        return toResponse(task);
    }

    // Deletes task by ID — validates existence first, returns 404 if not found
    public void deleteTask(Long id) {
        AppUser user = getLoggedInUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete failed - task not found: : id={}, user={}", id, user.getUsername());
                    return new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id);
                });
        taskRepository.deleteById(id);
        log.info("Task deleted: id={}, title='{}', user={}",  id, task.getTitle(), user.getUsername());
    }

    // PATCH — updates only the fields that are NOT null in the request
    // Fields the client didn't send stay unchanged in DB
    public TaskResponse patchTask(Long id, Task updateTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Patch failed - task not found: id={}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Task not found with id: " + id);
                });

        if (updateTask.getTitle() != null) {
            existingTask.setTitle(updateTask.getTitle());
        }
        if (updateTask.getDescription() != null) {
            existingTask.setDescription(updateTask.getDescription());
        }
        if (updateTask.getStatus() != null) {
            existingTask.setStatus(updateTask.getStatus());
        }
        TaskResponse response =  toResponse(taskRepository.save(existingTask));
        log.info("Task patched: id={}, title='{}'", id, existingTask.getTitle());
        return response;
    }

    // PUT — replaces ALL fields regardless of null (full replacement)
    public TaskResponse updateFullTask(Long id, Task updateTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed - task not found: id={}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Task not found with id: " + id);
                });

        existingTask.setTitle(updateTask.getTitle());
        existingTask.setDescription(updateTask.getDescription());
        existingTask.setStatus(updateTask.getStatus());

        TaskResponse response =  toResponse(taskRepository.save(existingTask));
        log.info("Task updated: id={}, title='{}'", id, existingTask.getTitle());
        return response;
    }
}
