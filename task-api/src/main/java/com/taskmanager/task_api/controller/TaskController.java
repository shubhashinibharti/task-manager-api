package com.taskmanager.task_api.controller;

import com.taskmanager.task_api.dto.TaskResponse;
import com.taskmanager.task_api.entity.Task;
import com.taskmanager.task_api.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<TaskResponse> getAllTask(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size){
        return taskService.getAllTasks(page,size);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse  getTaskById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse  createTask(@Valid @RequestBody Task task){
        return taskService.createTask(task);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
    }
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse  updatePartialTask(@PathVariable Long id,@RequestBody Task task){
        return taskService.patchTask(id,task);
    }
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse updateFullTask(@PathVariable Long id, @RequestBody Task task){
        return taskService.updateFullTask(id,task);
    }
}
