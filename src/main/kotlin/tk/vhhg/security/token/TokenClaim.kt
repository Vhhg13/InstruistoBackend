package tk.vhhg.security.token

data class TokenClaim(
    val name: String,
    val value: String
){
    companion object{
        const val userId = "userId"
    }
}
