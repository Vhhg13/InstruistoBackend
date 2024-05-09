package tk.vhhg.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import tk.vhhg.model.User
import kotlin.math.log

object DatabaseSingleton {
    fun init(){
        val driverClassName = "org.postgresql.Driver"
        val jdbcURL = "jdbc:postgresql://localhost:5432/instruisto"
        val database = Database.connect(jdbcURL, driverClassName,
            user = "instruisto",
            password = "lingvo_internacia"
        )
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun getUserByUsername(username: String): User? {
        val row = dbQuery {
            Users.selectAll().where{ Users.login eq username }.singleOrNull()
        }
        return row?.let{
            User(
                username = row[Users.login],
                password = row[Users.pwdHash],
                salt = row[Users.salt],
                id = row[Users.id].value
            )
        }
    }
    suspend fun insertUser(user: User): Boolean{
        try {
            dbQuery {
                Users.insert {
                    it[login] = user.username
                    it[pwdHash] = user.password
                    it[salt] = user.salt
                    it[lastRefresh] = user.lastRefresh
                    if (user.imageUrl != null) it[image] = user.imageUrl
                    it[newWordsLearnedToday] = user.newWordsToday
                }
            }
            return true
        }catch(e: ExposedSQLException){
            return false
        }
    }
}