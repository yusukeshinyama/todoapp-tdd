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

    @GetMapping("/api/todo")
    fun getTodos(): List<TodoEntity> {
        return todoRepository.getTodos()
    }

    @GetMapping("/api/todo/{id}")
    fun getTodo1(@PathVariable id: String): ResponseEntity<TodoEntity> {
        try {
            return ResponseEntity(todoRepository.getTodo1(id), HttpStatus.OK)
        } catch (e: NoSuchElementException) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping("/api/todo")
    fun postTodo(@RequestBody req: TodoRequest): String {
        return todoRepository.addTodo(req.text)
    }

    @DeleteMapping("/api/todo/{id}")
    fun deleteTodo(@PathVariable id: String): ResponseEntity<Void> {
        try {
            todoRepository.deleteTodo(id)
            return ResponseEntity(HttpStatus.OK)
        } catch (e: NoSuchElementException) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

}
