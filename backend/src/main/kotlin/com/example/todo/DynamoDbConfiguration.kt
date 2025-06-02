package com.example.todo

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.*
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI

@Configuration
class DynamoDbConfiguration(
    @Value("\${aws.dynamodb.region:ap-northeast-1}")
    private val region: String,
    @Value("\${aws.dynamodb.endpoint:http://localhost:4566/}")
    private val endpoint: String,
    @Value("\${aws.dynamodb.accessKey:xxx}")
    private val accessKey: String,
    @Value("\${aws.dynamodb.secretKey:yyy}")
    private val secretKey: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun awsCredentials(): AwsBasicCredentials {
        return AwsBasicCredentials.create(accessKey, secretKey)
    }

    @Bean
    fun credentialsProvider(credentials: AwsBasicCredentials): AwsCredentialsProviderChain {
        return AwsCredentialsProviderChain.of(
            DefaultCredentialsProvider.create(),
            StaticCredentialsProvider.create(credentials),
        )
    }

    @Bean
    fun dynamoDbClient(credentialsProvider: AwsCredentialsProvider): DynamoDbClient {
        logger.info("dynamoDbClient: region=$region, endpoint=$endpoint")
        return DynamoDbClient.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
            .build()
    }
}
