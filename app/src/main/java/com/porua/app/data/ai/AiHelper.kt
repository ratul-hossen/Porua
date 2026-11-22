package com.porua.app.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object AiHelper {

    // === API KEYS ===
    const val GOOGLE_API_KEY = "YOUR_GOOGLE_API_KEY_HERE"
    const val GROK_API_KEY = "YOUR_GROQ_API_KEY_HERE"

    var userGoogleApiKey: String? = null
    var userGrokApiKey: String? = null

    private fun cleanKey(key: String?): String {
        if (key.isNullOrBlank()) return ""
        return key.trim().trim('"', '\'', '`')
    }

    val effectiveGoogleApiKey: String
        get() = cleanKey(userGoogleApiKey).takeIf { it.isNotBlank() && !it.contains("YOUR_") }
            ?: cleanKey(GOOGLE_API_KEY).takeIf { it.isNotBlank() && !it.contains("YOUR_") } ?: ""

    val effectiveGrokApiKey: String
        get() = cleanKey(userGrokApiKey).takeIf { it.isNotBlank() && !it.contains("YOUR_") }
            ?: cleanKey(GROK_API_KEY).takeIf { it.isNotBlank() && !it.contains("YOUR_") } ?: ""

    fun hasAnyApiKey(): Boolean = effectiveGoogleApiKey.isNotBlank() || effectiveGrokApiKey.isNotBlank()

    // === MODELS ===
    const val GEMINI_MODEL = "gemini-2.0-flash"
    const val GEMINI_FALLBACK_MODEL = "gemini-1.5-flash"
    const val GROQ_MODEL_OPENAI_120B = "openai/gpt-oss-120b"
    const val GROQ_MODEL_OPENAI_20B = "openai/gpt-oss-20b"
    const val GROQ_MODEL_FAST = "llama-3.1-8b-instant"
    const val GROQ_VISION_MODEL = "llama-3.2-11b-vision-preview"
    const val GROK_MODEL = GROQ_MODEL_OPENAI_120B
    const val GROQ_FAST_FALLBACK = GROQ_MODEL_FAST
    const val GROK_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"

    @Volatile var activeGoogleModel: String = "gemini-2.0-flash"
    @Volatile var activeGoogleApiVersion: String = "v1beta"
    @Volatile var activeGroqModel: String = GROQ_MODEL_OPENAI_120B

    data class KeyCheckResult(
        val isValid: Boolean,
        val provider: String,
        val activeModel: String,
        val message: String
    )

    data class DualKeyStatus(
        val googleResult: KeyCheckResult,
        val grokResult: KeyCheckResult,
        val hasAnyActive: Boolean,
        val overallMessage: String
    )

    // === IN-MEMORY LRU CACHE (Max 50 items, thread-safe, prevents memory leak) ===
    private val queryMemoryCache = java.util.Collections.synchronizedMap(
        object : java.util.LinkedHashMap<String, String>(60, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
                return size > 50
            }
        }
    )

    fun clearCache() {
        queryMemoryCache.clear()
    }

    // === SYSTEM PROMPT FOR PORUA APP ===
    const val SYSTEM_PROMPT = """You are Porua AI, an independent, highly specialized AI Tutor exclusively dedicated to HSC Science students in Bangladesh following the NCTB curriculum (Class 11 & 12).

IDENTITY & MISSION:
- You are a comprehensive, self-reliant personal mentor for all HSC Science subjects:
  1. Physics (1st & 2nd Paper)
  2. Chemistry (1st & 2nd Paper)
  3. Higher Mathematics (1st & 2nd Paper)
  4. Biology (Botany & Zoology)
  5. ICT (C programming, HTML, Logic Gates, Number Systems)
  6. English (Grammar, academic vocabulary for Science students)
- You operate independently to provide concise, accurate, and pedagogical academic assistance.

STRICT LANGUAGE POLICY:
- PRIMARY & MANDATORY LANGUAGE IS ENGLISH.
- By default, ALL answers, scientific explanations, definitions, derivations, formulas, and problem solutions MUST BE WRITTEN IN ENGLISH.
- DO NOT answer in Bengali simply because a question contains textbook terms, Bengali words, or Banglish phrases.
- ONLY EXCEPTION: If the user explicitly asks you to explain in another language (e.g. "Explain in Bangla", "বাংলায় বলো", "বাংলায় সমাধান দাও", "Answer in Bengali"), ONLY then respond in that requested language. Otherwise, ALWAYS answer in English.

ANSWER LENGTH & CONCISENESS (SHORT TO MEDIUM):
- Keep your answers concise, structured, and focused (short to medium length).
- Avoid lengthy preambles, introductory filler, or conversational fluff.
- Present formulas, definitions, and numerical steps directly and crisply so students can read them quickly on mobile.

CORE CAPABILITIES & PEDAGOGICAL STRUCTURE:
1. MATHEMATICAL & NUMERICAL PROBLEM SOLVING:
   - Provide direct, concise steps:
     Given: Data with standard SI units.
     Formula: Relevant equation.
     Calculation: Direct arithmetic and substitution.
     Answer: Final value with SI units.

2. CREATIVE QUESTIONS (CQ - সৃজনশীল প্রশ্ন) EXPLANATION:
   - Direct, crisp answers for the relevant parts:
     (a) Knowledge (Direct definition or law)
     (b) Comprehension (Scientific concept and core reason)
     (c) Application (Step-by-step mathematical/numerical solution)
     (d) Higher Ability (Comparative analysis and concise conclusion)

3. MCQ DECONSTRUCTION & SHORTCUT TECHNIQUES:
   - State the correct option clearly.
   - 1-2 sentence concise reason why it is correct.
   - Quick shortcut or dimensional check if applicable.

4. CLEAN FORMATTING RULES:
   - ABSOLUTELY NO raw asterisks or markdown stars (*, **, or ***) anywhere in the text.
   - Write headers cleanly on new lines (e.g., "Given:", "Formula:", "Calculation:", "Answer:").
   - Do NOT use raw markdown table pipes (| or |---|); present comparisons using clean bullet points (- or •).
   - Write in clean, plain sentences with clean line breaks.

5. STRICT HSC SCIENCE FOCUS & SCOPE:
   - Your expertise is strictly dedicated to HSC Science (Class 11 & 12).
   - If a student asks questions outside HSC Science (movies, gaming, entertainment, politics, general chat, or non-science streams):
     Politely decline:
     "I am Porua AI, dedicated exclusively to assisting HSC Science (Physics, Chemistry, Higher Mathematics, Biology, ICT, and English) students. Please ask any academic question related to HSC Science."
     Do not entertain off-topic discussions."""

    /**
     * Unified AI caller:
     * 1. If an image is attached, Google Gemini handles vision / diagrams / OCR natively.
     * 2. Tries Google Gemini FIRST (gemini-2.0-flash / gemini-1.5-flash).
     * 3. If Google fails or rate limits, automatically falls back to Groq Cloud / xAI Grok.
     * 4. If offline or no network, falls back to Porua's intelligent offline NCTB tutor.
     * 5. Strips stray asterisks and markdown symbols so the answer is always clean plain text.
     */
    suspend fun askAi(
        prompt: String,
        imageBase64: String? = null,
        mimeType: String = "image/jpeg",
        history: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val googleReady = effectiveGoogleApiKey.isNotBlank()
        val grokReady = effectiveGrokApiKey.isNotBlank()

        val cacheKey = if (imageBase64.isNullOrBlank() && history.isEmpty()) prompt.trim().lowercase() else ""
        if (cacheKey.isNotBlank()) {
            val cached = queryMemoryCache[cacheKey]
            if (!cached.isNullOrBlank()) {
                return@withContext cached
            }
        }

        var googleError: String? = null
        var grokError: String? = null

        // 1. Primary Provider: Google Gemini FIRST (Text & Vision + Multi-turn context)
        if (googleReady) {
            val googleResult = askGoogle(prompt, imageBase64, mimeType, history)
            if (!googleResult.startsWith("Google API Error") && !googleResult.startsWith("Google API Connection Error")) {
                val cleaned = sanitizeEducationalResponse(googleResult)
                if (cacheKey.isNotBlank()) queryMemoryCache[cacheKey] = cleaned
                return@withContext cleaned
            } else {
                googleError = googleResult
            }
        }

        // 2. Secondary Provider / Fallback: Groq Cloud / xAI (Multi-turn context)
        if (grokReady) {
            val grokResult = askGrok(prompt, imageBase64, mimeType, history)
            if (!grokResult.startsWith("Grok API Error") && !grokResult.startsWith("Grok API Connection Error")) {
                val cleaned = sanitizeEducationalResponse(grokResult)
                if (cacheKey.isNotBlank()) queryMemoryCache[cacheKey] = cleaned
                return@withContext cleaned
            } else {
                grokError = grokResult
            }
        }

        // 3. If API keys were provided, but API failed with error:
        if (googleReady || grokReady) {
            val sb = StringBuilder()
            sb.append("⚠️ AI Connection Error:\n\n")
            if (googleError != null) {
                sb.append("• Gemini Error: ").append(googleError).append("\n")
            }
            if (grokError != null) {
                sb.append("• Groq/Grok Error: ").append(grokError).append("\n")
            }
            sb.append("\nPlease check: \n")
            sb.append("1. Tap 🔑 at the top right to verify/re-enter your API Key.\n")
            sb.append("2. Ensure your key is valid and has active quota.\n")
            sb.append("3. Make sure your device is connected to the internet.\n\n")
            sb.append("--- [Offline Backup Response] ---\n")
            sb.append(sanitizeEducationalResponse(getOfflineTutorResponse(prompt)))
            return@withContext sb.toString()
        }

        // 4. Fallback: Intelligent Offline NCTB Tutor (when no keys provided)
        val offlineResult = sanitizeEducationalResponse(getOfflineTutorResponse(prompt))
        if (cacheKey.isNotBlank()) queryMemoryCache[cacheKey] = offlineResult
        offlineResult
    }

    /**
     * Check and validate Google Gemini API key. Queries available models dynamically.
     */
    suspend fun checkGoogleKey(apiKey: String = effectiveGoogleApiKey): KeyCheckResult = withContext(Dispatchers.IO) {
        val clean = cleanKey(apiKey)
        if (clean.isBlank()) {
            return@withContext KeyCheckResult(false, "Gemini", "", "Google API key is not configured.")
        }
        var connection: HttpURLConnection? = null
        try {
            val encodedKey = java.net.URLEncoder.encode(clean, "UTF-8")
            var url = URL("https://generativelanguage.googleapis.com/v1beta/models?key=$encodedKey")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 15000
            }
            var responseCode = connection.responseCode
            var apiVer = "v1beta"
            var responseText = ""

            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                connection.disconnect()
                url = URL("https://generativelanguage.googleapis.com/v1/models?key=$encodedKey")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 15000
                }
                responseCode = connection.responseCode
                apiVer = "v1"
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                responseText = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use(BufferedReader::readText)
                val json = JSONObject(responseText)
                val modelsArray = json.optJSONArray("models")
                var selectedModel = ""
                if (modelsArray != null) {
                    val supportedList = mutableListOf<String>()
                    for (i in 0 until modelsArray.length()) {
                        val mObj = modelsArray.optJSONObject(i) ?: continue
                        val mName = mObj.optString("name", "").removePrefix("models/")
                        val methods = mObj.optJSONArray("supportedGenerationMethods")
                        var canGenerate = false
                        if (methods != null) {
                            for (j in 0 until methods.length()) {
                                if (methods.optString(j) == "generateContent") canGenerate = true
                            }
                        }
                        if (canGenerate) supportedList.add(mName)
                    }

                    selectedModel = supportedList.firstOrNull { it.contains("gemini-2.0-flash") }
                        ?: supportedList.firstOrNull { it.contains("gemini-2.5-flash") }
                        ?: supportedList.firstOrNull { it.contains("gemini-1.5-flash-latest") }
                        ?: supportedList.firstOrNull { it.contains("gemini-1.5-flash") }
                        ?: supportedList.firstOrNull { it.contains("gemini-1.5-pro") }
                        ?: supportedList.firstOrNull { it.contains("flash") }
                        ?: supportedList.firstOrNull() ?: "gemini-2.0-flash"
                } else {
                    selectedModel = "gemini-2.0-flash"
                }

                activeGoogleModel = selectedModel
                activeGoogleApiVersion = apiVer
                KeyCheckResult(true, "Gemini", selectedModel, "Gemini ($selectedModel) • Active")
            } else {
                val errorStream = connection.errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use(BufferedReader::readText) ?: ""
                val errorMsg = try {
                    JSONObject(errorStream).optJSONObject("error")?.optString("message") ?: errorStream
                } catch (e: Exception) {
                    errorStream.ifBlank { "HTTP $responseCode" }
                }
                KeyCheckResult(false, "Gemini", "", "Google API Error ($responseCode): $errorMsg")
            }
        } catch (e: Exception) {
            KeyCheckResult(false, "Gemini", "", "Google Connection Error: ${e.localizedMessage ?: "Network failed"}")
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Check and validate Groq / OpenAI / xAI API key.
     */
    suspend fun checkGrokKey(apiKey: String = effectiveGrokApiKey): KeyCheckResult = withContext(Dispatchers.IO) {
        val clean = cleanKey(apiKey)
        if (clean.isBlank()) {
            return@withContext KeyCheckResult(false, "Groq", "", "Groq/OpenAI key is not configured.")
        }
        var connection: HttpURLConnection? = null
        try {
            val isGroq = clean.startsWith("gsk_")
            val isOpenAi = clean.startsWith("sk-") && !clean.startsWith("sk-or-")
            val isXai = clean.startsWith("xai-")

            val modelsUrl = when {
                isGroq -> "https://api.groq.com/openai/v1/models"
                isOpenAi -> "https://api.openai.com/v1/models"
                isXai -> "https://api.x.ai/v1/models"
                clean.startsWith("sk-or-") -> "https://openrouter.ai/api/v1/models"
                else -> "https://api.groq.com/openai/v1/models"
            }

            connection = (URL(modelsUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $clean")
                connectTimeout = 10000
                readTimeout = 15000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use(BufferedReader::readText)
                var chosenModel = ""
                val providerName = when {
                    isGroq -> "Groq"
                    isOpenAi -> "OpenAI"
                    isXai -> "xAI"
                    else -> "AI"
                }

                if (isGroq) {
                    val json = JSONObject(responseText)
                    val data = json.optJSONArray("data")
                    val modelList = mutableListOf<String>()
                    if (data != null) {
                        for (i in 0 until data.length()) {
                            val id = data.optJSONObject(i)?.optString("id", "") ?: ""
                            if (id.isNotBlank()) modelList.add(id)
                        }
                    }
                    chosenModel = modelList.firstOrNull { it.contains("openai/gpt-oss-120b") }
                        ?: modelList.firstOrNull { it.contains("openai/gpt-oss-20b") }
                        ?: modelList.firstOrNull { it.contains("llama-3.1-8b-instant") }
                        ?: modelList.firstOrNull { it.contains("llama3-70b-8192") }
                        ?: GROQ_MODEL_OPENAI_120B

                    activeGroqModel = chosenModel
                } else if (isOpenAi) {
                    chosenModel = "gpt-4o-mini"
                    activeGroqModel = chosenModel
                } else if (isXai) {
                    chosenModel = "grok-2-latest"
                    activeGroqModel = chosenModel
                } else {
                    chosenModel = "custom"
                }

                KeyCheckResult(true, providerName, chosenModel, "$providerName ($chosenModel) • Active")
            } else {
                val errorStream = connection.errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use(BufferedReader::readText) ?: ""
                val errorMsg = try {
                    JSONObject(errorStream).optJSONObject("error")?.optString("message") ?: errorStream
                } catch (e: Exception) {
                    errorStream.ifBlank { "HTTP $responseCode" }
                }
                KeyCheckResult(false, if (isGroq) "Groq" else "AI", "", "API Error ($responseCode): $errorMsg")
            }
        } catch (e: Exception) {
            KeyCheckResult(false, "AI", "", "Connection Error: ${e.localizedMessage ?: "Network failed"}")
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Simultaneously check BOTH Google Gemini and Groq/OpenAI keys in parallel.
     * Returns individual validation statuses for each key and an overall operational summary.
     */
    suspend fun checkBothKeys(
        googleKey: String = effectiveGoogleApiKey,
        grokKey: String = effectiveGrokApiKey
    ): DualKeyStatus = withContext(Dispatchers.IO) {
        val googleReady = googleKey.isNotBlank()
        val grokReady = grokKey.isNotBlank()

        val gDeferred = async {
            if (googleReady) checkGoogleKey(googleKey) else KeyCheckResult(false, "Gemini", "", "No Google key entered")
        }
        val grDeferred = async {
            if (grokReady) checkGrokKey(grokKey) else KeyCheckResult(false, "Groq", "", "No Groq/OpenAI key entered")
        }

        val gRes = gDeferred.await()
        val grRes = grDeferred.await()

        val hasAny = gRes.isValid || grRes.isValid

        val overallMsg = when {
            gRes.isValid && grRes.isValid -> "Gemini & Groq • Active"
            gRes.isValid -> "${gRes.provider} (${gRes.activeModel}) • Active"
            grRes.isValid -> "${grRes.provider} (${grRes.activeModel}) • Active"
            !googleReady && !grokReady -> "Offline Mode • Tap to set API Key"
            googleReady && grokReady -> "⚠️ Both Keys Invalid • Tap to fix"
            googleReady -> "⚠️ Google Key Invalid • Tap to fix"
            grokReady -> "⚠️ Groq Key Invalid • Tap to fix"
            else -> "Offline Mode • Tap to set API Key"
        }

        DualKeyStatus(
            googleResult = gRes,
            grokResult = grRes,
            hasAnyActive = hasAny,
            overallMessage = overallMsg
        )
    }

    /**
     * Auto check active connection across BOTH keys.
     * The chatbot is fully active if AT LEAST ONE key is valid!
     */
    suspend fun autoCheckActiveConnection(): KeyCheckResult = withContext(Dispatchers.IO) {
        val dual = checkBothKeys()
        val provider = when {
            dual.googleResult.isValid && dual.grokResult.isValid -> "Gemini & Groq"
            dual.googleResult.isValid -> dual.googleResult.provider
            dual.grokResult.isValid -> dual.grokResult.provider
            else -> "Offline"
        }
        val activeModel = when {
            dual.googleResult.isValid -> dual.googleResult.activeModel
            dual.grokResult.isValid -> dual.grokResult.activeModel
            else -> ""
        }
        KeyCheckResult(
            isValid = dual.hasAnyActive,
            provider = provider,
            activeModel = activeModel,
            message = dual.overallMessage
        )
    }

    /**
     * Google Gemini API with intelligent multi-model / multi-version fallback cascade.
     */
    suspend fun askGoogle(
        prompt: String,
        imageBase64: String? = null,
        mimeType: String = "image/jpeg",
        history: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = effectiveGoogleApiKey
        if (apiKey.isBlank()) {
            return@withContext "Google Gemini API Key is not set! Please add your key in Settings or tap 🔑 in Ask Porua."
        }

        val candidates = mutableListOf<Pair<String, String>>()
        candidates.add(activeGoogleApiVersion to activeGoogleModel)
        val defaultCandidates = listOf(
            "v1beta" to "gemini-2.0-flash",
            "v1beta" to "gemini-2.5-flash",
            "v1" to "gemini-1.5-flash",
            "v1beta" to "gemini-1.5-flash-latest",
            "v1" to "gemini-2.0-flash",
            "v1beta" to "gemini-1.5-flash",
            "v1beta" to "gemini-1.5-pro"
        )
        for (c in defaultCandidates) {
            if (!candidates.contains(c)) candidates.add(c)
        }

        var lastError = ""
        for ((ver, mod) in candidates) {
            val result = executeGoogleRequest(mod, ver, prompt, imageBase64, mimeType, history)
            if (!result.startsWith("Google API Error") && !result.startsWith("Google API Connection Error")) {
                activeGoogleApiVersion = ver
                activeGoogleModel = mod
                return@withContext result
            }
            lastError = result
            if (result.contains("API_KEY_INVALID") || result.contains("PERMISSION_DENIED") || result.contains("(400)") || result.contains("(403)")) {
                return@withContext result
            }
        }
        lastError
    }

    private fun executeGoogleRequest(
        model: String,
        apiVersion: String = activeGoogleApiVersion,
        prompt: String,
        imageBase64: String?,
        mimeType: String,
        history: List<Pair<String, String>> = emptyList()
    ): String {
        var connection: HttpURLConnection? = null
        val apiKey = effectiveGoogleApiKey
        return try {
            val encodedKey = java.net.URLEncoder.encode(apiKey, "UTF-8")
            val endpoint = "https://generativelanguage.googleapis.com/$apiVersion/models/$model:generateContent?key=$encodedKey"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("x-goog-api-key", apiKey)
                doOutput = true
                connectTimeout = 15000
                readTimeout = 30000
            }

            val requestJson = JSONObject().apply {
                // System Instruction (v1beta/v1 camelCase)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", SYSTEM_PROMPT))
                    })
                })

                val contentsArray = JSONArray()

                // Append past turns (up to last 6 messages) for multi-turn context
                val safeHistory = history.takeLast(6)
                for (item in safeHistory) {
                    val turnRole = if (item.first.equals("model", ignoreCase = true) || item.first.equals("assistant", ignoreCase = true)) "model" else "user"
                    contentsArray.put(JSONObject().apply {
                        put("role", turnRole)
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", item.second))
                        })
                    })
                }

                // Append current user prompt & image
                val currentParts = JSONArray().apply {
                    put(JSONObject().put("text", prompt))
                    if (!imageBase64.isNullOrBlank()) {
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", mimeType)
                                put("data", imageBase64)
                            })
                        })
                    }
                }
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", currentParts)
                })

                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("maxOutputTokens", 1024)
                })
            }

            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use(BufferedReader::readText)
                val json = JSONObject(responseText)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val sb = StringBuilder()
                        for (i in 0 until parts.length()) {
                            val partObj = parts.optJSONObject(i)
                            if (partObj != null) {
                                sb.append(partObj.optString("text", ""))
                            }
                        }
                        val result = sb.toString().trim()
                        if (result.isNotBlank()) result else "Google API Error: No response generated."
                    } else {
                        "Google API Error: No response parts returned."
                    }
                } else {
                    "Google API Error: No response candidate returned."
                }
            } else {
                val errorText = connection.errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use(BufferedReader::readText) ?: "Unknown Error"
                val errorDetail = try {
                    JSONObject(errorText).optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() } ?: errorText
                } catch (e: Exception) {
                    errorText
                }
                "Google API Error ($responseCode): $errorDetail"
            }
        } catch (e: Exception) {
            "Google API Connection Error: ${e.localizedMessage ?: "Network failed"}"
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Groq / OpenAI Compatible API: Uses OpenAI models on Groq (openai/gpt-oss-120b & 20b),
     * native OpenAI (gpt-4o-mini), xAI (grok-2), with fast fallback to llama-3.1-8b-instant.
     */
    suspend fun askGrok(
        prompt: String,
        imageBase64: String? = null,
        mimeType: String = "image/jpeg",
        history: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = effectiveGrokApiKey
        if (apiKey.isBlank()) {
            return@withContext "Groq / OpenAI API Key is not set! Please add your key in Settings > AI Configuration or tap 🔑 in Ask Porua."
        }

        val isGroq = apiKey.startsWith("gsk_")
        val isXai = apiKey.startsWith("xai-")
        val isOpenRouter = apiKey.startsWith("sk-or-")
        val isOpenAi = apiKey.startsWith("sk-") && !isOpenRouter
        val hasImage = !imageBase64.isNullOrBlank()

        val candidates = when {
            isGroq && hasImage -> listOf(GROQ_VISION_MODEL)
            isGroq -> listOf(
                activeGroqModel,
                GROQ_MODEL_OPENAI_120B,
                GROQ_MODEL_OPENAI_20B,
                GROQ_MODEL_FAST,
                "llama-3.1-70b-versatile",
                "llama3-70b-8192"
            ).distinct()
            isXai -> listOf("grok-2-latest")
            isOpenAi -> listOf("gpt-4o-mini", "gpt-4o")
            isOpenRouter -> listOf("openai/gpt-4o-mini", "meta-llama/llama-3.3-70b-instruct")
            else -> listOf(activeGroqModel, GROQ_MODEL_OPENAI_120B, GROQ_MODEL_FAST).distinct()
        }

        var lastError = ""
        for (model in candidates) {
            val result = executeGrokRequest(model, prompt, imageBase64, mimeType, history)
            if (!result.startsWith("Grok API Error") && !result.startsWith("Grok API Connection Error")) {
                if (isGroq && !hasImage) {
                    activeGroqModel = model
                }
                return@withContext result
            }
            lastError = result
            if (result.contains("401") || result.contains("403") || result.contains("invalid_api_key")) {
                return@withContext result
            }
        }
        lastError
    }

    private fun executeGrokRequest(
        model: String,
        prompt: String,
        imageBase64: String?,
        mimeType: String,
        history: List<Pair<String, String>> = emptyList()
    ): String {
        var connection: HttpURLConnection? = null
        val apiKey = effectiveGrokApiKey
        return try {
            // Auto-detect Groq, OpenRouter, OpenAI, or xAI endpoint based on API key prefix
            val endpointUrl = when {
                apiKey.startsWith("gsk_") -> "https://api.groq.com/openai/v1/chat/completions"
                apiKey.startsWith("xai-") -> "https://api.x.ai/v1/chat/completions"
                apiKey.startsWith("sk-or-") -> "https://openrouter.ai/api/v1/chat/completions"
                apiKey.startsWith("sk-") -> "https://api.openai.com/v1/chat/completions"
                else -> "https://api.groq.com/openai/v1/chat/completions"
            }

            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Authorization", "Bearer $apiKey")
                if (endpointUrl.contains("openrouter")) {
                    setRequestProperty("HTTP-Referer", "https://porua.app")
                    setRequestProperty("X-Title", "Porua Educational App")
                }
                doOutput = true
                connectTimeout = 15000
                readTimeout = 30000
            }

            val requestJson = JSONObject().apply {
                put("model", model)
                put("temperature", 0.3)
                put("max_tokens", 1024)
                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", SYSTEM_PROMPT)
                    })

                    // Append past turns (up to last 6 messages) for multi-turn context
                    val safeHistory = history.takeLast(6)
                    for (item in safeHistory) {
                        val turnRole = if (item.first.equals("model", ignoreCase = true) || item.first.equals("assistant", ignoreCase = true)) "assistant" else "user"
                        put(JSONObject().apply {
                            put("role", turnRole)
                            put("content", item.second)
                        })
                    }

                    val userMessageObj = JSONObject().apply {
                        put("role", "user")
                        if (!imageBase64.isNullOrBlank()) {
                            val contentParts = JSONArray().apply {
                                put(JSONObject().apply {
                                    put("type", "text")
                                    put("text", prompt)
                                })
                                put(JSONObject().apply {
                                    put("type", "image_url")
                                    put("image_url", JSONObject().apply {
                                        put("url", "data:$mimeType;base64,$imageBase64")
                                    })
                                })
                            }
                            put("content", contentParts)
                        } else {
                            put("content", prompt)
                        }
                    }
                    put(userMessageObj)
                }
                put("messages", messagesArray)
            }

            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use(BufferedReader::readText)
                val json = JSONObject(responseText)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val firstChoice = choices.optJSONObject(0)
                    val message = firstChoice?.optJSONObject("message")
                    val content = message?.optString("content")?.takeIf { it.isNotBlank() }
                        ?: firstChoice?.optString("reasoning")?.takeIf { it.isNotBlank() }
                    if (!content.isNullOrBlank()) content else "Grok API Error: No response message."
                } else {
                    "Grok API Error: No response choices returned."
                }
            } else {
                val errorText = connection.errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use(BufferedReader::readText) ?: "Unknown Error"
                val errorDetail = try {
                    JSONObject(errorText).optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() } ?: errorText
                } catch (e: Exception) {
                    errorText
                }
                "Grok API Error ($responseCode): $errorDetail"
            }
        } catch (e: Exception) {
            "Grok API Connection Error: ${e.localizedMessage ?: "Network failed"}"
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Built-in Intelligent Offline NCTB Tutor:
     * Provides structured, step-by-step educational explanations based on NCTB curriculum topics
     * even when the device is completely offline or before API keys are configured.
     */
    fun getOfflineTutorResponse(prompt: String): String {
        val lower = prompt.lowercase()

        return when {
            // Greetings & Introductions
            lower in listOf("hi", "hello", "hey", "assalamu alaikum", "assalamualaikum") ||
            lower.startsWith("hi ") || lower.startsWith("hello ") || lower.startsWith("hey ") -> {
                """Hello! 👋 I'm Porua AI, your 24/7 personal study tutor for the NCTB curriculum.

Feel free to ask me any question in Physics, Chemistry, Higher Math, Biology, ICT, or English. You can also snap a photo of any textbook problem or formula, and I will explain it step by step!

• Tip: Ask any specific concept, derivation, numerical problem, or past board question to get an instant step-by-step solution."""
            }

            lower.contains("who are you") || lower.contains("about porua") -> {
                """I am Porua AI — a smart AI educational tutor built specifically for students in Bangladesh preparing for HSC (Class 11 & 12) board exams.

My Core Strengths:
1. Step-by-step solutions for Physics, Chemistry, Math, Biology, and ICT aligned with the NCTB curriculum.
2. Derivations, SI units, and board exam tips.
3. Personalized study guidance and instant practice review."""
            }

            // Formula Sheet / Formula Cheat Sheet
            lower.contains("formula sheet") || lower.contains("formula") || lower.contains("cheat sheet") -> {
                """Step 1 — Core Physics Formulas:
• Vectors Resultant: R = √(P² + Q² + 2PQ cos α), tan θ = (Q sin α) / (P + Q cos α)
• Newton's 2nd Law: F = m × a, Impulse J = F × Δt = m(v - u)
• Work & Energy: W = F × s cos θ, Kinetic Energy Ek = 1/2 m v²
• Ohm's Law: V = I × R, Power P = V × I = I²R

Step 2 — Core Chemistry Formulas:
• Cell Potential: E°cell = E°cathode - E°anode
• pH & pOH: pH = -log₁₀[H⁺], pH + pOH = 14 (at 25 °C)
• Buffer Equation: pH = pKa + log₁₀([Salt] / [Acid])
• Ideal Gas Law: P × V = n × R × T

Step 3 — Core Math Formulas:
• Derivative: d/dx(xⁿ) = n xⁿ⁻¹, d/dx(sin x) = cos x, d/dx(ln x) = 1/x
• Integral: ∫ xⁿ dx = (xⁿ⁺¹)/(n+1) + C, ∫ (1/x) dx = ln|x| + C
(NCTB HSC Complete Formula Guide)"""
            }

            // Daniell Cell
            lower.contains("daniell") || (lower.contains("zn") && lower.contains("cu")) -> {
                """Step 1 — Identify the Half-Cells:
Anode (Oxidation): Zn(s) -> Zn²⁺(aq) + 2e⁻, E°(Zn²⁺/Zn) = -0.76 V
Cathode (Reduction): Cu²⁺(aq) + 2e⁻ -> Cu(s), E°(Cu²⁺/Cu) = +0.34 V

Step 2 — Apply the Standard Cell Potential Formula:
E°cell = E°cathode - E°anode
E°cell = (+0.34 V) - (-0.76 V) = +1.10 V

Step 3 — NCTB Exam Takeaway:
Since E°cell > 0, the redox reaction is spontaneous. Zinc acts as the reducing agent and copper ions act as the oxidizing agent.
(NCTB HSC Chemistry 2nd Paper, Chapter 4: Electrochemistry)"""
            }

            // Standard Hydrogen Electrode (Word boundary \b to prevent matching "sheet")
            Regex("\\b(she)\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower) ||
            lower.contains("hydrogen electrode") || lower.contains("reference electrode") -> {
                """Step 1 — Definition & Purpose:
The Standard Hydrogen Electrode (SHE) is the universal primary reference electrode used to measure standard electrode potentials of all elements.

Step 2 — Standard Conditions:
• Platinum foil coated with platinum black (Pt)
• Hydrogen gas bubbled at 1 atm pressure (25 °C / 298 K)
• Acid solution with H⁺ ion concentration of 1.0 M (1 mol/L)

Step 3 — NCTB Exam Takeaway:
By international agreement (IUPAC), its potential is arbitrarily assigned as exactly 0.00 V.
Reaction: 2H⁺(aq, 1 M) + 2e⁻ ⇌ H₂(g, 1 atm), E° = 0.00 V
(NCTB HSC Chemistry 2nd Paper, Chapter 4: Electrochemistry)"""
            }

            // Lucas Test & Alcohols
            lower.contains("lucas") || (lower.contains("alcohol") && lower.contains("test")) -> {
                """Step 1 — Reagent Composition:
Lucas reagent is an equimolar solution of concentrated Hydrochloric acid (conc. HCl) and anhydrous Zinc Chloride (anh. ZnCl₂).

Step 2 — Identification Principle:
Alcohols react with Lucas reagent to form insoluble alkyl chlorides, which appear as white cloudiness/turbidity:
• Tertiary (3°) Alcohol: Turbidity appears immediately (< 10 seconds).
• Secondary (2°) Alcohol: Turbidity appears after 5–10 minutes.
• Primary (1°) Alcohol: No turbidity at room temperature; reacts only upon heating.

Step 3 — NCTB HSC Importance:
This test is essential for identifying and differentiating 1°, 2°, and 3° alcohols in board practicals and theory.
(NCTB HSC Chemistry 2nd Paper, Chapter 2: Organic Chemistry)"""
            }

            // Galvanic Cell Notation
            lower.contains("notation") || lower.contains("galvanic") || lower.contains("cell notation") -> {
                """Step 1 — Fundamental IUPAC Rule:
In electrochemical cell notation:
• The Anode (oxidation half-cell) is always placed on the extreme LEFT.
• The Cathode (reduction half-cell) is always placed on the extreme RIGHT.

Step 2 — Salt Bridge & Phase Boundaries:
• Single vertical line (|) represents a phase boundary (e.g., solid metal | liquid solution).
• Double vertical line (||) represents the salt bridge.
Example: Zn(s) | Zn²⁺(1 M) || Cu²⁺(1 M) | Cu(s)

Step 3 — NCTB Exam Takeaway:
Always list phase sequence from left to right: Anode metal | Anode electrolyte || Cathode electrolyte | Cathode metal.
(NCTB HSC Chemistry 2nd Paper, Chapter 4: Electrochemistry)"""
            }

            // Vectors & Resultant
            lower.contains("vector") || lower.contains("resultant") -> {
                """Step 1 — Vector Addition (Parallelogram Law):
For two vectors P and Q acting at an angle α:
Resultant magnitude: R = √(P² + Q² + 2PQ cos α)

Step 2 — Direction Formula:
tan θ = (Q sin α) / (P + Q cos α)
where θ is the angle between resultant R and vector P.

Step 3 — NCTB Exam Special Cases:
• α = 0° (same direction): R_max = P + Q
• α = 180° (opposite direction): R_min = |P - Q|
• α = 90° (perpendicular): R = √(P² + Q²)
(NCTB HSC Physics 1st Paper, Chapter 2: Vectors)"""
            }

            // Newton's Laws & Dynamics
            lower.contains("newton") || lower.contains("laws of motion") || lower.contains("inertia") || lower.contains("f=ma") -> {
                """Step 1 — The Three Laws of Motion:
1. 1st Law (Law of Inertia): An object remains at rest or uniform motion unless acted upon by an external net force.
2. 2nd Law (Force & Momentum): Rate of change of momentum is proportional to applied force: F = m × a.
3. 3rd Law (Action-Reaction): Every action has an equal and opposite reaction: F₁₂ = -F₂₁.

Step 2 — Impulse & Momentum Conservation:
• Impulse of Force: J = F × Δt = m(v - u) = Δp
• Law of Conservation of Linear Momentum: m₁u₁ + m₂u₂ = m₁v₁ + m₂v₂

Step 3 — NCTB Exam Takeaway:
Recoil velocity of gun: V = -(m × v) / M.
(NCTB HSC Physics 1st Paper, Chapter 4: Newtonian Mechanics)"""
            }

            // Acids, Bases & Buffer Solutions
            lower.contains("acid") || lower.contains("base") || lower.contains("ph") || lower.contains("buffer") -> {
                """Step 1 — pH & pOH Concept:
• pH is the negative logarithm of hydrogen ion concentration: pH = -log₁₀[H⁺]
• In pure water at 25 °C, Kw = [H⁺][OH⁻] = 10⁻¹⁴, so pH + pOH = 14.

Step 2 — Buffer Solutions (Henderson-Hasselbalch Equation):
For an acidic buffer (Weak acid + its salt):
pH = pKa + log₁₀([Salt] / [Acid])
For a basic buffer:
pOH = pKb + log₁₀([Salt] / [Base])

Step 3 — NCTB Exam Takeaway:
Buffer capacity is highest when [Salt] = [Acid], meaning pH = pKa. Human blood maintains pH at 7.4 via bicarbonate buffer.
(NCTB HSC Chemistry 1st Paper, Chapter 4: Chemical Changes)"""
            }

            // Organic Chemistry & Isomerism
            lower.contains("organic") || lower.contains("isomerism") || lower.contains("alkane") || lower.contains("alkene") -> {
                """Step 1 — Carbon Hybridization & Bonding:
• Single bond (Alkane): sp³ hybridization (Tetrahedral, 109.5°)
• Double bond (Alkene): sp² hybridization (Trigonal planar, 120°)
• Triple bond (Alkyne): sp hybridization (Linear, 180°)

Step 2 — Types of Isomerism:
1. Structural Isomerism: Chain, Positional, Functional, Metamerism, Tautomerism.
2. Stereoisomerism: Geometrical (Cis-Trans) and Optical (d- and l- enantiomers with chiral carbon).

Step 3 — NCTB Exam Takeaway:
Markovnikov's rule: Electrophilic addition of HBr to asymmetric alkene places H on the carbon with more hydrogen atoms ("The rich get richer").
(NCTB HSC Chemistry 2nd Paper, Chapter 2: Organic Chemistry)"""
            }

            // Calculus: Differentiation
            lower.contains("differentiation") || lower.contains("derivative") || lower.contains("d/dx") -> {
                """Step 1 — Definition:
Differentiation measures the instantaneous rate of change of a function:
f'(x) = lim (h -> 0) [f(x+h) - f(x)] / h

Step 2 — Core Derivative Formulas:
• d/dx(xⁿ) = n × xⁿ⁻¹
• d/dx(sin x) = cos x,  d/dx(cos x) = -sin x
• d/dx(eˣ) = eˣ,  d/dx(ln x) = 1/x
• Product Rule: d/dx(u × v) = u(dv/dx) + v(du/dx)
• Quotient Rule: d/dx(u / v) = [v(du/dx) - u(dv/dx)] / v²

Step 3 — NCTB Exam Takeaway:
To find local maximum or minimum of y = f(x): Set dy/dx = 0, then check d²y/dx² (< 0 for maximum, > 0 for minimum).
(NCTB HSC Higher Math 1st Paper, Chapter 9: Differentiation)"""
            }

            // Calculus: Integration
            lower.contains("integration") || lower.contains("integral") || lower.contains("∫") -> {
                """Step 1 — Concept:
Integration is the inverse operation of differentiation (finding the anti-derivative or area under a curve).

Step 2 — Core Integral Formulas:
• ∫ xⁿ dx = (xⁿ⁺¹) / (n + 1) + C  (for n ≠ -1)
• ∫ (1/x) dx = ln|x| + C
• ∫ eˣ dx = eˣ + C
• ∫ sin x dx = -cos x + C,  ∫ cos x dx = sin x + C
• Integration by Parts: ∫ u × v dx = u ∫ v dx - ∫ [ (du/dx) × ∫ v dx ] dx

Step 3 — NCTB Exam Takeaway:
Remember the LIATE rule for selecting 'u' in Integration by Parts:
Logarithmic > Inverse Trig > Algebraic > Trigonometric > Exponential.
(NCTB HSC Higher Math 1st Paper, Chapter 10: Integration)"""
            }

            // Ohm's Law & Electricity
            lower.contains("ohm") || lower.contains("resistance") -> {
                """Step 1 — Ohm's Law:
At constant temperature, the current (I) flowing through a conductor is directly proportional to the potential difference (V) across its ends:
V = I × R  ⇒  I = V / R

Step 2 — Resistors in Combinations:
• Series Combination: R_s = R₁ + R₂ + R₃ + ...
• Parallel Combination: 1/R_p = (1/R₁) + (1/R₂) + ...  (For two resistors: R_p = (R₁ × R₂) / (R₁ + R₂))

Step 3 — NCTB Exam Takeaway:
Resistance depends on dimensions: R = ρ × (L / A). Temperature coefficient of resistance: R_t = R₀(1 + α × t).
(NCTB HSC Physics 2nd Paper, Chapter 3: Current Electricity)"""
            }

            else -> {
                val cleanPrompt = if (prompt.length > 50) prompt.take(50) + "..." else prompt
                """Porua AI — Step-by-Step Study Guidance:
Your question: "$cleanPrompt"

How to solve this problem:
1. Identify the given values, constants, and target unknowns along with their standard SI units.
2. Apply the relevant formulas and governing laws from your NCTB syllabus textbook.
3. Perform the algebraic steps methodically to arrive at the final answer with appropriate units.

• Tip: Ask specifically about topics like Vectors, Ohm's Law, Newton's Laws, Daniell Cell, Acids and Bases, Differentiation, or Integration for in-depth solutions!"""
            }
        }
    }

    /**
     * Sanitizes educational responses to strictly ensure:
     * - No asterisks (* or **)
     * - No markdown hashtags (# or ##)
     * - No raw table dividers or borders (| or |---|)
     * - Clean, readable plain text
     */
    fun sanitizeEducationalResponse(rawText: String): String {
        // Strip code block fence tags (```json, ```markdown, ```) while keeping code content
        var text = rawText
            .replace(Regex("```[a-zA-Z]*\\n?"), "")
            .replace("```", "")

        // Remove HTML tags if model outputs <br>, <p>, <b>, etc.
        text = text.replace(Regex("<[^>]*>"), "")

        val lines = text.lines()
        val cleaned = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()

            // Skip markdown table dividers (|---|---|)
            if (trimmed.startsWith("|") && (trimmed.contains("---") || trimmed.contains("==="))) continue

            // Convert table row to bullet point
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                val cols = trimmed.split("|")
                    .map { it.replace("*", "").trim() }
                    .filter { it.isNotBlank() }
                if (cols.isNotEmpty()) {
                    if (cols.size == 1) {
                        cleaned.add("• ${cols[0]}")
                    } else if (cols.size == 2) {
                        cleaned.add("• ${cols[0]}: ${cols[1]}")
                    } else {
                        cleaned.add("• ${cols[0]}: ${cols.subList(1, cols.size).joinToString(" — ")}")
                    }
                    continue
                }
            }

            // Remove leading markdown headers (###, ##, #)
            var cleanLine = trimmed.replace(Regex("^#+\\s*"), "")

            // Remove markdown blockquote symbols (> )
            cleanLine = cleanLine.replace(Regex("^>+\\s*"), "")

            // Remove all markdown asterisks (* and **)
            cleanLine = cleanLine.replace("*", "")

            // Remove markdown underscore italics/bolds (_text_ -> text)
            cleanLine = cleanLine.replace(Regex("(?<=\\s|^)_(.+?)_(?=\\s|\$)"), "$1")

            // Remove stray markdown bracket citations like [1], [2]
            cleanLine = cleanLine.replace(Regex("\\[\\d+\\]"), "")

            // Clean multiple consecutive horizontal whitespace
            cleanLine = cleanLine.replace(Regex("[ \\t]+"), " ")

            cleaned.add(cleanLine)
        }

        // Collapse excessive consecutive blank lines into a single blank line
        return cleaned.joinToString("\n").replace(Regex("\\n{3,}"), "\n\n").trim()
    }
}
