package com.porua.app.data.repository

import com.porua.app.data.auth.AuthDataStore
import com.porua.app.data.auth.AuthUser
import com.porua.app.data.auth.PasswordHasher
import com.porua.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Authentication repository interface.
 * Abstracts local or remote (e.g., Firebase Auth) authentication mechanisms.
 */
interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUser: Flow<AuthUser?>

    suspend fun signUp(
        name: String,
        emailOrPhone: String,
        password: String,
        group: String,
        hscClass: String
    ): Result<AuthUser>

    suspend fun login(
        emailOrPhone: String,
        password: String
    ): Result<AuthUser>

    suspend fun logout()
}

/**
 * Local implementation of AuthRepository storing data in DataStore and Room.
 */
class LocalAuthRepository(
    private val authDataStore: AuthDataStore,
    private val poruaRepository: PoruaRepository? = null
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = authDataStore.isLoggedIn
    override val currentUser: Flow<AuthUser?> = authDataStore.currentUser

    override suspend fun signUp(
        name: String,
        emailOrPhone: String,
        password: String,
        group: String,
        hscClass: String
    ): Result<AuthUser> {
        return try {
            val hash = PasswordHasher.hash(password)
            authDataStore.saveAccount(
                name = name.trim(),
                emailOrPhone = emailOrPhone.trim(),
                passwordHash = hash,
                group = group,
                hscClass = hscClass
            )

            // Normalize stream name
            val englishGroup = when (group.lowercase().trim()) {
                "science" -> "Science"
                "humanities", "arts" -> "Arts"
                "business studies", "commerce", "commers" -> "Commerce"
                else -> group
            }

            val examYear = if (hscClass.contains("2027")) "2027" else "2026"

            poruaRepository?.let { repo ->
                val current = repo.getUserProfile().first() ?: UserProfileEntity()
                repo.updateUserProfile(
                    current.copy(
                        name = name.trim(),
                        examTrack = "HSC",
                        group = englishGroup,
                        examYear = examYear,
                        examDate = "12 Apr $examYear"
                    )
                )
            }

            val user = AuthUser(
                name = name.trim(),
                emailOrPhone = emailOrPhone.trim(),
                group = englishGroup,
                hscClass = "HSC"
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(
        emailOrPhone: String,
        password: String
    ): Result<AuthUser> {
        return try {
            val storedEmailOrPhone = authDataStore.storedEmailOrPhone.first()
            val storedHash = authDataStore.storedPasswordHash.first()
            val user = authDataStore.currentUser.first()

            if (storedEmailOrPhone.isNullOrBlank() || storedHash.isNullOrBlank()) {
                return Result.failure(Exception("No saved account found. Please create a new account."))
            }

            if (storedEmailOrPhone.equals(emailOrPhone.trim(), ignoreCase = true)) {
                val inputHash = PasswordHasher.hash(password)
                if (inputHash == storedHash) {
                    authDataStore.setLoggedIn(true)
                    val activeUser = user ?: AuthUser("Student", emailOrPhone.trim(), "Science", "HSC")
                    Result.success(activeUser)
                } else {
                    Result.failure(Exception("Incorrect password. Please try again."))
                }
            } else {
                Result.failure(Exception("No account matches this email or phone number."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        authDataStore.logout()
    }
}
