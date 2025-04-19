package com.example.todo

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class TodoApplicationTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var dynamoDbClient: DynamoDbClient

	@Test
	fun contextLoads() {
	}

	@Test
	fun dynamoDbTest() {
		// DynamoDB関連の機能テスト。
		val tableName = "todo"
		val id = UUID.randomUUID().toString()

		// テーブルに値を追加。
		val item = mapOf(
			"PK" to fromS(id),
			"text" to fromS("foo"),
		)
		val putItemRequest = PutItemRequest
			.builder()
			.tableName(tableName)
			.item(item)
			.build()
		dynamoDbClient.putItem(putItemRequest)

		// テーブルの全項目を取得。
		val scanRequest = ScanRequest
			.builder()
			.tableName(tableName)
			.build()
		val scanResponse = dynamoDbClient.scan(scanRequest)
		for (item in scanResponse.items()) {
			val id = item["PK"]
			val text = item["text"]
			println("id=$id, text=$text")
		}

		// テーブルから値を削除。
		val key = mapOf(
			"PK" to fromS(id),
		)
		val deleteItemRequest = DeleteItemRequest
			.builder()
			.tableName(tableName)
			.key(key)
			.build()
		dynamoDbClient.deleteItem(deleteItemRequest)
	}

//	@Test
//	fun `初期状態でtodoエンドポイントをGETすると、空のリストが返る。`() {
//		mockMvc
//			.perform(
//				get("/api/todo")
//			)
//			.andExpect(status().isOk)
//			.andExpect(jsonPath("$").isEmpty)
//			.andExpect(jsonPath("$").isArray)
//	}

//	@Test
//	fun `todoエンドポイントにJSONをPOSTすると、200 OKが返る。`() {
//	}

//	@Test
//	fun `todoエンドポイントにJSONをPOSTすると、データベースに追加されている。`() {
//	}

//	@Test
//	fun `複数回JSONをPOSTすると、その数だけデータベースに追加されている。`() {
//	}

//	@Test
//	fun `GETすると、現在までにPOSTした内容すべてがリストとして返される。`() {
//	}

//	@Test
//	fun `POSTすると、新しく追加されたidを返す。`() {
//	}

//	@Test
//	fun `存在しないIDに対してGETしようとすると、404エラーを返す。`() {
//	}

//	@Test
//	fun `特定のIDに対してGETすると、その項目だけを返す`() {
//	}

//	@Test
//	fun `存在しないIDに対してDELETEしようとすると、404エラーを返す。`() {
//	}

//	@Test
//	fun `特定のIDに対してDELETEすると、その項目はデータベースから削除される。`() {
//	}

}
