package com.fanxan.jemputkuyapi

import com.mongodb.client.MongoClient
import org.litote.kmongo.KMongo
import org.springframework.stereotype.Component

@Component
class DatabaseComponent {
    private val DB_URL = System.getenv("DATABASE_URL")
    val database: MongoClient = KMongo.createClient(DB_URL)
}