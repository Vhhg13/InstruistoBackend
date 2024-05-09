package tk.vhhg.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import tk.vhhg.routes.changePassword
import tk.vhhg.routes.login
import tk.vhhg.routes.register
import tk.vhhg.security.hashing.HashingService
import tk.vhhg.security.token.TokenConfig
import tk.vhhg.security.token.TokenService

fun Application.configureRouting(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        login(hashingService, tokenService, tokenConfig)
        register(hashingService)
        changePassword(hashingService)
    }
}
