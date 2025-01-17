package de.unibayreuth.se.taskboard.api.controller;

import de.unibayreuth.se.taskboard.api.dtos.TaskDto;
import de.unibayreuth.se.taskboard.api.mapper.TaskDtoMapper;
import de.unibayreuth.se.taskboard.business.domain.TaskStatus;
import de.unibayreuth.se.taskboard.business.exceptions.MalformedRequestException;
import de.unibayreuth.se.taskboard.business.exceptions.TaskNotFoundException;
import de.unibayreuth.se.taskboard.business.exceptions.UserNotFoundException;
import de.unibayreuth.se.taskboard.business.ports.TaskService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@OpenAPIDefinition(
        info = @Info(
                title = "TaskBoard",
                version = "0.0.1"
        )
)
@Tag(name = "Tasks")
@Controller
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final TaskDtoMapper taskDtoMapper;

    @Operation(
            summary = "Get all tasks.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = TaskDto.class)
                            ),
                            description = "All tasks as a JSON array."
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<TaskDto>> getAll() {
        return ResponseEntity.ok(
                taskService.getAll().stream()
                        .map(taskDtoMapper::fromBusiness)
                        .toList()
        );
    }

    @Operation(
            summary = "Get task by ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TaskDto.class)
                            ),
                            description = "The task with the provided ID as a JSON object."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "No task with the provided ID could not be found."
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(
                    taskDtoMapper.fromBusiness(taskService.getById(id))
            );
        } catch (TaskNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @Operation(
            summary = "Get tasks by status.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = TaskDto.class)
                            ),
                            description = "The tasks with the provided status as a JSON array."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "No task with the provided ID could not be found."
                    )
            }
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDto>> getByStatus(@PathVariable TaskStatus status) {
        return ResponseEntity.ok(
                taskService.getByStatus(status).stream()
                        .map(taskDtoMapper::fromBusiness)
                        .toList()
        );
    }

    @Operation(
            summary = "Get tasks by assignee.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = TaskDto.class)
                            ),
                            description = "The tasks with the provided status as a JSON array."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "No task with the provided ID could not be found."
                    )
            }
    )
    @GetMapping("/assignee/{userId}")
    public ResponseEntity<List<TaskDto>> getByStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                taskService.getByAssignee(userId).stream()
                        .map(taskDtoMapper::fromBusiness)
                        .toList()
        );
    }

    @Operation(
            summary = "Creates a new task.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TaskDto.class)
                            ),
                            description = "The new task as a JSON object."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ID present or user with the provided user ID could not be found."
                    )
            }
    )
    @PostMapping
    public ResponseEntity<TaskDto> create(@RequestBody @Valid TaskDto taskDto) {
        try {
            return ResponseEntity.ok(
                    taskDtoMapper.fromBusiness(
                            taskService.create(
                                    taskDtoMapper.toBusiness(taskDto)
                            )
                    )
            );
        } catch (MalformedRequestException | UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @Operation(
            summary = "Updates an existing task.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TaskDto.class)
                            ),
                            description = "The updated task as a JSON object."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "IDs do not match or no task or user with the provided IDs could be found."
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> update(@PathVariable UUID id, @RequestBody @Valid TaskDto taskDto) {
        if (!id.equals(taskDto.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID in path and body do not match.");
        }

        try {
            return ResponseEntity.ok(
                    taskDtoMapper.fromBusiness(
                            taskService.upsert(
                                    taskDtoMapper.toBusiness(taskDto)
                            )
                    )
            );
        } catch (TaskNotFoundException | UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @Operation(
            summary = "Delete a task.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The task with the provided ID was successfully deleted."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "No task with the provided ID could be found."
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        try {
            taskService.delete(id);
            return ResponseEntity.ok().build();
        } catch (TaskNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }

    }
}
