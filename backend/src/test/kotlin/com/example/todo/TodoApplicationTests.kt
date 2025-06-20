package com.example.todo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class TodoApplicationTests {

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var dynamoDbClient: DynamoDbClient

	val tableName = "todo"

	@Test
	fun contextLoads() {
	}

	@BeforeEach
	fun setUp() {
		// テーブルを空にする。
		val scanRequest = ScanRequest
			.builder()
			.tableName(tableName)
			.build()
		val scanResponse = dynamoDbClient.scan(scanRequest)
		for (item in scanResponse.items()) {
			val key = mapOf(
				"PK" to item["PK"],
				"SK" to item["SK"],
			)
			val deleteItemRequest = DeleteItemRequest
				.builder()
				.tableName(tableName)
				.key(key)
				.build()
			dynamoDbClient.deleteItem(deleteItemRequest)
		}
	}

	// todoをポストし、IDを返す。
	private fun postTodo(userId: String, data: TodoRequest): String {
		val result = mockMvc
			.perform(
				post("/api/todo/$userId")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(data))
			)
			.andReturn()
		return result.response.contentAsString
	}

	// データベースからtodoの一覧を取得する。
	private fun getTodos(userId: String): List<TodoEntity> {
		val result = mockMvc
			.perform(
				get("/api/todo/$userId")
			)
			.andReturn()
		val json = result.response.contentAsString
		return objectMapper.readValue<List<TodoEntity>>(json)
	}

	// データベースからtodoをひとつ取得する。
	private fun getTodo1(userId: String, id: String): TodoEntity {
		val result = mockMvc
			.perform(
				get("/api/todo/$userId/$id")
			)
			.andReturn()
		val json = result.response.contentAsString
		return objectMapper.readValue<TodoEntity>(json)
	}

	@Test
	fun dynamoDbTest() {
		// DynamoDB関連の機能テスト。
		val userId = UUID.randomUUID().toString()
		val id = UUID.randomUUID().toString()

		// テーブルに値を追加。
		val item1 = mapOf(
			"PK" to fromS(userId),
			"SK" to fromS(id),
			"text" to fromS("foo"),
		)
		val putItemRequest = PutItemRequest
			.builder()
			.tableName(tableName)
			.item(item1)
			.build()
		dynamoDbClient.putItem(putItemRequest)

		// テーブルの全項目を取得。
		val scanRequest = ScanRequest
			.builder()
			.tableName(tableName)
			.build()
		val scanResponse = dynamoDbClient.scan(scanRequest)
		for (item in scanResponse.items()) {
			val userId = item["PK"]
			val id = item["SK"]
			val text = item["text"]
			println("userId=$userId, id=$id, text=$text")
		}

		// テーブルの1項目を取得。
		val key1 = mapOf(
			"PK" to fromS(userId),
			"SK" to fromS(id),
		)
		val getItemRequest = GetItemRequest
			.builder()
			.tableName(tableName)
			.key(key1)
			.build()
		val item2 = dynamoDbClient.getItem(getItemRequest)
		println("item2=$item2")

		// テーブルから値を削除。
		val key2 = mapOf(
			"PK" to fromS(userId),
			"SK" to fromS(id),
		)
		val deleteItemRequest = DeleteItemRequest
			.builder()
			.tableName(tableName)
			.key(key2)
			.build()
		dynamoDbClient.deleteItem(deleteItemRequest)
	}

	@Test
	fun `初期状態でtodoエンドポイントをGETすると、空のリストが返る。`() {
		mockMvc
			.perform(
				get("/api/todo/john")
			)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$").isEmpty)
			.andExpect(jsonPath("$").isArray)
	}

	@Test
	fun `todoエンドポイントにJSONをPOSTすると、200 OKが返る。`() {
		val data = TodoRequest(text="abc")
		mockMvc
			.perform(
				post("/api/todo/john")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(data))
			)
			.andExpect(status().isOk)
	}

	@Test
	fun `todoエンドポイントにJSONをPOSTすると、データベースに追加されている。`() {
		// Arrange

		// Act
		// {text:"abc"}をポストする
		val data = TodoRequest(text = "abc")
		postTodo("john", data)

		// Assert
		val items = getTodos("john")
		// テーブルを全取得すると長さ1。
		assertThat(items.size, equalTo(1))
		// 0番目の要素が "abc" である。
		assertThat(items[0].text, equalTo(data.text))
	}

	@Test
	fun `複数回JSONをPOSTすると、その数だけデータベースに追加されている。`() {
		// Arrange

		// Act
		// {text:"abc"}をポストする
		val data1 = TodoRequest(text="abc")
		postTodo("john", data1)
		// {text:"def"}をポストする
		val data2 = TodoRequest(text="def")
		postTodo("john", data2)

		// Assert
		val items = getTodos("john")
		// テーブルを全取得すると長さ2。
		assertThat(items.size, equalTo(2))
		val texts = items.map { it.text }
		// 2つの要素が "abc", "def" である。
		assertThat(texts, hasItems(data1.text, data2.text))
	}

	@Test
	fun `GETすると、現在までにPOSTした内容すべてがリストとして返される。`() {
		// Arrange
		// {text:"abc"}をポストする
		val data1 = TodoRequest(text="abc")
		postTodo("john", data1)
		// {text:"def"}をポストする
		val data2 = TodoRequest(text="def")
		postTodo("john", data2)

		// Act
		// /api/todoをGETする。
		val result = mockMvc.perform(
			get("/api/todo/john")
		)
			.andReturn()
		val json = result.response.contentAsString
		val items = objectMapper.readValue<List<TodoRequest>>(json)

		// Assert
		// 2つの要素が "abc", "def" である。
		assertThat(items.size, equalTo(2))
		val texts = items.map { it.text }
		assertThat(texts, hasItems(data1.text, data2.text))
	}

	@Test
	fun `POSTすると、新しく追加されたidを返す。`() {
		// Act
		// {text:"abc"}をポストし、idを取得する。
		val data1 = TodoRequest(text="abc")
		val id = postTodo("john", data1)

		// Assert
		// 返されたidの項目がテーブル中に存在している。
		val item = getTodo1("john", id)
		assertThat(item.id, equalTo(id))
		assertThat(item.text, equalTo(data1.text))
	}

	@Test
	fun `存在しないIDに対してGETしようとすると、404エラーを返す。`() {
		// Act
		// id=999 をGETする。
		mockMvc
			.perform(
				get("/api/todo/john/999")
			)
			// Assert
			// 404が返される。
			.andExpect(status().isNotFound)
	}

	@Test
	fun `特定のIDに対してGETすると、その項目だけを返す`() {
		// Arrange
		// {text:"abc"}をポストし、idを取得する。
		val data1 = TodoRequest(text="abc")
		val id = postTodo("john", data1)

		// act
		// /api/todo/id をGETする。
		mockMvc
			.perform(
				get("/api/todo/john/$id")
			)
			// Assert
			// 200が返される。
			.andExpect(status().isOk)
			// idが一致。
			.andExpect(jsonPath("$.id").value(id))
			// textが一致。
			.andExpect(jsonPath("$.text").value(data1.text))
	}

	@Test
	fun `存在しないIDに対してDELETEしようとすると、404エラーを返す。`() {
		// Act
		// id=999 をDELETEする。
		mockMvc
			.perform(
				delete("/api/todo/john/999")
			)
			// Assert
			// 404が返される。
			.andExpect(status().isNotFound)
	}

	@Test
	fun `特定のIDに対してDELETEすると、その項目はデータベースから削除される。`() {
		// Arrange
		// {text:"abc"}をポストし、idを取得する。
		val data1 = TodoRequest(text="abc")
		val id = postTodo("john", data1)

		// act
		// /api/todo/id をDELETEする。
		mockMvc
			.perform(
				delete("/api/todo/john/$id")
			)
			// Assert
			// 200が返される。
			.andExpect(status().isOk)

		// Assert
		// 返されたidの項目がテーブル中に存在していない。
		mockMvc
			.perform(
				get("/api/todo/john/$id")
			)
			// Assert
			// 200が返される。
			.andExpect(status().isNotFound)
	}

	@Test
	fun `異なるユーザIDに対するPOSTは独立している。`() {
		// Arrange
		val user1 = "john"
		val user2 = "mary"
		// user1とuser2にそれぞれ項目を追加。
		val data1 = TodoRequest(text="abc")
		val data2 = TodoRequest(text="def")
		postTodo(user1, data1)
		postTodo(user2, data2)

		// Act
		// user1とuser2の項目一覧をそれぞれ取得。
		val items1 = getTodos(user1)
		val items2 = getTodos(user2)

		// Assert
		// 別々の値が含まれていることをチェック。
		assertThat(items1.size, equalTo(1))
		assertThat(items2.size, equalTo(1))
		assertThat(items1[0].text, equalTo(data1.text))
		assertThat(items2[0].text, equalTo(data2.text))
	}
}
