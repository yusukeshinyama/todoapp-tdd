package com.example.todo

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TodoController(val todoRepository: TodoRepository) {

    @GetMapping("/api/todo/{userId}")
    fun getTodos(@PathVariable userId: String): List<TodoEntity> {
        return todoRepository.getTodos(userId)
    }

    @GetMapping("/api/todo/{userId}/{id}")
    fun getTodo1(@PathVariable userId: String, @PathVariable id: String): ResponseEntity<TodoEntity> {
        try {
            return ResponseEntity(todoRepository.getTodo1(userId, id), HttpStatus.OK)
        } catch (_: NoSuchElementException) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping("/api/todo/{userId}")
    fun postTodo(@PathVariable userId: String, @RequestBody req: TodoRequest): String {
        return todoRepository.addTodo(userId, req)
    }

    @DeleteMapping("/api/todo/{userId}/{id}")
    fun deleteTodo(@PathVariable userId: String, @PathVariable id: String): ResponseEntity<Void> {
        try {
            todoRepository.deleteTodo(userId, id)
            return ResponseEntity(HttpStatus.OK)
        } catch (_: NoSuchElementException) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

}
