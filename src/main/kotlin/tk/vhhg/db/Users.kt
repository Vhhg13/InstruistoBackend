package tk.vhhg.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object Users : IntIdTable("Users") {
    val login = varchar("login", 63)
    val salt = varchar("salt", 63)
    val pwdHash = varchar("passwd_hash", 255)
    val image = varchar("image", 255)
    val lastRefresh = date("flashcards_refreshed")
    val newWordsLearnedToday = integer("new_words_learned_today")
}