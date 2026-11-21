package com.porua.app.data.ai

import kotlinx.coroutines.Dispatchers
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

    // === MODELS ===
    const val GEMINI_MODEL = "gemini-flash-lite-latest"
    const val GEMINI_FALLBACK_MODEL = "gemini-flash-latest"
    const val GROK_MODEL = "openai/gpt-oss-120b"
    const val GROQ_VISION_MODEL = "qwen/qwen3.8-27b"
    const val GROQ_FAST_FALLBACK = "openai/gpt-oss-20b"
    const val GROK_ENDPOINT = "https://api.x.ai/v1/chat/completions"

    // === IN-MEMORY CACHE (Fast 0ms response for repeated queries) ===
    private val queryMemoryCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    fun clearCache() {
        queryMemoryCache.clear()
    }

    // === SYSTEM PROMPT FOR PORUA APP ===
    const val SYSTEM_PROMPT = """You are Porua AI, an academic study tutor for students in Bangladesh preparing for the NCTB HSC curriculum exams (Class 11 & 12).

RULES:
1. STRICT ENGLISH ONLY:
- Always reply strictly in English. Never use Bengali or Banglish in answers.

2. ABSOLUTELY NO ASTERISKS OR MARKDOWN STARS:
- Never use asterisks (*, **, or ***) anywhere in your response.
- Do NOT bold text using asterisks (write "Answer:" instead of "**Answer:**").
- Never use hashtags (# or ##) for titles.
- Never use raw markdown table pipes (| or |---|).
- Write in clean, plain sentences and simple bullet points (- or •) with clean line breaks.

3. SIMPLE, DIRECT, AND CONCISE:
- Answer simply, clearly, and directly what was asked. Avoid unnecessary clutter, verbose filler, or repetitive headers.

4. OUT-OF-SUBJECT / OFF-TOPIC QUERIES:
- If a student asks questions outside their academic syllabus or subjects (e.g., movies, gaming, entertainment, personal advice, gossip, jokes, or non-educational topics), handle it respectfully and politely.
- State that you are Porua AI, an academic tutor dedicated to NCTB curriculum subjects (Physics, Chemistry, Higher Math, Biology, ICT, English, Bangla, and Commerce), and invite them to ask any question regarding their academic syllabus or exam preparation."""

    /**
     * Unified AI caller:
     * 1. If an image is attached, Google Gemini handles vision / diagrams / OCR natively.
     * 2. Tries Google Gemini FIRST (gemini-flash-lite-latest, fast and reliable).
     * 3. If Google fails or rate limits, automatically falls back to Groq Cloud (openai/gpt-oss-120b).
     * 4. If offline or no network, falls back to Porua's intelligent offline NCTB tutor.
     * 5. Strips stray asterisks and markdown symbols so the answer is always clean plain text.
     */
    suspend fun askAi(prompt: String, imageBase64: String? = null, mimeType: String = "image/jpeg"): String = withContext(Dispatchers.IO) {
        val googleReady = GOOGLE_API_KEY.isNotBlank() && !GOOGLE_API_KEY.contains("YOUR_")
        val grokReady = GROK_API_KEY.isNotBlank() && !GROK_API_KEY.contains("YOUR_")

        val cacheKey = if (imageBase64.isNullOrBlank()) prompt.trim().lowercase() else ""
        if (cacheKey.isNotBlank()) {
            val cached = queryMemoryCache[cacheKey]
            if (!cached.isNullOrBlank()) {
                return@withContext cached
            }
        }

        // 1. Primary Provider: Google Gemini FIRST (Text & Vision)
        if (googleReady) {
            val googleResult = askGoogle(prompt, imageBase64, mimeType)
            if (!googleResult.startsWith("Google API Error") && !googleResult.startsWith("Google API Connection Error")) {
                val cleaned = sanitizeEducationalResponse(googleResult)
                if (cacheKey.isNotBlank()) queryMemoryCache[cacheKey] = cleaned
                return@withContext cleaned
            }
        }

        // 2. Secondary Provider / Fallback: Groq Cloud (openai/gpt-oss-120b)
        if (grokReady) {
            val grokResult = askGrok(prompt, imageBase64, mimeType)
            if (!grokResult.startsWith("Grok API Error") && !grokResult.startsWith("Grok API Connection Error")) {
                val cleaned = sanitizeEducationalResponse(grokResult)
                if (cacheKey.isNotBlank()) queryMemoryCache[cacheKey] = cleaned
                return@withContext cleaned
            }
        }

        // 3. Fallback: Intelligent Offline NCTB Tutor
        val offlineResult = sanitizeEducationalResponse(getOfflineTutorResponse(prompt))
        if (cacheKey.isNotBlank()) queryMemoryCache[cacheKey] = offlineResult
        offlineResult
    }

    /**
     * Google Gemini API (gemini-2.0-flash with fallback to gemini-1.5-flash)
     */
    suspend fun askGoogle(prompt: String, imageBase64: String? = null, mimeType: String = "image/jpeg"): String = withContext(Dispatchers.IO) {
        if (GOOGLE_API_KEY.contains("YOUR_") || GOOGLE_API_KEY.isBlank()) {
            return@withContext "Google API Key is not set! Please set GOOGLE_API_KEY in AiHelper.kt"
        }

        // Try primary model (gemini-2.0-flash), fallback to gemini-1.5-flash if needed
        val primaryResult = executeGoogleRequest(GEMINI_MODEL, prompt, imageBase64, mimeType)
        if (primaryResult.startsWith("Google API Error (404)") ||
            primaryResult.startsWith("Google API Error (400)") ||
            primaryResult.startsWith("Google API Error (429)") ||
            primaryResult.startsWith("Google API Error (503)")) {
            val fallbackResult = executeGoogleRequest(GEMINI_FALLBACK_MODEL, prompt, imageBase64, mimeType)
            if (!fallbackResult.startsWith("Google API Error") && !fallbackResult.startsWith("Google API Connection Error")) {
                return@withContext fallbackResult
            }
        }
        primaryResult
    }

    private fun executeGoogleRequest(model: String, prompt: String, imageBase64: String?, mimeType: String): String {
        var connection: HttpURLConnection? = null
        return try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$GOOGLE_API_KEY"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("x-goog-api-key", GOOGLE_API_KEY)
                doOutput = true
                connectTimeout = 15000
                readTimeout = 30000
            }

            val requestJson = JSONObject().apply {
                // System Instruction (v1beta camelCase)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", SYSTEM_PROMPT))
                    })
                })

                // Contents (Text + optional inlineData)
                val partsArray = JSONArray().apply {
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
                val contentsArray = JSONArray().apply {
                    put(JSONObject().put("parts", partsArray))
                }
                put("contents", contentsArray)

                // Generation Config: Low temperature for direct, factual, non-cluttered answers
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
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
     * Grok / Groq / OpenAI Compatible API (auto Groq flagship routing)
     */
    suspend fun askGrok(prompt: String, imageBase64: String? = null, mimeType: String = "image/jpeg"): String = withContext(Dispatchers.IO) {
        if (GROK_API_KEY.contains("YOUR_") || GROK_API_KEY.isBlank()) {
            return@withContext "Grok API Key is not set! Please set GROK_API_KEY in AiHelper.kt"
        }

        val isGroq = GROK_API_KEY.startsWith("gsk_")
        val hasImage = !imageBase64.isNullOrBlank()

        // On Groq Cloud:
        // - If image is attached, route to Groq Multimodal Vision: qwen/qwen3.8-27b
        // - Otherwise use flagship reasoning model: openai/gpt-oss-120b
        val targetModel = when {
            isGroq && hasImage -> GROQ_VISION_MODEL
            isGroq -> GROK_MODEL
            else -> GROK_MODEL
        }

        val primaryResult = executeGrokRequest(targetModel, prompt, imageBase64, mimeType)
        if (!primaryResult.startsWith("Grok API Error") && !primaryResult.startsWith("Grok API Connection Error")) {
            return@withContext primaryResult
        }

        // Fast fallback on Groq to openai/gpt-oss-20b if 120b was busy (do not retry if key is invalid / 401 / 403)
        if (isGroq && !hasImage && targetModel != GROQ_FAST_FALLBACK &&
            !primaryResult.contains("401") && !primaryResult.contains("403")) {
            val fastFallback = executeGrokRequest(GROQ_FAST_FALLBACK, prompt, imageBase64, mimeType)
            if (!fastFallback.startsWith("Grok API Error") && !fastFallback.startsWith("Grok API Connection Error")) {
                return@withContext fastFallback
            }
        }

        primaryResult
    }

    private fun executeGrokRequest(model: String, prompt: String, imageBase64: String?, mimeType: String): String {
        var connection: HttpURLConnection? = null
        return try {
            // Auto-detect Groq, OpenRouter or xAI endpoint based on API key prefix
            val endpointUrl = when {
                GROK_API_KEY.startsWith("gsk_") -> "https://api.groq.com/openai/v1/chat/completions"
                GROK_API_KEY.startsWith("sk-or-") -> "https://openrouter.ai/api/v1/chat/completions"
                else -> GROK_ENDPOINT
            }

            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Authorization", "Bearer $GROK_API_KEY")
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
                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", SYSTEM_PROMPT)
                    })

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
        val lines = rawText.lines()
        val cleaned = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()

            // Skip markdown table dividers
            if (trimmed.startsWith("|") && trimmed.contains("---")) continue

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

            // Remove all markdown asterisks (* and **)
            cleanLine = cleanLine.replace("*", "")

            cleaned.add(cleanLine)
        }

        return cleaned.joinToString("\n").trim()
    }
}
