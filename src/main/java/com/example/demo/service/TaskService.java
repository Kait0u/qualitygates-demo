package com.example.demo.service;

import com.example.demo.model.Task;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final Map<Long, Task> tasks = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Task create(Task task) {
        long id = idGenerator.getAndIncrement();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    public Task findById(Long id) {
        return tasks.get(id);
    }

    public boolean deleteById(Long id) {
        return tasks.remove(id) != null;
    }
}
