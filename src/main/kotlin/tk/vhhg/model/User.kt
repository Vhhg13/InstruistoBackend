package tk.vhhg.model

import java.time.LocalDate
import java.util.Date

data class User(
    val username: String,
    val password: String,
    val salt: String,
    val imageUrl: String? = null,
    val lastRefresh: LocalDate = LocalDate.now(),
    val newWordsToday: Int = 0,
    val id: Int = 0
)