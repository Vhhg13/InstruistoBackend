package tk.vhhg.security.hashing

import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class HashingService {
    fun generateSaltedHash(value: String, saltLength: Int = 32): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
        val hash = DigestUtils.sha256Hex("$salt$value")
        return SaltedHash(hash, "$salt")
    }
    fun verify(value: String, saltedHash: SaltedHash): Boolean {
        return DigestUtils.sha256Hex(saltedHash.salt + value) == saltedHash.hash
    }
}