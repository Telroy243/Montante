package com.example.data

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Base64
import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// --- Preferred model mappings ---
// "gemini-1.5-flash" is our high-speed default
// "gemini-1.5-pro" is our advanced reasoning default
object GeminiModels {
    const val FLASH_MODEL = "gemini-1.5-flash"
    const val PRO_MODEL = "gemini-1.5-pro"
}

// --- Moshi models for Gemini API ---

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiResponseSchema(
    val type: String,
    val properties: Map<String, GeminiSchemaProperty>? = null,
    val required: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiSchemaProperty(
    val type: String,
    val description: String? = null,
    val items: GeminiSchemaProperty? = null,
    val properties: Map<String, GeminiSchemaProperty>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: GeminiResponseSchema? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponseCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiResponseCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiErrorDetails(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorResponse(
    val error: GeminiErrorDetails? = null
)

// --- Domain structured outputs as parsed from Gemini ---

@JsonClass(generateAdapter = true)
data class CouponMatch(
    val teamHome: String,
    val teamAway: String,
    val prediction: String,
    val cote: Double
)

@JsonClass(generateAdapter = true)
data class VisualAnalysisResult(
    val matches: List<CouponMatch>,
    val globalCote: Double,
    val respectsInterval: Boolean,
    val riskAssessment: String, // "Faible", "Moyen", "Élevé"
    val isRiskAcceptable: Boolean,
    val criticism: String,
    val advice: String
)

@JsonClass(generateAdapter = true)
data class CrashTestResult(
    val status: String, // "GREEN", "ORANGE", "RED"
    val verdict: String,
    val reasons: List<String>
)

@JsonClass(generateAdapter = true)
data class StrategyReport(
    val title: String,
    val riskEvaluation: String,
    val progressAnalysis: String,
    val specificAdvice: String,
    val suggestedRules: List<String>,
    val alertMessage: String?
)

// --- Settings Manager (using SharedPreferences) ---

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("stadium_montante_prefs", Context.MODE_PRIVATE)

    var geminiApiKey: String
        get() = prefs.getString("gemini_api_key", "").orEmpty()
        set(value) = prefs.edit().putString("gemini_api_key", value).apply()

    var geminiModelLabel: String
        get() = prefs.getString("gemini_model_label", "gemini-1.5-flash").orEmpty()
        set(value) = prefs.edit().putString("gemini_model_label", value).apply()

    val actualModel: String
        get() = geminiModelLabel.trim().ifEmpty { "gemini-1.5-flash" }

    var isThemeStadium: Boolean
        get() = prefs.getBoolean("is_theme_stadium", true)
        set(value) = prefs.edit().putBoolean("is_theme_stadium", value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean("is_haptic_enabled", true)
        set(value) = prefs.edit().putBoolean("is_haptic_enabled", value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("is_sound_enabled", true)
        set(value) = prefs.edit().putBoolean("is_sound_enabled", value).apply()

    var activeCurrency: String
        get() = prefs.getString("active_currency", "CDF").orEmpty()
        set(value) = prefs.edit().putString("active_currency", value).apply()

    var exchangeRateUsdToCdf: Float
        get() = prefs.getFloat("exchange_rate_usd_to_cdf", 2500f)
        set(value) = prefs.edit().putFloat("exchange_rate_usd_to_cdf", value).apply()
}

// --- Sensorial Feedback Manager ---

class SensorialManager(private val context: Context, private val settingsManager: SettingsManager) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val toneGenerator = try {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    } catch (e: Exception) {
        null
    }

    fun vibrateShortDouble() {
        if (!settingsManager.isHapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Double vibration standard
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 100, 80), intArrayOf(0, 180, 0, 180), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 80, 100, 80), -1)
            }
        } catch (e: Exception) {
            Log.e("SensorialManager", "Vibrate error", e)
        }
    }

    fun vibrateLongSingle() {
        if (!settingsManager.isHapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(450, 220))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(450)
            }
        } catch (e: Exception) {
            Log.e("SensorialManager", "Vibrate error", e)
        }
    }

    fun vibratePulsingContinuous(durationMs: Long) {
        if (!settingsManager.isHapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.e("SensorialManager", "Vibrate error", e)
        }
    }

    fun playSoundWin() {
        if (!settingsManager.isSoundEnabled) return
        try {
            // Ding ding simulation with standard DTMF tones or notification tones
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_3, 100)
            Thread {
                Thread.sleep(120)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_6, 120)
            }.start()
        } catch (e: Exception) {
            Log.e("SensorialManager", "Sound error", e)
        }
    }

    fun playSoundLoss() {
        if (!settingsManager.isSoundEnabled) return
        try {
            // Low descending tone
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
        } catch (e: Exception) {
            Log.e("SensorialManager", "Sound error", e)
        }
    }

    fun playSoundBip() {
        if (!settingsManager.isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            Log.e("SensorialManager", "Sound error", e)
        }
    }
}

// --- Montante Repository (handles database & mathematical computations) ---

class MontanteRepository(
    private val montanteDao: MontanteDao,
    private val betDao: BetDao,
    private val settingsManager: SettingsManager,
    private val appBuildConfigKey: String = ""
) {
    val allMontantes: Flow<List<Montante>> = montanteDao.getAllMontantes()
    val activeMontanteFlow: Flow<Montante?> = montanteDao.getActiveMontanteFlow()

    fun getBetsForMontante(montanteId: Int): Flow<List<Bet>> = betDao.getBetsForMontante(montanteId)

    suspend fun getActiveMontante(): Montante? = montanteDao.getActiveMontante()

    suspend fun createMontante(name: String, capital: Double, goal: Double, minCote: Double, maxCote: Double): Long {
        val montante = Montante(
            name = name,
            capitalDepart = capital,
            objectifFinal = goal,
            coteMin = minCote,
            coteMax = maxCote,
            isActive = true
        )
        return montanteDao.createAndActivateMontante(montante)
    }

    suspend fun selectActiveMontante(id: Int) {
        montanteDao.selectActiveMontante(id)
    }

    suspend fun deleteMontante(montante: Montante) {
        montanteDao.deleteMontante(montante)
    }

    suspend fun addBet(bet: Bet) {
        betDao.insertBet(bet)
    }

    suspend fun updateBet(bet: Bet) {
        betDao.updateBet(bet)
    }

    suspend fun deleteBet(bet: Bet) {
        betDao.deleteBet(bet)
    }

    fun getAllBetsFlow(): Flow<List<Bet>> = betDao.getAllBetsFlow()

    /**
     * Compute current bankroll and step metadata for a montante.
     */
    fun computeBankrollState(montante: Montante, bets: List<Bet>): BankrollState {
        var currentBankroll = montante.capitalDepart
        var activeSeriesLoss = 0.0

        // Process bets in chronological order to simulate bankroll progress
        val sortedBets = bets.sortedBy { it.timestamp }
        for (bet in sortedBets) {
            when (bet.status) {
                "WON" -> {
                    currentBankroll += bet.mise * (bet.cote - 1.0)
                }
                "LOST" -> {
                    currentBankroll -= bet.mise
                }
                "PENDING" -> {
                    // Pending bets reduce the immediately available purse, or let's keep them in count
                    currentBankroll -= bet.mise
                }
            }
        }

        // Cumulative active series loss is defined as:
        // Sum of all lost bets since the most recent "WON" bet in this montante
        val lastWonIndex = sortedBets.indexOfLast { it.status == "WON" }
        val activeSeriesBets = if (lastWonIndex == -1) {
            sortedBets
        } else {
            sortedBets.subList(lastWonIndex + 1, sortedBets.size)
        }
        activeSeriesLoss = activeSeriesBets.filter { it.status == "LOST" }.sumOf { it.mise }

        // We target standard completion in 5 successful series.
        // Step target gain per series:
        val seriesTargetGain = (montante.objectifFinal - montante.capitalDepart) / 5.0

        return BankrollState(
            currentBankroll = currentBankroll,
            activeSeriesLoss = activeSeriesLoss,
            recommendedStepTargetGain = seriesTargetGain
        )
    }

    /**
     * Helper to compute recommended stake size 'Mise' for a certain target cote.
     * Formula: Mise = (TargetProfit + SeriesLosses) / (Cote - 1.0)
     */
    fun calculateRecommendedMise(cote: Double, bankrollState: BankrollState): Double {
        if (cote <= 1.0) return 1.0
        val targetGain = bankrollState.recommendedStepTargetGain
        val lossToRecover = bankrollState.activeSeriesLoss
        val recommended = (targetGain + lossToRecover) / (cote - 1.0)
        // Cap the recommended bet at the current bankroll to avoid overdraft
        return if (recommended > bankrollState.currentBankroll) {
            kotlin.math.max(1.0, bankrollState.currentBankroll)
        } else {
            kotlin.math.max(1.0, recommended)
        }
    }

    // --- Gemini Network Client ---

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getEffectiveApiKey(): String {
        // Preference key overrides BuildConfig fallback
        val savedKey = settingsManager.geminiApiKey.trim()
        return if (savedKey.isNotBlank()) savedKey else appBuildConfigKey.trim()
    }

    private fun parseApiError(code: Int, defaultMsg: String, bodyStr: String?): String {
        if (bodyStr.isNullOrBlank()) {
            return "Erreur API Gemini ($code) : $defaultMsg"
        }
        return try {
            val errorResponse = moshi.adapter(GeminiErrorResponse::class.java).fromJson(bodyStr)
            val extractedMessage = errorResponse?.error?.message
            if (!extractedMessage.isNullOrBlank()) {
                "Erreur $code : $extractedMessage"
            } else {
                "Erreur $code : $defaultMsg\n$bodyStr"
            }
        } catch (e: Exception) {
            "Erreur $code : $defaultMsg\n($bodyStr)"
        }
    }    private fun getUrlForModel(model: String, apiKey: String): String {
        val cleanModel = model.trim().ifEmpty { "gemini-1.5-flash" }
        val version = when (cleanModel) {
            "gemini-2.0-flash" -> "v1beta"
            else -> "v1"
        }
        return "https://generativelanguage.googleapis.com/$version/models/$cleanModel:generateContent?key=$apiKey"
    }

    /**
     * Calls Gemini to perform visually guided analyzing of a betting coupon (or ticket).
     */
    suspend fun analyzeCouponImage(
        bitmapBytesBase64: String,
        montante: Montante,
        bankrollState: BankrollState
    ): VisualAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            throw Exception("Clé API Gemini non configurée ! Veuillez la saisir dans les Paramètres.")
        }

        val activeModel = settingsManager.actualModel
        val url = getUrlForModel(activeModel, apiKey)

        val systemPrompt = """
            Tu es un analyste financier d'élite spécialisé dans le pari sportif et la gestion de Bankroll par montante.
            Tu dois analyser l'image d'un coupon (qui contient des matchs de foot ou autres, et des cotes) et vérifier s'il respecte les contraintes et s'il est statistiquement judicieux.
            
            Les règles de la montante de l'utilisateur sont (toutes les valeurs sont exprimées et calculées exclusivement en Franc Congolais - CDF / FC) :
            - Devise Active : CDF (FC)
            - Capital d'origine : ${montante.capitalDepart} FC
            - Bankroll actuelle disponible : ${bankrollState.currentBankroll} FC
            - Perte récente à récupérer de la série en cours : ${bankrollState.activeSeriesLoss} FC
            - Objectif final à atteindre : ${montante.objectifFinal} FC
            - Zone autorisée de côtes : Minimum ${montante.coteMin} et Maximum ${montante.coteMax}
            
            Tu DOIS analyser l'image et extraire les matchs, predictions, et cotes, puis vérifier la compatibilité.
            Tu DOIS retourner un rapport au format JSON STRICT respectant exactement ce schéma :
            {
              "matches": [
                {
                  "teamHome": "Nom équipe domicile",
                  "teamAway": "Nom équipe extérieur",
                  "prediction": "Le prono (ex: Victoire Paris SG ou Over 2.5)",
                  "cote": 1.45
                }
              ],
              "globalCote": 1.45,
              "respectsInterval": true, 
              "riskAssessment": "Faible", 
              "isRiskAcceptable": true,
              "criticism": "Une critique constructive et argumentée des matchs détectés.",
              "advice": "Le conseil stratégique précis (ex: 'Ce coupon propose une cote globale de 1.85, ce qui dépasse ton max de 1.60. Pour sécuriser, élimine le match Y et garde X pour descendre à 1.45.')"
            }
        """.trimIndent()

        val requestPayload = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "Analyse cette image de coupon de pari par rapport à ma montante.\n\n$systemPrompt"),
                        GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = bitmapBytesBase64))
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                responseSchema = GeminiResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "globalCote" to GeminiSchemaProperty(type = "NUMBER"),
                        "respectsInterval" to GeminiSchemaProperty(type = "BOOLEAN"),
                        "riskAssessment" to GeminiSchemaProperty(type = "STRING"),
                        "isRiskAcceptable" to GeminiSchemaProperty(type = "BOOLEAN"),
                        "criticism" to GeminiSchemaProperty(type = "STRING"),
                        "advice" to GeminiSchemaProperty(type = "STRING"),
                        "matches" to GeminiSchemaProperty(
                            type = "ARRAY",
                            items = GeminiSchemaProperty(
                                type = "OBJECT",
                                properties = mapOf(
                                    "teamHome" to GeminiSchemaProperty(type = "STRING"),
                                    "teamAway" to GeminiSchemaProperty(type = "STRING"),
                                    "prediction" to GeminiSchemaProperty(type = "STRING"),
                                    "cote" to GeminiSchemaProperty(type = "NUMBER")
                                )
                            )
                        )
                    ),
                    required = listOf("globalCote", "respectsInterval", "riskAssessment", "isRiskAcceptable", "criticism", "advice", "matches")
                ),
                temperature = 0.2f
            )
        )

        val adapter = moshi.adapter(GeminiRequest::class.java)
        val jsonRequest = adapter.toJson(requestPayload)

        val request = Request.Builder()
            .url(url)
            .post(jsonRequest.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string()
                    Log.e("GeminiNetwork", "Error code ${response.code}: $bodyStr")
                    throw Exception(parseApiError(response.code, response.message, bodyStr))
                }
                val bodyStr = response.body?.string() ?: throw Exception("Réponse vide de Gemini.")
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val geminiResponse = responseAdapter.fromJson(bodyStr)
                val textResponse = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Impossible d'extraire le texte de la réponse Gemini.")

                val resultAdapter = moshi.adapter(VisualAnalysisResult::class.java)
                resultAdapter.fromJson(textResponse)
                    ?: throw Exception("Échec du décodage du JSON retourné par Gemini.")
            }
        } catch (e: Exception) {
            Log.e("GeminiNetwork", "API error", e)
            throw e
        }
    }

    /**
     * Textual prediction "Crash Test" combine request.
     */
    suspend fun crashTestPrediction(
        predictionText: String,
        coteProposed: Double,
        activeMontante: Montante,
        betType: String,
        successRateInCoteRange: Double,
        successRateForPronoType: Double
    ): CrashTestResult = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            throw Exception("Clé API Gemini non configurée ! Veuillez la saisir dans les Paramètres.")
        }

        val activeModel = settingsManager.actualModel
        val url = getUrlForModel(activeModel, apiKey)

        val systemPrompt = """
            Tu es un système expert de Crash Test de paris sportifs d'élite "STADIUM".
            Ton rôle est de faire le crash test d'une idée de prono formulée par l'utilisateur en combinant ses statistiques de réussites réelles enregistrées en local.
            
            Tu dois juger si l'idée est saine en attribuant un feu Vert (GREEN), Orange (ORANGE) ou Rouge (RED).
            
            Règles d'évaluation :
            - GREEN (Feu Vert) : L'idée de pari respecte la montante, a de bonnes statistiques locales (ex: taux de réussite global et par type élevés), et est mathématiquement acceptable.
            - ORANGE (Feu Orange) : Il y a un bémol, le prono est moyen ou le taux de réussite local est modéré, ou la cote est juste en bordure.
            - RED (Feu Rouge) : Alerte critique. Le taux de réussite historique sur ce type de pari ou cette cote est catastrophique, ou elle viole ouvertement l'intervalle de cote [${activeMontante.coteMin} - ${activeMontante.coteMax}] de la montante !
            
            Données reçues (toutes les valeurs de montante sont en FC / CDF) :
            - Proposition utilisateur : "$predictionText"
            - Type de prono : $betType
            - Côte proposée : $coteProposed (intervalle autorisé : [${activeMontante.coteMin} - ${activeMontante.coteMax}])
            - Taux historique local dans cette tranche de cote : ${(successRateInCoteRange * 100).toInt()}%
            - Taux historique local pour ce type de prono ($betType) : ${(successRateForPronoType * 100).toInt()}%
            
            Tu DOIS retourner un rapport au format JSON STRICT respectant exactement ce schéma :
            {
              "status": "GREEN", 
              "verdict": "Une conclusion de synthèse super percutante sur la pertinence du pari et de l'adéquation avec ses statistiques.",
              "reasons": [
                "Raison 1 : Son taux de réussite pour la tranche de cote est excellent.",
                "Raison 2 : Attention, son taux pour le type de pari $betType est bas.",
                "Raison 3 : Spécifier si c'est conforme à l'intervalle de cote autorisé."
              ]
            }
        """.trimIndent()

        val requestPayload = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "$systemPrompt\n\nFais le crash test de mon idée de pari.")
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                responseSchema = GeminiResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "status" to GeminiSchemaProperty(type = "STRING"),
                        "verdict" to GeminiSchemaProperty(type = "STRING"),
                        "reasons" to GeminiSchemaProperty(
                            type = "ARRAY",
                            items = GeminiSchemaProperty(type = "STRING")
                        )
                    ),
                    required = listOf("status", "verdict", "reasons")
                ),
                temperature = 0.2f
            )
        )

        val adapter = moshi.adapter(GeminiRequest::class.java)
        val jsonRequest = adapter.toJson(requestPayload)

        val request = Request.Builder()
            .url(url)
            .post(jsonRequest.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string()
                    Log.e("GeminiNetwork", "Error code ${response.code}: $bodyStr")
                    throw Exception(parseApiError(response.code, response.message, bodyStr))
                }
                val bodyStr = response.body?.string() ?: throw Exception("Réponse vide de Gemini.")
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val geminiResponse = responseAdapter.fromJson(bodyStr)
                val textResponse = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Impossible d'extraire le texte de la réponse Gemini.")

                val testAdapter = moshi.adapter(CrashTestResult::class.java)
                testAdapter.fromJson(textResponse)
                    ?: throw Exception("Échec du décodage du JSON retourné par Gemini.")
            }
        } catch (e: Exception) {
            Log.e("GeminiNetwork", "Crash test error", e)
            throw e
        }
    }

    suspend fun generateSmartStrategy(
        montante: Montante,
        bankrollState: BankrollState,
        activeBetsCount: Int,
        statsTrancheA: Pair<Double, Int>,
        statsTrancheB: Pair<Double, Int>,
        statsTrancheC: Pair<Double, Int>,
        statsTrancheD: Pair<Double, Int>,
        stats1N2: Pair<Double, Int>,
        statsOverUnder: Pair<Double, Int>,
        statsBothTeams: Pair<Double, Int>,
        statsButeur: Pair<Double, Int>,
        statsAutre: Pair<Double, Int>
    ): StrategyReport = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            throw Exception("Clé API Gemini non configurée ! Veuillez la saisir dans les Paramètres.")
        }

        val activeModel = settingsManager.actualModel
        val url = getUrlForModel(activeModel, apiKey)

        val systemPrompt = """
            Tu es un conseiller financier et algorithmique d'élite spécialisé dans les montantes et la gestion de bankroll de paris sportifs "STADIUM".
            Ton rôle est d'analyser l'état de la montante active, le capital, le palier actuel et les statistiques réelles enregistrées par l'utilisateur en local pour générer un rapport stratégique ultra-personnalisé et optimisé.
            
            Données de la Montante (toutes les valeurs financières sont exclusivement en Franc Congolais - CDF / FC) :
            - Nom : ${montante.name}
            - Capital de départ : ${montante.capitalDepart} FC
            - Objectif Final : ${montante.objectifFinal} FC
            - Intervalle de cote autorisé : [${montante.coteMin} — ${montante.coteMax}]
            - Nombre de paliers validés : $activeBetsCount
            - Capital Actuel : ${bankrollState.currentBankroll} FC
            - Perte accumulée sur la série active : ${bankrollState.activeSeriesLoss} FC
            
            Statistiques de réussite réelles de l'utilisateur (%) :
            - Tranche A (Cotes 1.10 - 1.39) : ${(statsTrancheA.first * 100).toInt()}% (sur ${statsTrancheA.second} paris)
            - Tranche B (Cotes 1.40 - 1.69) : ${(statsTrancheB.first * 100).toInt()}% (sur ${statsTrancheB.second} paris)
            - Tranche C (Cotes 1.70 - 1.99) : ${(statsTrancheC.first * 100).toInt()}% (sur ${statsTrancheC.second} paris)
            - Tranche D (Cotes >= 2.00) : ${(statsTrancheD.first * 100).toInt()}% (sur ${statsTrancheD.second} paris)
            - Type 1N2 : ${(stats1N2.first * 100).toInt()}% (sur ${stats1N2.second} paris)
            - Type Over/Under : ${(statsOverUnder.first * 100).toInt()}% (sur ${statsOverUnder.second} paris)
            - Type Les Deux Marquent : ${(statsBothTeams.first * 100).toInt()}% (sur ${statsBothTeams.second} paris)
            - Type Buteur : ${(statsButeur.first * 100).toInt()}% (sur ${statsButeur.second} paris)
            - Type Autre : ${(statsAutre.first * 100).toInt()}% (sur ${statsAutre.second} paris)
            
            Génère un retour au format JSON STRICT structuré comme suit :
            {
              "title": "Un titre court pour ce conseil strategique (ex: Stabilisation du Palier X)",
              "riskEvaluation": "NIVEAU DE RISQUE (Faible / Modéré / Élevé / Critique) + explication courte",
              "progressAnalysis": "Analyse chiffrée de la progression vers l'objectif",
              "specificAdvice": "Un paragraphe d'analyse approfondie de 2-3 phrases combinant intelligemment les statistiques locales et l'état actuel.",
              "suggestedRules": [
                "Recommandation 1 (ex: Privilégier les cotes entre X et Y où vous excellez à Z%)",
                "Recommandation 2 (ex: Ajustement ou arrêt temporaire sur d'autres sports si stats faibles)",
                "Recommandation 3 (Règle d'or sur la mise ou la discipline à avoir)"
              ],
              "alertMessage": "Un message d'alerte dynamique s'il y a un danger (pertes consécutives ou capital de sécurité entamé), sinon null."
            }
        """.trimIndent()

        val requestPayload = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "$systemPrompt\n\nAnalyse ma situation de montante et donne-moi une stratégie optimisée.")
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                responseSchema = GeminiResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "title" to GeminiSchemaProperty(type = "STRING"),
                        "riskEvaluation" to GeminiSchemaProperty(type = "STRING"),
                        "progressAnalysis" to GeminiSchemaProperty(type = "STRING"),
                        "specificAdvice" to GeminiSchemaProperty(type = "STRING"),
                        "suggestedRules" to GeminiSchemaProperty(
                            type = "ARRAY",
                            items = GeminiSchemaProperty(type = "STRING")
                        ),
                        "alertMessage" to GeminiSchemaProperty(type = "STRING")
                    ),
                    required = listOf("title", "riskEvaluation", "progressAnalysis", "specificAdvice", "suggestedRules")
                ),
                temperature = 0.3f
            )
        )

        val adapter = moshi.adapter(GeminiRequest::class.java)
        val jsonRequest = adapter.toJson(requestPayload)

        val request = Request.Builder()
            .url(url)
            .post(jsonRequest.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string()
                    Log.e("GeminiNetwork", "Error code ${response.code}: $bodyStr")
                    throw Exception(parseApiError(response.code, response.message, bodyStr))
                }
                val bodyStr = response.body?.string() ?: throw Exception("Réponse vide de Gemini.")
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val geminiResponse = responseAdapter.fromJson(bodyStr)
                val textResponse = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Impossible d'extraire la stratégie de la réponse Gemini.")

                val testAdapter = moshi.adapter(StrategyReport::class.java)
                testAdapter.fromJson(textResponse)
                    ?: throw Exception("Échec du décodage du rapport de stratégie retourné par Gemini.")
            }
        } catch (e: Exception) {
            Log.e("GeminiNetwork", "Strategy generator error", e)
            throw e
        }
    }
}

data class BankrollState(
    val currentBankroll: Double,
    val activeSeriesLoss: Double,
    val recommendedStepTargetGain: Double
)
