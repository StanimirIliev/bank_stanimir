package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.FileReader

class JdbcUserRepositoryTest {
    private val mySqlDataSource = MysqlDataSource()

    private val table = "Users"
    private val sessionsTable = "Sessions"
    private lateinit var userRepository: JdbcUserRepository

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        userRepository = JdbcUserRepository(MySQLJdbcTemplate(mySqlDataSource), table)
        val statement = mySqlDataSource.connection.createStatement()
        if (statement.executeQuery("SHOW TABLES LIKE '$table'").next()) {//  check if the table exists
            statement.execute("DELETE FROM $table")//   clears table
        } else {
            statement.execute(FileReader("schema/$table.sql").readText())// if the table does not exists create it
        }
        if (statement.executeQuery("SHOW TABLES LIKE '$sessionsTable'").next()) {//  check if the table exists
            statement.execute("DELETE FROM $sessionsTable")//   clears table
        } else {
            statement.execute(FileReader("schema/$sessionsTable.sql").readText())// if the table does not exists create it
        }
    }

    @Test
    fun tryToRegisterAlreadyRegisteredUser() {
        userRepository.registerUser("user123", "password456")
        assertThat(userRepository.registerUser("user123", "password789"),
                `is`(equalTo(-1)))
    }

    @Test
    fun authenticateUserThatWasRegistered() {
        userRepository.registerUser("user123", "password789")
        assertThat(userRepository.authenticate("user123", "password789"), `is`(equalTo(true)))
    }

    @Test
    fun tryToAuthenticateUnregisteredUser() {
        assertThat(userRepository.authenticate("user123", "password456"), `is`(equalTo(false)))
    }

    @Test
    fun getUsernameOfRegisteredUser() {
        val userId = userRepository.registerUser("user123", "password789")
        assertThat(userRepository.getUsername(userId), `is`(equalTo("user123")))
    }

    @Test
    fun tryToGetUsernameOfUnregisteredUser() {
        assertThat(userRepository.getUsername(-1), `is`(nullValue()))
    }
}
