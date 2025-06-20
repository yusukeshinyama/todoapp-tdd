package com.example.todo

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.util.UUID

@Repository
class TodoRepository(
    val dynamoDbClient: DynamoDbClient,
    @Value("\${todo.tableName:todo}")
    val tableName: String,
) {

    fun getTodos(userId: String): List<TodoEntity> {
        val scanRequest = ScanRequest
            .builder()
            .tableName(tableName)
            .build()
        val scanResponse = dynamoDbClient.scan(scanRequest)
        val items = scanResponse.items().toList()
        return items
            .filter { it["PK"]!!.s() == userId }
            .map {
                TodoEntity(id = it["SK"]!!.s(), text = it["text"]!!.s())
            }
    }

    fun getTodo1(userId: String, id: String): TodoEntity {
        val key = mapOf(
            "PK" to fromS(userId),
            "SK" to fromS(id),
        )
        val getItemRequest = GetItemRequest
            .builder()
            .tableName(tableName)
            .key(key)
            .build()
        val getItemResponse = dynamoDbClient.getItem(getItemRequest)
        if (!getItemResponse.hasItem()) {
            throw NoSuchElementException(id)
        }

        val item = getItemResponse.item()
        return TodoEntity(id = item["SK"]!!.s(), text = item["text"]!!.s())
    }

    fun addTodo(userId: String, data: TodoRequest): String {
        // テーブルに値を追加。
        val id = UUID.randomUUID().toString()
        val item1 = mapOf(
            "PK" to fromS(userId),
            "SK" to fromS(id),
            "text" to fromS(data.text),
        )
        val putItemRequest = PutItemRequest
            .builder()
            .tableName(tableName)
            .item(item1)
            .build()
        dynamoDbClient.putItem(putItemRequest)
        return id
    }

    fun deleteTodo(userId: String, id: String) {
        // テーブルから値を取得。
        val key = mapOf(
            "PK" to fromS(userId),
            "SK" to fromS(id),
        )
        val getItemRequest = GetItemRequest
            .builder()
            .tableName(tableName)
            .key(key)
            .build()
        val getItemResponse = dynamoDbClient.getItem(getItemRequest)
        if (!getItemResponse.hasItem()) {
            throw NoSuchElementException(id)
        }
        // テーブルから値を削除。
        val deleteItemRequest = DeleteItemRequest
            .builder()
            .tableName(tableName)
            .key(key)
            .build()
        dynamoDbClient.deleteItem(deleteItemRequest)
    }

}