package com.example.todo

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class TodoApplicationTests {

	@Autowired
	lateinit var mockMvc: MockMvc

	@Test
	fun contextLoads() {
	}

	@Test
	fun `初期状態でtodoエンドポイントをGETすると、空のリストが返る。`() {
		mockMvc
			.perform(
				get("/api/todo")
			)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$").isEmpty)
			.andExpect(jsonPath("$").isArray)
	}

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
