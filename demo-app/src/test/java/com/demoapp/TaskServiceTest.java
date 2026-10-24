package com.demoapp;

import com.demoapp.model.Task;
import com.demoapp.repository.TaskRepository;
import com.demoapp.service.TaskService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        taskService = new TaskService(taskRepository, meterRegistry);
    }

    @Test
    @DisplayName("Should create task and increment app_tasks_created_total counter")
    void testCreateTaskIncrementsMetric() {
        Task input = new Task("Test Title", "Description", "TODO", "HIGH");
        when(taskRepository.save(any(Task.class))).thenReturn(input);

        Task created = taskService.createTask(input);

        assertNotNull(created);
        assertEquals("Test Title", created.getTitle());
        verify(taskRepository, times(1)).save(input);

        // Verify micrometer counter incremented
        double counterValue = meterRegistry.get("app_tasks_created_total").counter().count();
        assertEquals(1.0, counterValue);
    }

    @Test
    @DisplayName("Should retrieve all tasks ordered by creation date")
    void testGetAllTasks() {
        List<Task> mockList = List.of(
                new Task("Task 1", "Desc 1", "TODO", "LOW"),
                new Task("Task 2", "Desc 2", "DONE", "HIGH")
        );
        when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(mockList);

        List<Task> results = taskService.getAllTasks();

        assertEquals(2, results.size());
        assertEquals("Task 1", results.get(0).getTitle());
    }

    @Test
    @DisplayName("Should filter tasks by status successfully")
    void testGetTasksByStatus() {
        List<Task> inProgressTasks = List.of(new Task("Deploy EC2", "Run Terraform", "IN_PROGRESS", "HIGH"));
        when(taskRepository.findByStatus("IN_PROGRESS")).thenReturn(inProgressTasks);

        List<Task> result = taskService.getTasksByStatus("IN_PROGRESS");

        assertEquals(1, result.size());
        assertEquals("Deploy EC2", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should update existing task fields correctly")
    void testUpdateTaskSuccess() {
        Task existing = new Task("Old Title", "Old Desc", "TODO", "LOW");
        existing.setId(1L);
        Task updateData = new Task("New Title", "New Desc", "IN_PROGRESS", "HIGH");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Task> updated = taskService.updateTask(1L, updateData);

        assertTrue(updated.isPresent());
        assertEquals("New Title", updated.get().getTitle());
        assertEquals("IN_PROGRESS", updated.get().getStatus());
        assertEquals("HIGH", updated.get().getPriority());
    }

    @Test
    @DisplayName("Should return empty optional when updating non-existent task")
    void testUpdateTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Task> updated = taskService.updateTask(999L, new Task("Title", "Desc", "DONE", "LOW"));

        assertTrue(updated.isEmpty());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete task when id exists")
    void testDeleteTask() {
        when(taskRepository.existsById(5L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(5L);

        boolean deleted = taskService.deleteTask(5L);

        assertTrue(deleted);
        verify(taskRepository, times(1)).deleteById(5L);
    }
}
