package tk.vhhg

import io.ktor.server.application.*
import tk.vhhg.db.DatabaseSingleton
import tk.vhhg.plugins.*
import tk.vhhg.security.hashing.HashingService
import tk.vhhg.security.token.TokenConfig
import tk.vhhg.security.token.TokenService

fun main(args: Array<String>) {
    io.ktor.server.tomcat.EngineMain.main(args)
}

fun Application.module() {
    DatabaseSingleton.init()
    val tokenService = TokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L  * 60L  * 60L * 24L,
        secret = "jwt-secret" // ¯\_(ツ)_/¯
    )
    val hashingService = HashingService()

    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(hashingService, tokenService, tokenConfig)
}
