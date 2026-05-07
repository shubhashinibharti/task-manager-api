package com.taskmanager.task_api.entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  //@Column(name = "id", nullable = false)

    @NotBlank(message = "Title cannot be empty")
    private String title;

    private String description;

    @NotBlank(message = "Status cannot be empty")
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id") // creates column named "user_id" in tasks table// creates column named "user_id" in tasks table
    private AppUser assignedUser;
}
