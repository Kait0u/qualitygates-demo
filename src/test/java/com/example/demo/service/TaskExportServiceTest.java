package com.example.demo.service;

import com.example.demo.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskExportServiceTest {

    private TaskService taskService;
    private TaskExportService taskExportService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
        taskExportService = new TaskExportService(taskService);
    }

    @Test
    void exportAsCsv_containsHeader() {
        assertThat(taskExportService.exportAsCsv()).startsWith("id,title,description");
    }

    @Test
    void exportAsCsv_containsTaskData() {
        Task task = new Task();
        task.setTitle("My Task");
        task.setDescription("Details");
        taskService.create(task);

        String csv = taskExportService.exportAsCsv();

        assertThat(csv).contains("My Task").contains("Details");
    }

    @Test
    void exportAsHtml_containsTableStructure() {
        assertThat(taskExportService.exportAsHtml()).contains("<table>").contains("</table>");
    }

    @Test
    void exportAsHtml_containsTaskData() {
        Task task = new Task();
        task.setTitle("HTML Task");
        task.setDescription("HTML Details");
        taskService.create(task);

        assertThat(taskExportService.exportAsHtml()).contains("HTML Task");
    }

    @Test
    void exportToFile_doesNotThrow() {
        taskExportService.exportToFile("csv");
        taskExportService.exportToFile("html");
    }
}
