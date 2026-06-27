package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val settingsManager = SettingsManager(application)
    val sensorialManager = SensorialManager(application, settingsManager)
    
    // Fallback key injected from AI Studio Secret Gradle Plugin
    private val appBuildConfigKey = BuildConfig.GEMINI_API_KEY

    val repository = MontanteRepository(
        montanteDao = database.montanteDao(),
        betDao = database.betDao(),
        settingsManager = settingsManager,
        appBuildConfigKey = appBuildConfigKey
    )

    // --- UI Theme Preferences ---
    var isThemeStadium by mutableStateOf(settingsManager.isThemeStadium)
        private set

    fun toggleTheme() {
        val newVal = !isThemeStadium
        settingsManager.isThemeStadium = newVal
        isThemeStadium = newVal
    }

    fun updateTheme(stadium: Boolean) {
        settingsManager.isThemeStadium = stadium
        isThemeStadium = stadium
    }

    // --- Sound and Haptic Preferences ---
    var isHapticEnabled by mutableStateOf(settingsManager.isHapticEnabled)
        private set
    var isSoundEnabled by mutableStateOf(settingsManager.isSoundEnabled)
        private set

    fun toggleHaptic() {
         val newVal = !isHapticEnabled
         settingsManager.isHapticEnabled = newVal
         isHapticEnabled = newVal
    }

    fun toggleSound() {
        val newVal = !isSoundEnabled
        settingsManager.isSoundEnabled = newVal
        isSoundEnabled = newVal
    }

    // --- Gemini Settings ---
    var geminiApiKey by mutableStateOf(settingsManager.geminiApiKey)
        private set
    var geminiModelLabel by mutableStateOf(settingsManager.geminiModelLabel)
        private set

    fun updateGeminiApiKey(key: String) {
        settingsManager.geminiApiKey = key
        geminiApiKey = key
    }

    fun updateGeminiModelLabel(label: String) {
        settingsManager.geminiModelLabel = label
        geminiModelLabel = label
    }

    // --- Currency Preferences ---
    var activeCurrency by mutableStateOf(settingsManager.activeCurrency)
        private set
    var exchangeRateUsdToCdf by mutableStateOf(settingsManager.exchangeRateUsdToCdf)
        private set

    fun updateActiveCurrency(currency: String) {
        settingsManager.activeCurrency = currency
        activeCurrency = currency
    }

    fun updateExchangeRate(rate: Float) {
        settingsManager.exchangeRateUsdToCdf = rate
        exchangeRateUsdToCdf = rate
    }

    // --- Montantes & Bets Flows ---
    val allMontantes: StateFlow<List<Montante>> = repository.allMontantes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMontante: StateFlow<Montante?> = repository.activeMontanteFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Bets of the current active montante
    val activeBets: StateFlow<List<Bet>> = activeMontante
        .flatMapLatest { montante ->
            if (montante != null) {
                repository.getBetsForMontante(montante.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State regarding active montante's bankroll mathematics
    val bankrollState: StateFlow<BankrollState?> = combine(activeMontante, activeBets) { montante, bets ->
        if (montante != null) {
            repository.computeBankrollState(montante, bets)
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Stats flows computed locally (100% offline) ---
    val allBets: StateFlow<List<Bet>> = repository.getAllBetsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statisticsState: StateFlow<StatisticsState> = combine(allBets, activeMontante, activeBets) { allHistory, activeM, activeB ->
        calculateStatistics(allHistory, activeM, activeB)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsState())

    // --- Visual Analysis Coupon states ---
    var visualAnalysisLoading by mutableStateOf(false)
        private set
    var visualAnalysisResult by mutableStateOf<VisualAnalysisResult?>(null)
        private set
    var visualAnalysisError by mutableStateOf<String?>(null)
        private set
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set
    var selectedImageBitmap by mutableStateOf<Bitmap?>(null)
        private set

    // --- Crash Test states ---
    var crashTestLoading by mutableStateOf(false)
        private set
    var crashTestResult by mutableStateOf<CrashTestResult?>(null)
        private set
    var crashTestError by mutableStateOf<String?>(null)
        private set

    // --- AI Strategy Advisor states ---
    var aiStrategyLoading by mutableStateOf(false)
        private set
    var aiStrategyReport by mutableStateOf<com.example.data.StrategyReport?>(null)
        private set
    var aiStrategyError by mutableStateOf<String?>(null)
        private set

    // --- Initial operations ---
    init {
        // Log configuration state
        Log.d("AppViewModel", "Initialized with model: ${settingsManager.actualModel}")
    }

    fun selectActiveMontante(id: Int) {
        viewModelScope.launch {
            repository.selectActiveMontante(id)
        }
    }

    fun createMontante(name: String, capital: Double, goal: Double, minCote: Double, maxCote: Double) {
        viewModelScope.launch {
            repository.createMontante(name, capital, goal, minCote, maxCote)
            sensorialManager.vibrateShortDouble()
            sensorialManager.playSoundBip()
        }
    }

    fun deleteMontante(montante: Montante) {
        viewModelScope.launch {
            repository.deleteMontante(montante)
        }
    }

    fun addBet(matchName: String, cote: Double, predictionType: String) {
        val montante = activeMontante.value ?: return
        val currentState = bankrollState.value ?: return
        
        // Calculate recommended stake size "Mise" automatically
        val calculatedMise = repository.calculateRecommendedMise(cote, currentState)

        viewModelScope.launch {
            val bet = Bet(
                montanteId = montante.id,
                matchName = matchName,
                cote = cote,
                mise = calculatedMise,
                status = "PENDING",
                predictionType = predictionType
            )
            repository.addBet(bet)
            sensorialManager.vibrateShortDouble()
            sensorialManager.playSoundBip()
        }
    }

    fun resolveBet(bet: Bet, newStatus: String) {
        viewModelScope.launch {
            val updated = bet.copy(status = newStatus)
            repository.updateBet(updated)
            
            // Sensorial triggers
            if (newStatus == "WON") {
                sensorialManager.vibrateShortDouble()
                sensorialManager.playSoundWin()
            } else if (newStatus == "LOST") {
                sensorialManager.vibrateLongSingle()
                sensorialManager.playSoundLoss()
            }
        }
    }

    fun deleteBet(bet: Bet) {
        viewModelScope.launch {
            repository.deleteBet(bet)
            sensorialManager.playSoundBip()
        }
    }

    // --- Image handling for coupon scanning ---

    fun selectImage(uri: Uri?) {
        selectedImageUri = uri
        visualAnalysisResult = null
        visualAnalysisError = null
        if (uri != null) {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedImageBitmap = bitmap
            } catch (e: Exception) {
                Log.e("AppViewModel", "Failed to decode image", e)
                visualAnalysisError = "Échec du chargement de l'image : ${e.localizedMessage}"
            }
        } else {
            selectedImageBitmap = null
        }
    }

    fun triggerVisualAnalysis() {
        val bitmap = selectedImageBitmap
        val montante = activeMontante.value
        val state = bankrollState.value

        if (bitmap == null) {
            visualAnalysisError = "Veuillez d'abord sélectionner une image de coupon."
            return
        }
        if (montante == null || state == null) {
            visualAnalysisError = "Veuillez créer une montante active d'abord."
            return
        }

        visualAnalysisLoading = true
        visualAnalysisError = null
        visualAnalysisResult = null

        // Vibration continuous pulsation
        viewModelScope.launch {
            sensorialManager.vibratePulsingContinuous(300)
            try {
                // Convert bitmap to jpeg base64 representation
                val base64Bytes = withContext(Dispatchers.Default) {
                    val outputStream = ByteArrayOutputStream()
                    // Compress to reduce network size, 80% quality is perfect
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                    Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                }

                val analysis = repository.analyzeCouponImage(base64Bytes, montante, state)
                visualAnalysisResult = analysis
                sensorialManager.vibrateShortDouble()
                sensorialManager.playSoundBip()
            } catch (e: Exception) {
                Log.e("AppViewModel", "Visual Analysis Exception", e)
                visualAnalysisError = e.localizedMessage ?: "Une erreur de communication est survenue."
                sensorialManager.vibrateLongSingle()
                sensorialManager.playSoundLoss()
            } finally {
                visualAnalysisLoading = false
            }
        }
    }

    // --- Crash Test handler ---

    fun triggerCrashTest(predictionText: String, coteProposed: Double, predictionType: String) {
        val montante = activeMontante.value
        if (montante == null) {
            crashTestError = "Aucune montante active pour effectuer le crash test."
            return
        }
        if (predictionText.isBlank()) {
            crashTestError = "Veuillez écrire votre idée de prono."
            return
        }
        if (coteProposed <= 1.0) {
            crashTestError = "Veuillez entrer une cote valide (> 1.00)"
            return
        }

        crashTestLoading = true
        crashTestError = null
        crashTestResult = null

        val stats = statisticsState.value
        
        // Success rate in this cote range
        val successRateInCote = when {
            coteProposed in 1.10..1.30 -> stats.rateTrancheA
            coteProposed in 1.31..1.50 -> stats.rateTrancheB
            coteProposed in 1.51..1.70 -> stats.rateTrancheC
            else -> stats.rateTrancheD
        }

        // Success rate for prediction type
        val successRateForType = when (predictionType) {
            "1N2" -> stats.rate1N2
            "Over/Under Buts" -> stats.rateOverUnder
            "Les deux équipes marquent" -> stats.rateBothTeams
            "Buteur" -> stats.rateButeur
            else -> stats.rateAutre
        }

        viewModelScope.launch {
            sensorialManager.vibratePulsingContinuous(200)
            try {
                val result = repository.crashTestPrediction(
                    predictionText = predictionText,
                    coteProposed = coteProposed,
                    activeMontante = montante,
                    betType = predictionType,
                    successRateInCoteRange = successRateInCote,
                    successRateForPronoType = successRateForType
                )
                crashTestResult = result
                sensorialManager.vibrateShortDouble()
                sensorialManager.playSoundBip()
            } catch (e: Exception) {
                Log.e("AppViewModel", "Crash Test Error", e)
                crashTestError = e.localizedMessage ?: "Une erreur est survenue lors du crash test."
                sensorialManager.vibrateLongSingle()
                sensorialManager.playSoundLoss()
            } finally {
                crashTestLoading = false
            }
        }
    }

    fun clearCrashTest() {
        crashTestResult = null
        crashTestError = null
    }

    // --- AI Strategy Advisor ---

    fun triggerAIStrategyReport() {
        val montante = activeMontante.value
        if (montante == null) {
            aiStrategyError = "Aucune montante active pour générer le rapport stratégique."
            return
        }

        aiStrategyLoading = true
        aiStrategyError = null
        aiStrategyReport = null

        val currentBets = activeBets.value
        val computedState = repository.computeBankrollState(montante, currentBets)
        val stats = statisticsState.value

        viewModelScope.launch {
            sensorialManager.vibratePulsingContinuous(200)
            try {
                val report = repository.generateSmartStrategy(
                    montante = montante,
                    bankrollState = computedState,
                    activeBetsCount = currentBets.size,
                    statsTrancheA = Pair(stats.rateTrancheA, stats.totalTrancheA),
                    statsTrancheB = Pair(stats.rateTrancheB, stats.totalTrancheB),
                    statsTrancheC = Pair(stats.rateTrancheC, stats.totalTrancheC),
                    statsTrancheD = Pair(stats.rateTrancheD, stats.totalTrancheD),
                    stats1N2 = Pair(stats.rate1N2, stats.total1N2),
                    statsOverUnder = Pair(stats.rateOverUnder, stats.totalOverUnder),
                    statsBothTeams = Pair(stats.rateBothTeams, stats.totalBothTeams),
                    statsButeur = Pair(stats.rateButeur, stats.totalButeur),
                    statsAutre = Pair(stats.rateAutre, stats.totalAutre)
                )
                aiStrategyReport = report
                sensorialManager.vibrateShortDouble()
                sensorialManager.playSoundBip()
            } catch (e: Exception) {
                Log.e("AppViewModel", "AI Strategy Error", e)
                aiStrategyError = e.localizedMessage ?: "Une erreur est survenue lors de l'analyse stratégique."
                sensorialManager.vibrateLongSingle()
                sensorialManager.playSoundLoss()
            } finally {
                aiStrategyLoading = false
            }
        }
    }

    fun clearAIStrategy() {
        aiStrategyReport = null
        aiStrategyError = null
    }

    // --- Internal Statistic Processor (100% offline) ---

    private fun calculateStatistics(allHistory: List<Bet>, activeMontante: Montante?, activeBets: List<Bet>): StatisticsState {
        // 1. Success rate per Cote range
        // Ranges:
        // Tranche A: [1.10 - 1.30]
        // Tranche B: [1.31 - 1.50]
        // Tranche C: [1.51 - 1.70]
        // Tranche D: [1.71+]
        // Note: Filters only resolved bets (WON or LOST)
        val resolvedBets = allHistory.filter { it.status == "WON" || it.status == "LOST" }

        val trancheABets = resolvedBets.filter { it.cote in 1.10..1.30 }
        val trancheBBets = resolvedBets.filter { it.cote in 1.31..1.50 }
        val trancheCBets = resolvedBets.filter { it.cote in 1.51..1.70 }
        val trancheDBets = resolvedBets.filter { it.cote > 1.70 }

        fun getRate(bets: List<Bet>): Double {
            if (bets.isEmpty()) return 0.5 // Default/neutral 50% success prior
            val won = bets.count { it.status == "WON" }
            return won.toDouble() / bets.size
        }

        // 2. Success rate per type
        val bets1N2 = resolvedBets.filter { it.predictionType == "1N2" }
        val betsOverUnder = resolvedBets.filter { it.predictionType == "Over/Under Buts" }
        val betsBothTeams = resolvedBets.filter { it.predictionType == "Les deux équipes marquent" }
        val betsButeur = resolvedBets.filter { it.predictionType == "Buteur" }
        val betsAutre = resolvedBets.filter { it.predictionType == "Autre" }

        // 3. Progression path percentage
        var percentageProgress = 0.0
        val bankrollPoints = mutableListOf<Double>()

        if (activeMontante != null) {
            val calcState = repository.computeBankrollState(activeMontante, activeBets)
            val currentBal = calcState.currentBankroll
            val start = activeMontante.capitalDepart
            val goal = activeMontante.objectifFinal

            val totalDiff = goal - start
            if (totalDiff > 0) {
                percentageProgress = ((currentBal - start) / totalDiff * 100.0).coerceIn(0.0, 100.0)
            }

            // Let's rebuild the bankroll progression points step by step
            bankrollPoints.add(start)
            var accum = start
            for (bet in activeBets.sortedBy { it.timestamp }) {
                when (bet.status) {
                    "WON" -> {
                        accum += bet.mise * (bet.cote - 1.0)
                    }
                    "LOST" -> {
                        accum -= bet.mise
                    }
                    "PENDING" -> {
                        accum -= bet.mise
                    }
                }
                bankrollPoints.add(accum)
            }
        }

        return StatisticsState(
            rateTrancheA = getRate(trancheABets),
            totalTrancheA = trancheABets.size,
            rateTrancheB = getRate(trancheBBets),
            totalTrancheB = trancheBBets.size,
            rateTrancheC = getRate(trancheCBets),
            totalTrancheC = trancheCBets.size,
            rateTrancheD = getRate(trancheDBets),
            totalTrancheD = trancheDBets.size,

            rate1N2 = getRate(bets1N2),
            total1N2 = bets1N2.size,
            rateOverUnder = getRate(betsOverUnder),
            totalOverUnder = betsOverUnder.size,
            rateBothTeams = getRate(betsBothTeams),
            totalBothTeams = betsBothTeams.size,
            rateButeur = getRate(betsButeur),
            totalButeur = betsButeur.size,
            rateAutre = getRate(betsAutre),
            totalAutre = betsAutre.size,

            percentageProgress = percentageProgress,
            activeBankrollHistory = bankrollPoints
        )
    }
}

data class StatisticsState(
    val rateTrancheA: Double = 0.5,
    val totalTrancheA: Int = 0,
    val rateTrancheB: Double = 0.5,
    val totalTrancheB: Int = 0,
    val rateTrancheC: Double = 0.5,
    val totalTrancheC: Int = 0,
    val rateTrancheD: Double = 0.5,
    val totalTrancheD: Int = 0,

    val rate1N2: Double = 0.5,
    val total1N2: Int = 0,
    val rateOverUnder: Double = 0.5,
    val totalOverUnder: Int = 0,
    val rateBothTeams: Double = 0.5,
    val totalBothTeams: Int = 0,
    val rateButeur: Double = 0.5,
    val totalButeur: Int = 0,
    val rateAutre: Double = 0.5,
    val totalAutre: Int = 0,

    val percentageProgress: Double = 0.0,
    val activeBankrollHistory: List<Double> = emptyList()
)
