package com.example.todo

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.util.UUID

@RestController
class TodoController(val dynamoDbClient: DynamoDbClient) {

    @GetMapping("/api/todo")
    fun getTodos(): List<TodoEntity> {
        val scanRequest = ScanRequest
            .builder()
            .tableName("todo")
            .build()
        val scanResponse = dynamoDbClient.scan(scanRequest)
        val items = scanResponse.items().toList()
        return items.map {
            TodoEntity(id = it["PK"]!!.s(), text = it["text"]!!.s())
        }
    }

    @GetMapping("/api/todo/{id}")
    fun getTodo1(@PathVariable id: String): ResponseEntity<TodoEntity> {
        val key1 = mapOf(
            "PK" to fromS(id),
        )
        val getItemRequest = GetItemRequest
            .builder()
            .tableName("todo")
            .key(key1)
            .build()
        val getItemResponse = dynamoDbClient.getItem(getItemRequest)
        if (getItemResponse.hasItem()) {
            val item = getItemResponse.item()
            return ResponseEntity(TodoEntity(id = item["PK"]!!.s(), text = item["text"]!!.s()), HttpStatus.OK)
        }
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/api/todo")
    fun postTodo(@RequestBody req: TodoRequest): String {
        // テーブルに値を追加。
        val id = UUID.randomUUID().toString()
        val item1 = mapOf(
            "PK" to fromS(id),
            "text" to fromS(req.text),
        )
        val putItemRequest = PutItemRequest
            .builder()
            .tableName("todo")
            .item(item1)
            .build()
        dynamoDbClient.putItem(putItemRequest)
        return id
    }

    @DeleteMapping("/api/todo/{id}")
    fun deleteTodo(@PathVariable id: String): ResponseEntity<Void> {
        // テーブルから値を取得。
        val key1 = mapOf(
            "PK" to fromS(id),
        )
        val getItemRequest = GetItemRequest
            .builder()
            .tableName("todo")
            .key(key1)
            .build()
        val getItemResponse = dynamoDbClient.getItem(getItemRequest)
        if (!getItemResponse.hasItem()) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        // テーブルから値を削除。
        val key2 = mapOf(
            "PK" to fromS(id),
        )
        val deleteItemRequest = DeleteItemRequest
            .builder()
            .tableName("todo")
            .key(key2)
            .build()
        dynamoDbClient.deleteItem(deleteItemRequest)
        return ResponseEntity(HttpStatus.OK)
    }

}