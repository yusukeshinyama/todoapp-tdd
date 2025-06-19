package com.example.todo

import org.springframework.web.bind.annotation.RestController
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@RestController
class TodoController(val dynamoDbClient: DynamoDbClient) {

}