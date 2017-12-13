package jdbc 

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.adapter.jdbc.JdbcSessionRepository
import com.clouway.app.adapter.jdbc.JdbcUserRepository
import com.clouway.app.core.Session
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.FileReader
import java.sql.Timestamp
import java.time.LocalDateTime

class JdbcSessionRepositoryTest {
    private val mySqlDataSource = MysqlDataSource()

    private val table = "Sessions"
    private val userTable = "Users"
    private lateinit var sessionRepository: JdbcSessionRepository
    private lateinit var userRepository: JdbcUserRepository

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        sessionRepository = JdbcSessionRepository(MySQLJdbcTemplate(mySqlDataSource), table, userTable)
        userRepository = JdbcUserRepository(MySQLJdbcTemplate(mySqlDataSource), userTable)
        val statement = mySqlDataSource.connection.createStatement()
        statement.execute("DROP TABLE IF EXISTS $table")
        statement.execute("DROP TABLE IF EXISTS $userTable")
        statement.execute(FileReader("schema/$userTable.sql").readText())
        statement.execute(FileReader("schema/$table.sql").readText())
    }

    @Test
    fun getSessionThatWasRegistered() {
        userRepository.registerUser("user123", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        val session = Session("hdwasd", "user123", expiresAt)
        assertThat(sessionRepository.registerSession(session),
                `is`(equalTo(true)))
        assertThat(sessionRepository.getSession("hdwasd"), `is`(equalTo(session)))
    }

    @Test
    fun tryToBindSessionToUnregisteredUser() {
        val session = Session("hdwasd", "unregisteredUsername", Timestamp.valueOf(LocalDateTime.now()))
        assertThat(sessionRepository.registerSession(session),
                `is`(equalTo(false)))
    }

    @Test
    fun tryToGetSessionWithWrongId() {
        assertThat(sessionRepository.getSession("notExistingId"), `is`(nullValue()))
    }

    @Test
    fun tryToGetSessionThatWasExpired() {
        userRepository.registerUser("user123", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().minusHours(1))
        sessionRepository.registerSession(Session("hdwasd", "user123", expiresAt))
        assertThat(sessionRepository.getSession("hdwasd"), `is`(nullValue()))
    }

    @Test
    fun tryToRegisterTwoSessionsWithTheSameId() {
        userRepository.registerUser("user123", "somePassword")
        userRepository.registerUser("anotherUser", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(1))
        sessionRepository.registerSession(Session("hdwasd", "user123", expiresAt))
        assertThat(sessionRepository.registerSession(Session("hdwasd", "anotherUser", expiresAt)),
                `is`(equalTo(false)))
    }

    @Test
    fun getActiveSessionsCount() {
        assertThat(sessionRepository.getSessionsCount(), `is`(equalTo(0)))
        userRepository.registerUser("user123", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        sessionRepository.registerSession(Session("hdwasd", "user123", expiresAt))
        assertThat(sessionRepository.getSessionsCount(), `is`(equalTo(1)))
    }

    @Test
    fun deleteSessionThatWasRegistered() {
        userRepository.registerUser("user123", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        sessionRepository.registerSession(Session("hdwasd", "user123", expiresAt))
        assertThat(sessionRepository.deleteSession("hdwasd"), `is`(equalTo(true)))
    }

    @Test
    fun tryToDeleteSessionThatWasNotRegistered() {
        assertThat(sessionRepository.deleteSession("notExistingId"), `is`(equalTo(false)))
    }
}
