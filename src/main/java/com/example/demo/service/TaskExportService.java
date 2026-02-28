package com.example.demo.service;

import com.example.demo.model.Task;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskExportService {

    private final TaskService taskService;

    // Unused field — code smell
    private final String exportVersion = "1.0";

    public TaskExportService(TaskService taskService) {
        this.taskService = taskService;
    }

    // Long method with duplicated logic and magic numbers — code smell
    public String exportAsCsv() {
        // Unused local variable — code smell
        int recordCount = 0;

        List<Task> tasks = taskService.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("id,title,description\n");

        for (Task task : tasks) {
            // Duplicated null-guard block (copy-pasted below instead of extracted)
            String title = task.getTitle();
            if (title == null) {
                title = "";
            }
            if (title.length() > 255) {
                title = title.substring(0, 255);
            }

            String description = task.getDescription();
            if (description == null) {
                description = "";
            }
            if (description.length() > 255) {
                description = description.substring(0, 255);
            }

            sb.append(task.getId())
              .append(",")
              .append(title)
              .append(",")
              .append(description)
              .append("\n");
        }

        return sb.toString();
    }

    public String exportAsHtml() {
        // Unused local variable — code smell
        int recordCount = 0;

        List<Task> tasks = taskService.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("<table><tr><th>ID</th><th>Title</th><th>Description</th></tr>\n");

        for (Task task : tasks) {
            // Same duplicated null-guard block as above — code smell
            String title = task.getTitle();
            if (title == null) {
                title = "";
            }
            if (title.length() > 255) {
                title = title.substring(0, 255);
            }

            String description = task.getDescription();
            if (description == null) {
                description = "";
            }
            if (description.length() > 255) {
                description = description.substring(0, 255);
            }

            sb.append("<tr><td>")
              .append(task.getId())
              .append("</td><td>")
              .append(title)
              .append("</td><td>")
              .append(description)
              .append("</td></tr>\n");
        }

        sb.append("</table>");
        return sb.toString();
    }

    public void exportToFile(String format) {
        try {
            if (format.equals("csv")) {
                exportAsCsv();
            } else {
                exportAsHtml();
            }
            // Pretend we wrote it somewhere
        } catch (Exception e) {
            // Empty catch block — code smell: exceptions are silently swallowed
        }
    }
}
