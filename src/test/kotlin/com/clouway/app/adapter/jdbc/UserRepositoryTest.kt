package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.User
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.FileReader

class UserRepositoryTest {
    private val mySqlDataSource = MysqlDataSource()

    private val table = "Users"
    private lateinit var userRepository: JdbcUserRepository

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        userRepository = JdbcUserRepository(MySQLJdbcTemplate(mySqlDataSource), table)
        val statement = mySqlDataSource.connection.createStatement()
        statement.execute("DROP TABLE IF EXISTS $table")
        statement.execute(FileReader("schema/$table.sql").readText())
    }

    @Test
    fun tryToRegisterAlreadyRegisteredUser() {
        userRepository.registerUser("user123", "password456")
        assertThat(userRepository.registerUser("user123", "password789"),
                `is`(nullValue()))
    }

    @Test
    fun getUserThatWasRegistered() {
        val user = User(0, "user123", "password789")
        val registeredUser = userRepository.registerUser("user123", "password789")
        assertThat(userRepository.authenticate(user.username, user.password), `is`(equalTo(registeredUser)))
    }

    @Test
    fun tryToGetUnregisteredUser() {
        assertThat(userRepository.authenticate("user123", "password456"), `is`(nullValue()))
    }

}