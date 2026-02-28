package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void post_tasks_returns200_withCreatedTask() throws Exception {
        Task saved = new Task();
        saved.setId(1L);
        saved.setTitle("New task");
        saved.setDescription("Some description");

        when(taskService.create(any())).thenReturn(saved);

        Task request = new Task();
        request.setTitle("New task");
        request.setDescription("Some description");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New task"));
    }

    @Test
    void get_tasks_id_returns200_whenFound() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Found task");

        when(taskService.findById(1L)).thenReturn(task);

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Found task"));
    }

    @Test
    void get_tasks_id_returns404_whenNotFound() throws Exception {
        when(taskService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_tasks_id_returns204_whenDeleted() throws Exception {
        when(taskService.deleteById(1L)).thenReturn(true);

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_tasks_id_returns404_whenNotFound() throws Exception {
        when(taskService.deleteById(99L)).thenReturn(false);

        mockMvc.perform(delete("/tasks/99"))
                .andExpect(status().isNotFound());
    }
}
