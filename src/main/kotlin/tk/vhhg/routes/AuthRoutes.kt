package tk.vhhg.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.update
import tk.vhhg.db.DatabaseSingleton
import tk.vhhg.db.Users
import tk.vhhg.model.AuthRequest
import tk.vhhg.model.User
import tk.vhhg.security.hashing.HashingService
import tk.vhhg.security.hashing.SaltedHash
import tk.vhhg.security.token.TokenClaim
import tk.vhhg.security.token.TokenConfig
import tk.vhhg.security.token.TokenService

fun Routing.login(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) =
    post("/login"){
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = DatabaseSingleton.getUserByUsername(request.username)
        if(user == null){
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }
        val isPasswordValid = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if(!isPasswordValid){
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = TokenClaim.userId,
                value = user.id.toString()
            )
        )
        call.respond(HttpStatusCode.OK, token)
    }

fun Routing.register(
    hashingService: HashingService
) =
    post("/register"){
        val request = kotlin.runCatching<AuthRequest?> { call.receiveNullable<AuthRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        if(DatabaseSingleton.insertUser(user))
            call.respond(HttpStatusCode.OK)
        else
            call.respond(HttpStatusCode.Conflict)
    }

fun Routing.changePassword(
    hashingService: HashingService
) =
    authenticate {
        post("/changePassword"){
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim(TokenClaim.userId)?.asString()
            if(userId == null){
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            val request = call.receiveText()
            val saltedHash = hashingService.generateSaltedHash(request)
            val updatedCount = DatabaseSingleton.dbQuery {
                Users.update({ Users.id eq userId.toInt() }) {
                    it[pwdHash] = saltedHash.hash
                    it[salt] = saltedHash.salt
                }
            }
            if(updatedCount == 1)
                call.respond(HttpStatusCode.OK)
            else
                call.respond(HttpStatusCode.Unauthorized)
        }
    }