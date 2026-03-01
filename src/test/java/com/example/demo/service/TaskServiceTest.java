package com.example.demo.service;

import com.example.demo.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskServiceTest {

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
    }

    @Test
    void create_assignsIdAndStoresTask() {
        Task task = new Task();
        task.setTitle("Write tests");
        task.setDescription("Ensure 80% coverage");

        Task created = taskService.create(task);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Write tests");
    }

    @Test
    void create_incrementsIdForEachTask() {
        Task first = taskService.create(taskWithTitle("First"));
        Task second = taskService.create(taskWithTitle("Second"));

        assertThat(second.getId()).isGreaterThan(first.getId());
    }

    @Test
    void findById_returnsTask_whenExists() {
        Task created = taskService.create(taskWithTitle("Find me"));

        Task found = taskService.findById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Find me");
    }

    @Test
    void findById_returnsNull_whenNotExists() {
        assertThat(taskService.findById(999L)).isNull();
    }

    @Test
    void deleteById_returnsTrue_andRemovesTask_whenExists() {
        Task created = taskService.create(taskWithTitle("Delete me"));

        boolean result = taskService.deleteById(created.getId());

        assertThat(result).isTrue();
        assertThat(taskService.findById(created.getId())).isNull();
    }

    @Test
    void deleteById_returnsFalse_whenNotExists() {
        assertThat(taskService.deleteById(999L)).isFalse();
    }

    @Test
    void findAll_returnsAllCreatedTasks() {
        taskService.create(taskWithTitle("Task A"));
        taskService.create(taskWithTitle("Task B"));

        assertThat(taskService.findAll()).hasSize(2);
    }

    @Test
    void update_modifiesExistingTask() {
        Task created = taskService.create(taskWithTitle("Original"));

        Task changes = new Task();
        changes.setTitle("Updated");
        changes.setDescription("New description");

        Task result = taskService.update(created.getId(), changes);

        assertThat(result.getTitle()).isEqualTo("Updated");
        assertThat(result.getDescription()).isEqualTo("New description");
    }

    @Test
    void update_returnsNull_whenNotExists() {
        assertThat(taskService.update(999L, new Task())).isNull();
    }

    // -----------------------------------------------------------------------

    private Task taskWithTitle(String title) {
        Task t = new Task();
        t.setTitle(title);
        return t;
    }
}
