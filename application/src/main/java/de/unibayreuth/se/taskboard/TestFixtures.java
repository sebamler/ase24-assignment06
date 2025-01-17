package de.unibayreuth.se.taskboard;

import de.unibayreuth.se.taskboard.business.domain.Task;
import de.unibayreuth.se.taskboard.business.domain.User;
import de.unibayreuth.se.taskboard.business.ports.UserService;
import de.unibayreuth.se.taskboard.business.ports.TaskService;
import org.apache.commons.lang3.SerializationUtils;

import java.util.List;
import java.util.stream.Collectors;

public class TestFixtures {
    private static final List<User> USERS = List.of(
            new User("Alice"),
            new User("Bob"),
            new User("Charlie")
    );

    private static final List<Task> TASKS = List.of(
            new Task("Task 1", "Description 1"),
            new Task("Task 2", "Description 2"),
            new Task("Task 3", "Description 3")
    );

    public static List<User> getUsers() {
        return USERS.stream()
                .map(SerializationUtils::clone) // prevent issues when tests modify the fixture objects
                .toList();
    }

    public static List<Task> getTasks() {
        return TASKS.stream()
                .map(SerializationUtils::clone) // prevent issues when tests modify the fixture objects
                .toList();
    }

    public static List<User> createUsers(UserService userService) {
        return USERS.stream()
                .map(SerializationUtils::clone) // prevent issues when tests modify the fixture objects
                .map(userService::create)
                .collect(Collectors.toList());
    }

    public static List<Task> createTasks(TaskService taskService) {
        return TASKS.stream()
                .map(SerializationUtils::clone) // prevent issues when tests modify the fixture objects
                .map(taskService::create)
                .collect(Collectors.toList());
    }
}
