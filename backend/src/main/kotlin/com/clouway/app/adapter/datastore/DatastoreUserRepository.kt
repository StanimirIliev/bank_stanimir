package com.clouway.app.adapter.datastore

import com.clouway.app.SaltedHash
import com.clouway.app.datastore.core.DatastoreTemplate
import com.clouway.app.core.UserRepository
import com.clouway.app.datastore.core.EntityMapper
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Query.*
import com.google.appengine.api.datastore.Query.FilterOperator.*
import com.google.appengine.api.datastore.Query.CompositeFilterOperator.*
import org.apache.commons.codec.digest.DigestUtils

class DatastoreUserRepository(private val datastoreTemplate: DatastoreTemplate) : UserRepository {

    private val entityMapper = object : EntityMapper<Entity> {
        override fun fetch(entity: Entity): Entity = entity
    }

    override fun registerUser(username: String, password: String): Int {
        //check if there is someone registered with this username already
        val list = datastoreTemplate.fetch("Users", null, entityMapper)
        if(list.find{ it.getProperty("Username").toString() == username} != null) {
            return -1
        }
        //register this user
        val saltedHash = SaltedHash(30, password)
        val entity = Entity("Users")
        entity.setProperty("Username", username)
        entity.setProperty("Password", saltedHash.hash)
        entity.setProperty("Salt", saltedHash.salt)
        val key = datastoreTemplate.insert(entity)?: return -1
        return key.id.toInt()
    }

    override fun authenticate(username: String, password: String): Boolean {
        val salt = datastoreTemplate.fetch("Users", null, object : EntityMapper<String> {
            override fun fetch(entity: Entity): String {
                return entity.getProperty("Salt").toString()
            }
        })
        if (salt.isEmpty()) {
            return false
        }
        val filter1 = FilterPredicate("Username", EQUAL, username)
        val filter2 = FilterPredicate("Password", EQUAL, DigestUtils.sha256Hex(salt.first() + password))
        val compositeFilter = CompositeFilter(AND, listOf(filter1, filter2))
        val list = datastoreTemplate.fetch(compositeFilter, "Users", object : EntityMapper<Entity> {
            override fun fetch(entity: Entity): Entity = entity
        })
        return !list.isEmpty()
    }

    override fun getUsername(id: Int): String? {
        val list = datastoreTemplate.fetch("Users", null, entityMapper)
        val desiredEntity = list.find { it.key.id.toInt() == id} ?: return null
        return desiredEntity.getProperty("Username").toString()
    }

    override fun getUserId(username: String): Int {
        val list = datastoreTemplate.fetch("Users", null, entityMapper)
        return list.find { it.getProperty("Username").toString() == username}?.key?.id?.toInt() ?: -1
    }
}