package com.example.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*

fun formatCurrency(value: Double, activeCurrency: String = "CDF", rate: Float = 1f): String {
    return String.format("%,.0f FC", value).replace(",", " ").replace(" ", " ")
}

fun formatCurrencyShort(value: Double, activeCurrency: String = "CDF", rate: Float = 1f): String {
    return String.format("%,.0f FC", value).replace(",", " ").replace(" ", " ")
}

enum class Screen {
    MONTANTES,
    ANALYSE_IMAGE,
    STATISTIQUES,
    PARAMETRES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: AppViewModel) {
    val isStadium = viewModel.isThemeStadium
    var currentScreen by remember { mutableStateOf(Screen.MONTANTES) }

    MyApplicationTheme(isStadium = isStadium) {
        val backgroundColor = MaterialTheme.colorScheme.background
        
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_main_scaffold"),
            containerColor = backgroundColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "SESSION EN COURS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isStadium) StadiumTextSecondary else LightTextSecondary,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = titleForScreen(currentScreen),
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (isStadium) StadiumNeonGreen else LightPrimary,
                                            shape = CircleShape
                                        )
                                        .drawBehind {
                                            drawCircle(
                                                color = if (isStadium) StadiumNeonGreen.copy(alpha = 0.4f) else LightPrimary.copy(alpha = 0.4f),
                                                radius = size.minDimension / 2 + 4.dp.toPx()
                                            )
                                        }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = backgroundColor,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier
                                .padding(end = 12.dp, top = 8.dp)
                                .size(40.dp)
                                .background(
                                    color = if (isStadium) StadiumAnthracite else LightSurface,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isStadium) Color.White.copy(alpha = 0.05f) else LightBorder,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isStadium) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Changer le Theme",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = if (isStadium) StadiumAnthracite else MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.MONTANTES,
                        onClick = { currentScreen = Screen.MONTANTES },
                        icon = { Icon(if (currentScreen == Screen.MONTANTES) Icons.Filled.SportsScore else Icons.Outlined.SportsScore, contentDescription = "Montantes") },
                        label = { Text("Montantes", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.ANALYSE_IMAGE,
                        onClick = { currentScreen = Screen.ANALYSE_IMAGE },
                        icon = { Icon(if (currentScreen == Screen.ANALYSE_IMAGE) Icons.Filled.DocumentScanner else Icons.Outlined.DocumentScanner, contentDescription = "Analyse coupon") },
                        label = { Text("Analyse Coupon", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.STATISTIQUES,
                        onClick = { currentScreen = Screen.STATISTIQUES },
                        icon = { Icon(if (currentScreen == Screen.STATISTIQUES) Icons.Filled.Analytics else Icons.Outlined.Analytics, contentDescription = "Stats") },
                        label = { Text("Stats", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.PARAMETRES,
                        onClick = { currentScreen = Screen.PARAMETRES },
                        icon = { Icon(if (currentScreen == Screen.PARAMETRES) Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Paramètres") },
                        label = { Text("Paramètres", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(backgroundColor)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "ScreenTransition"
                ) { targetScreen ->
                    when (targetScreen) {
                        Screen.MONTANTES -> MontantesScreen(viewModel)
                        Screen.ANALYSE_IMAGE -> AnalyseImageScreen(viewModel)
                        Screen.STATISTIQUES -> StatistiquesScreen(viewModel)
                        Screen.PARAMETRES -> ParametresScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun imageIconForScreen(screen: Screen): ImageVector {
    return when (screen) {
        Screen.MONTANTES -> Icons.Default.SportsScore
        Screen.ANALYSE_IMAGE -> Icons.Default.DocumentScanner
        Screen.STATISTIQUES -> Icons.Default.Analytics
        Screen.PARAMETRES -> Icons.Default.Settings
    }
}

@Composable
fun titleForScreen(screen: Screen): String {
    return when (screen) {
        Screen.MONTANTES -> "STADIUM MONTANTE"
        Screen.ANALYSE_IMAGE -> "ANALYSE COUPON IA"
        Screen.STATISTIQUES -> "STATISTIQUES LOCALES"
        Screen.PARAMETRES -> "PARAMÈTRES SÉCURISÉS"
    }
}

// ==========================================
// 1. MONTANTES SCREEN (HOME)
// ==========================================

@Composable
fun MontantesScreen(viewModel: AppViewModel) {
    val activeMontante by viewModel.activeMontante.collectAsState()
    val activeBets by viewModel.activeBets.collectAsState()
    val bankrollState by viewModel.bankrollState.collectAsState()
    val allMontantes by viewModel.allMontantes.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddBetDialog by remember { mutableStateOf(false) }
    var showAllMontantesDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Active Montante Card
        item {
            if (activeMontante == null) {
                NoActiveMontanteCard(
                    onCreateNewClick = { showCreateDialog = true },
                    onSelectExistingClick = { showAllMontantesDialog = true },
                    hasExisting = allMontantes.isNotEmpty()
                )
            } else {
                ActiveMontanteDetailsCard(
                    montante = activeMontante!!,
                    bankrollState = bankrollState,
                    onSwitchMontante = { showAllMontantesDialog = true },
                    onCreateNew = { showCreateDialog = true },
                    activeBetsCount = activeBets.size
                )
            }
        }

        if (activeMontante != null) {
            // "Crash Test" Widget
            item {
                CrashTestCardWidget(viewModel = viewModel, activeMontante = activeMontante!!)
            }

            // "AI Strategy Advisor" Widget
            item {
                AIStrategyAdvisorCardWidget(viewModel = viewModel)
            }

            // Quick Bet Creation Action & Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mes Paliers de Paris",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = { showAddBetDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_bet_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Bets/Paliers list
            if (activeBets.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Aucun palier rattaché à cette montante.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Ajoute ton premier pari pour calculer la mise !",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // List of bets
                items(activeBets.reversed(), key = { it.id }) { bet ->
                    BetRowItem(bet = bet, onSetStatus = { status ->
                        viewModel.resolveBet(bet, status)
                    }, onDelete = {
                        viewModel.deleteBet(bet)
                    })
                }
            }
        }
    }

    // --- Dialogs ---
    if (showCreateDialog) {
        CreateMontanteDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, capital, goal, minC, maxC ->
                viewModel.createMontante(name, capital, goal, minC, maxC)
                showCreateDialog = false
            },
            activeCurrency = viewModel.activeCurrency
        )
    }

    if (showAddBetDialog) {
        AddBetDialog(
            activeMontante = activeMontante,
            bankrollState = bankrollState,
            onDismiss = { showAddBetDialog = false },
            onCalculateMise = { cote ->
                bankrollState?.let { viewModel.repository.calculateRecommendedMise(cote, it) } ?: 1.0
            },
            onConfirm = { match, cote, type ->
                viewModel.addBet(match, cote, type)
                showAddBetDialog = false
            },
            activeCurrency = viewModel.activeCurrency,
            exchangeRate = viewModel.exchangeRateUsdToCdf
        )
    }

    if (showAllMontantesDialog) {
        AllMontantesDialog(
            montantes = allMontantes,
            activeMontante = activeMontante,
            onDismiss = { showAllMontantesDialog = false },
            onSelect = { id ->
                viewModel.selectActiveMontante(id)
                showAllMontantesDialog = false
            },
            onDelete = { montante ->
                viewModel.deleteMontante(montante)
            },
            activeCurrency = viewModel.activeCurrency,
            exchangeRate = viewModel.exchangeRateUsdToCdf
        )
    }
}

@Composable
fun NoActiveMontanteCard(
    onCreateNewClick: () -> Unit,
    onSelectExistingClick: () -> Unit,
    hasExisting: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("no_active_montante_card"),
        colors = CardDefaults.cardColors(containerColor = StadiumAnthracite),
        border = BorderStroke(1.dp, StadiumNeonGreen.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(StadiumNeonGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SportsScore,
                    contentDescription = null,
                    tint = StadiumNeonGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AUCUNE MONTANTE ACTIVE",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Créez une montante pour calculer vos mises de récupération optimisées et suivre vos objectifs 100% hors-ligne.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = StadiumTextSecondary,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCreateNewClick,
                colors = ButtonDefaults.buttonColors(containerColor = StadiumNeonGreen, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("create_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CRÉER UNE MONTANTE", fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 0.5.sp)
            }
            
            if (hasExisting) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onSelectExistingClick,
                    border = BorderStroke(1.dp, StadiumTextSecondary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("CHARGER UNE MONTANTE EXISTANTE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ActiveMontanteDetailsCard(
    montante: Montante,
    bankrollState: BankrollState?,
    onSwitchMontante: () -> Unit,
    onCreateNew: () -> Unit,
    activeBetsCount: Int = 0,
    activeCurrency: String = "CDF",
    exchangeRate: Float = 2500f
) {
    val currentB = bankrollState?.currentBankroll ?: montante.capitalDepart
    val pathPct = if (montante.objectifFinal > montante.capitalDepart) {
        ((currentB - montante.capitalDepart) / (montante.objectifFinal - montante.capitalDepart) * 100).coerceIn(0.0, 100.0)
    } else {
        0.0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = StadiumAnthracite
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header: Name and Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OBJECTIF " + formatCurrencyShort(montante.objectifFinal, activeCurrency, exchangeRate),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Intervalle: ${montante.coteMin} — ${montante.coteMax}",
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = StadiumTextSecondary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, StadiumNeonGreen.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "PALIER $activeBetsCount",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = StadiumNeonGreen,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    IconButton(onClick = onSwitchMontante) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "Changer montante",
                            tint = StadiumNeonGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Immersive Circular Progress Visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(88.dp)) {
                    // Track circle
                    drawCircle(
                        color = Color(0xFF2D2D2F),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Neon green progression arc
                    drawArc(
                        color = StadiumNeonGreen,
                        startAngle = -90f,
                        sweepAngle = (pathPct.toFloat() * 3.6f),
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.0f%%", pathPct),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = "GROWTH",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = StadiumTextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sub-metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Box 1: Next stake estimation or active bankroll
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "BANKROLL ACTUELLE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = StadiumTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(currentB, activeCurrency, exchangeRate),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }
 
                // Box 2: Departure capital
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "CAPITAL DÉPART",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = StadiumTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(montante.capitalDepart, activeCurrency, exchangeRate),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// Single Bet Row
@Composable
fun BetRowItem(
    bet: Bet,
    onSetStatus: (String) -> Unit,
    onDelete: () -> Unit,
    activeCurrency: String = "CDF",
    exchangeRate: Float = 2500f
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bet_item_${bet.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(bet.predictionType, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("Cote @%.2f", bet.cote),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bet.matchName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: calculation + status resolver
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Mise nécessaire", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        formatCurrency(bet.mise, activeCurrency, exchangeRate),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (bet.status) {
                        "PENDING" -> {
                            OutlinedButton(
                                onClick = { onSetStatus("LOST") },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDanger),
                                border = BorderStroke(1.dp, RedDanger.copy(alpha = 0.4f)),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Perdu ❌", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onSetStatus("WON") },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess, contentColor = StadiumBlack),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Gagné ✅", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        "WON" -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GreenSuccess.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("GAGNÉ", color = GreenSuccess, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        "LOST" -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RedDanger.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, RedDanger.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = RedDanger, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PERDU", color = RedDanger, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Crash Test widget on Montantes tab
@Composable
fun CrashTestCardWidget(viewModel: AppViewModel, activeMontante: Montante) {
    var pronoIdea by remember { mutableStateOf("") }
    var proposedCote by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("1N2") }
    val types = listOf("1N2", "Over/Under Buts", "Les deux équipes marquent", "Buteur", "Autre")
    var isExpanded by remember { mutableStateOf(false) }

    val loading = viewModel.crashTestLoading
    val result = viewModel.crashTestResult
    val error = viewModel.crashTestError

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) StadiumAnthracite else StadiumNeonGreen
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isExpanded) Color.White.copy(alpha = 0.05f) else Color.Transparent
        )
    ) {
        Column {
            // Header (Always shows, is Green when collapsed, Slate Dark when expanded)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isExpanded) StadiumAnthracite else StadiumNeonGreen,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dark square icon box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (isExpanded) StadiumNeonGreen.copy(alpha = 0.1f) else Color.Black,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = if (isExpanded) StadiumNeonGreen else StadiumNeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Title and subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CRASH TEST GEMINI",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Analyse ton prono texte avant de valider le palier.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isExpanded) StadiumTextSecondary else Color.White.copy(alpha = 0.7f),
                        lineHeight = 14.sp
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isExpanded) StadiumNeonGreen else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StadiumAnthracite)
                        .padding(start = 18.dp, end = 18.dp, bottom = 18.dp)
                ) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Saisis ton idée de pari. L'IA la combinera avec tes statistiques locales pour rendre un verdict objectif (Feu Vert/Orange/Rouge).",
                        fontSize = 11.sp,
                        color = StadiumTextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = pronoIdea,
                        onValueChange = { pronoIdea = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("crash_test_input"),
                        placeholder = { Text("Ex: PSG gagne avec 2 buts d'écart", fontSize = 13.sp, color = StadiumTextSecondary.copy(alpha = 0.7f)) },
                        maxLines = 2,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.4f),
                            focusedIndicatorColor = StadiumNeonGreen,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = proposedCote,
                            onValueChange = { proposedCote = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("crash_test_cote_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("Cote (ex: 1.45)", fontSize = 13.sp, color = StadiumTextSecondary.copy(alpha = 0.7f)) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.4f),
                                focusedIndicatorColor = StadiumNeonGreen,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Prediction type picker
                        var typeExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedButton(
                                onClick = { typeExpanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Black.copy(alpha = 0.4f),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(selectedType, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                types.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, fontSize = 13.sp) },
                                        onClick = {
                                            selectedType = type
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (loading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = StadiumNeonGreen
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Analyse en cours...",
                                fontSize = 12.sp,
                                color = StadiumNeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                val coteVal = proposedCote.toDoubleOrNull() ?: 0.0
                                viewModel.triggerCrashTest(pronoIdea, coteVal, selectedType)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("crash_test_submit"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StadiumNeonGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("COMMENCER LE CRASH TEST", fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                        }
                    }

                    // Show crash test result
                    if (result != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        val statusColor = when (result.status.uppercase()) {
                            "GREEN", "VERT" -> GreenSuccess
                            "ORANGE" -> OrangeWarning
                            else -> RedDanger
                        }
                        val statusLabel = when (result.status.uppercase()) {
                            "GREEN", "VERT" -> "FEU VERT 🟢"
                            "ORANGE" -> "FEU ORANGE 🟡"
                            else -> "FEU ROUGE 🔴"
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(statusColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = statusLabel,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = statusColor
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = result.verdict,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(10.dp))

                                result.reasons.forEach { reason ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("•", color = statusColor, modifier = Modifier.width(12.dp), fontWeight = FontWeight.Bold)
                                        Text(reason, fontSize = 11.sp, color = StadiumTextSecondary, lineHeight = 15.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = error,
                            color = RedDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AIStrategyAdvisorCardWidget(viewModel: AppViewModel) {
    var isExpanded by remember { mutableStateOf(false) }
    val loading = viewModel.aiStrategyLoading
    val report = viewModel.aiStrategyReport
    val error = viewModel.aiStrategyError

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_strategy_widget"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StadiumAnthracite),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(StadiumNeonGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = StadiumNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CONSEILLER STRATÉGIQUE IA",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Obtiens une feuille de route optimisée selon tes stats locales.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = StadiumTextSecondary,
                        lineHeight = 14.sp
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = StadiumNeonGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StadiumAnthracite)
                        .padding(start = 18.dp, end = 18.dp, bottom = 18.dp)
                ) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Analyse et synthétise l'ensemble de votre montante en se basant sur vos taux réels de réussite par tranche de cotes et type de prono.",
                        fontSize = 11.sp,
                        color = StadiumTextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (loading) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = StadiumNeonGreen
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Calcul de stratégie optimisée...",
                                fontSize = 12.sp,
                                color = StadiumNeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.triggerAIStrategyReport() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ai_strategy_submit"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StadiumNeonGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("GÉNÉRER MON CONSEIL IA", fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                            }
                        }
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = error,
                            color = RedDanger,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Report content
                    if (report != null) {
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        // Header panel
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, StadiumNeonGreen.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = report.title,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(StadiumNeonGreen.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = report.riskEvaluation,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StadiumNeonGreen
                                        )
                                    }
                                    
                                    Text(
                                        text = report.progressAnalysis,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = StadiumTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "CONSEIL PERSONNALISÉ :",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StadiumNeonGreen,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = report.specificAdvice,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "RÈGLES EN FORME DE FEUILLE DE ROUTE :",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StadiumNeonGreen,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                report.suggestedRules.forEach { rule ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("✔", color = StadiumNeonGreen, modifier = Modifier.width(16.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(rule, fontSize = 11.sp, color = StadiumTextSecondary, lineHeight = 15.sp)
                                    }
                                }

                                report.alertMessage?.let { alert ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OrangeWarning.copy(alpha = 0.1f))
                                            .border(1.dp, OrangeWarning.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text("⚠", color = OrangeWarning, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(alert, fontSize = 11.sp, color = Color.White, lineHeight = 15.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. ANALYSE IMAGE SCREEN
// ==========================================

@Composable
fun AnalyseImageScreen(viewModel: AppViewModel) {
    val imageBitmap = viewModel.selectedImageBitmap
    val loading = viewModel.visualAnalysisLoading
    val result = viewModel.visualAnalysisResult
    val error = viewModel.visualAnalysisError
    val activeM by viewModel.activeMontante.collectAsState()

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            viewModel.selectImage(uri)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeM == null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StadiumAnthracite),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, RedDanger.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RedDanger, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Aucune Montante Active", fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "Vous devez configurer une montante active (Capital, objectifs, cotes autorisées) sur l'écran d'accueil avant d'importer un coupon.",
                        fontSize = 12.sp,
                        color = StadiumTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        Text(
            "Analyse ton Coupon de Pari Sportif 📸",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            "Prends en photo ou importe un coupon (ticket, liste de matchs côtes). L'IA Gemini analysera les cotes des matchs détectés et évaluera la cohérence par rapport à ton intervalle autorisé [${activeM!!.coteMin} - ${activeM!!.coteMax}] !",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Image picker container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = "Coupon importé",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Importer mon Coupon", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Cliquez pour parcourir la galerie", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (imageBitmap != null) {
            if (loading) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Analyse visuelle et calculs de cohérence en cours... 🤖", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Vibration sync pulsée...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Button(
                    onClick = { viewModel.triggerVisualAnalysis() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("scan_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ANALYSER CE TICKET PAR IA", fontWeight = FontWeight.Black)
                }
            }
        }

        // Display results
        if (result != null) {
            VisualAnalysisReportCard(result = result)
        }

        if (error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RedDanger.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, RedDanger.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = RedDanger,
                    modifier = Modifier.padding(14.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun VisualAnalysisReportCard(result: VisualAnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rapport d'Analyse IA", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
                
                Surface(
                    color = (if (result.respectsInterval) GreenSuccess else RedDanger).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (result.respectsInterval) "CONFORME ✅" else "CONFORME : NON ⚠️",
                        color = if (result.respectsInterval) GreenSuccess else RedDanger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Matches
            Text("Matchs & Paris Détectés :", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))

            result.matches.forEach { match ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${match.teamHome} - ${match.teamAway}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            match.prediction,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        String.format("@%.2f", match.cote),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Côte globale détectée :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    String.format("@%.2f", result.globalCote),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Risk Assessment badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Évaluation du risque : ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                val riskColor = when (result.riskAssessment.lowercase()) {
                    "faible" -> GreenSuccess
                    "moyen" -> OrangeWarning
                    else -> RedDanger
                }
                Surface(
                    color = riskColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = result.riskAssessment.uppercase(),
                        color = riskColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Criticism
            Text("Critique exhaustive :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Text(
                result.criticism,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Advice
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Recommandations Stratégiques :", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        result.advice,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. STATISTIQUES SCREEN
// ==========================================

@Composable
fun StatistiquesScreen(viewModel: AppViewModel) {
    val stats by viewModel.statisticsState.collectAsState()
    val activeM by viewModel.activeMontante.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Summary Metrics Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Synthèse de Bankroll", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Chemin parcouru", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                String.format("%.1f %%", stats.percentageProgress),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Taux Réussite Moyen", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val allSuccessCount = (stats.totalTrancheA + stats.totalTrancheB + stats.totalTrancheC + stats.totalTrancheD)
                            val weightedRate = if (allSuccessCount > 0) {
                                ((stats.rateTrancheA * stats.totalTrancheA +
                                  stats.rateTrancheB * stats.totalTrancheB +
                                  stats.rateTrancheC * stats.totalTrancheC +
                                  stats.rateTrancheD * stats.totalTrancheD) / allSuccessCount * 100)
                            } else {
                                50.0
                            }
                            Text(
                                String.format("%.0f %%", weightedRate),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        // Custom Bankroll Line Chart Card
        if (activeM != null && stats.activeBankrollHistory.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Courbe de Bankroll (Montante Active)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Historique des fluctuations du capital après validation de chaque palier.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom drawing Canvas
                        BankrollProgressionChart(
                            history = stats.activeBankrollHistory,
                            initialCapital = activeM!!.capitalDepart,
                            targetGoal = activeM!!.objectifFinal,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Taux de réussite par intervalle de cote
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Réussite par Intervalle de Cotes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Identifie les tranches de cotes où ton rendement est le meilleur.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CoteRateItem(label = "Cotes Très Sécurisées [1.10 - 1.30]", rate = stats.rateTrancheA, total = stats.totalTrancheA)
                    Spacer(modifier = Modifier.height(12.dp))
                    CoteRateItem(label = "Cotes Zone Cible [1.31 - 1.50]", rate = stats.rateTrancheB, total = stats.totalTrancheB)
                    Spacer(modifier = Modifier.height(12.dp))
                    CoteRateItem(label = "Cotes Ambicieuses [1.51 - 1.70]", rate = stats.rateTrancheC, total = stats.totalTrancheC)
                    Spacer(modifier = Modifier.height(12.dp))
                    CoteRateItem(label = "Cotes Risquées [1.71+]", rate = stats.rateTrancheD, total = stats.totalTrancheD)
                }
            }
        }

        // Taux de réussite par type de prono
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Réussite par Type de Pronostic",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Pourcentage de victoires selon le format de pari validé.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CoteRateItem(label = "Résultat Match [1N2]", rate = stats.rate1N2, total = stats.total1N2)
                    Spacer(modifier = Modifier.height(12.dp))
                    CoteRateItem(label = "Nombre de buts [Over/Under]", rate = stats.rateOverUnder, total = stats.totalOverUnder)
                    Spacer(modifier = Modifier.height(12.dp))
                    CoteRateItem(label = "Les deux équipes marquent", rate = stats.rateBothTeams, total = stats.totalBothTeams)
                    Spacer(modifier = Modifier.height(12.dp))
                    CoteRateItem(label = "Buteur du match", rate = stats.rateButeur, total = stats.totalButeur)
                    Spacer(modifier = Modifier.height(12.dp))
                    CoteRateItem(label = "Autres", rate = stats.rateAutre, total = stats.totalAutre)
                }
            }
        }

        // Feature Improvement 1: Real-time Interactive Montante Simulation & Ruin Risk Analyzer
        item {
            MontanteSimulatorCard(
                activeCurrency = viewModel.activeCurrency,
                exchangeRate = viewModel.exchangeRateUsdToCdf
            )
        }
    }
}

@Composable
fun MontanteSimulatorCard(
    activeCurrency: String,
    exchangeRate: Float
) {
    var capitalInput by remember { mutableStateOf(100.0) }
    var goalInput by remember { mutableStateOf(200.0) }
    var coteInput by remember { mutableStateOf(1.5f) }
    var winRateInput by remember { mutableStateOf(65f) }
    var stepsInput by remember { mutableStateOf(15f) }

    var hasSimulated by remember { mutableStateOf(false) }
    val simulatedWons = remember { mutableStateListOf<Boolean>() }
    val simulatedMises = remember { mutableStateListOf<Double>() }
    val simulatedBalances = remember { mutableStateListOf<Double>() }
    var ruinOccurred by remember { mutableStateOf(false) }
    var maxConsecutiveLosses by remember { mutableStateOf(0) }
    var realWinRate by remember { mutableStateOf(0f) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Mode Bac à Sable : Simulateur de Risque",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Évaluez la longévité de votre capital sur une série de paris théoriques avec la montante.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // -- Input 1: Capital
            Text(
                text = "Capital de départ : " + formatCurrency(capitalInput, activeCurrency, exchangeRate),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = capitalInput.toFloat(),
                onValueChange = { 
                    capitalInput = it.toDouble()
                    if (goalInput <= capitalInput) {
                        goalInput = capitalInput * 1.5
                    }
                },
                valueRange = 10f..1000f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )

            // -- Input 2: Goal
            Text(
                text = "Objectif final ciblé : " + formatCurrency(goalInput, activeCurrency, exchangeRate),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = goalInput.toFloat(),
                onValueChange = { goalInput = kotlin.math.max(capitalInput + 5.0, it.toDouble()) },
                valueRange = (capitalInput.toFloat() + 5f)..5000f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )

            // -- Input 3: Cote
            Text(
                text = String.format("Cote moyenne estimée : %.2f", coteInput),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = coteInput,
                onValueChange = { coteInput = it },
                valueRange = 1.10f..3.00f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )

            // -- Input 4: Estimated Win Rate
            Text(
                text = String.format("Taux de réussite théorique de vos paris : %.0f %%", winRateInput),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = winRateInput,
                onValueChange = { winRateInput = it },
                valueRange = 25f..95f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )

            // -- Input 5: Number of bets
            Text(
                text = String.format("Nombre de paris de la série : %.0f", stepsInput),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = stepsInput,
                onValueChange = { stepsInput = it },
                valueRange = 5f..40f,
                steps = 34,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Simulation button
            Button(
                onClick = {
                    simulatedWons.clear()
                    simulatedMises.clear()
                    simulatedBalances.clear()
                    
                    var currentB = capitalInput
                    var activeLoss = 0.0
                    val seriesTargetGain = (goalInput - capitalInput) / 5.0
                    var currentConsecutive = 0
                    var maxConsec = 0
                    var wins = 0
                    var broke = false

                    simulatedBalances.add(currentB)

                    for (step in 1..stepsInput.toInt()) {
                        if (currentB <= 1.0) {
                            broke = true
                            break
                        }

                        val recommended = (seriesTargetGain + activeLoss) / (coteInput - 1.0)
                        val mise = if (recommended > currentB) {
                            currentB
                        } else {
                            kotlin.math.max(1.0, recommended)
                        }

                        val won = (1..100).random() <= winRateInput.toInt()
                        simulatedWons.add(won)
                        simulatedMises.add(mise)

                        if (won) {
                            currentB += mise * (coteInput.toDouble() - 1.0)
                            activeLoss = 0.0
                            currentConsecutive = 0
                            wins++
                        } else {
                            currentB -= mise
                            activeLoss += mise
                            currentConsecutive++
                            if (currentConsecutive > maxConsec) {
                                maxConsec = currentConsecutive
                            }
                        }
                        simulatedBalances.add(currentB)
                    }

                    ruinOccurred = broke
                    maxConsecutiveLosses = maxConsec
                    realWinRate = if (simulatedWons.isNotEmpty()) {
                        (wins.toFloat() / simulatedWons.size) * 100f
                    } else {
                        0f
                    }
                    hasSimulated = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Lancer la Simulation", fontWeight = FontWeight.Black)
                }
            }

            if (hasSimulated) {
                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Résultats de la simulation d'entraînement :",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stats rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Capital Final", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val finalBal = simulatedBalances.lastOrNull() ?: capitalInput
                        Text(
                            text = formatCurrency(finalBal, activeCurrency, exchangeRate),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (ruinOccurred) RedDanger else if (finalBal >= goalInput) GreenSuccess else OrangeWarning
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Taux de Réussite Réel", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format("%.0f %%", realWinRate),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Série de Pertes Max", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$maxConsecutiveLosses d'affilée",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (maxConsecutiveLosses >= 3) RedDanger else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Verdict Badge
                val finalBal = simulatedBalances.lastOrNull() ?: capitalInput
                val verdictLabel = when {
                    ruinOccurred -> "🔴 FAILLITE (Le capital a été entièrement épuisé)"
                    finalBal >= goalInput -> "🟢 SUCCÈS ! (Objectif final atteint ou largement dépassé)"
                    else -> "🟡 SURVIE (La montante n'est pas ruinée mais l'objectif n'est pas atteint)"
                }
                val verdictColor = when {
                    ruinOccurred -> RedDanger
                    finalBal >= goalInput -> GreenSuccess
                    else -> OrangeWarning
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(verdictColor.copy(alpha = 0.08f))
                        .border(1.dp, verdictColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = verdictLabel,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = verdictColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sequence layout
                Text("Chronologie de la série :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    simulatedWons.forEachIndexed { idx, won ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (won) GreenSuccess else RedDanger),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (won) "V" else "D",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Small detailed list of steps expansion
                var showStepDetails by remember { mutableStateOf(false) }

                TextButton(
                    onClick = { showStepDetails = !showStepDetails },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (showStepDetails) "Masquer les détails par palier" else "Afficher les détails de chaque mise",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (showStepDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (showStepDetails) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        simulatedMises.forEachIndexed { idx, mise ->
                            val isWin = if (idx < simulatedWons.size) simulatedWons[idx] else false
                            val outcomeString = if (isWin) {
                                String.format("+%.2f $", mise * (coteInput.toDouble() - 1.0))
                            } else {
                                String.format("-%.2f $", mise)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Paris n°${idx + 1} : Mise ${formatCurrency(mise, activeCurrency, exchangeRate)}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Text(
                                    text = if (isWin) "Gain: $outcomeString" else "Perte: $outcomeString",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWin) GreenSuccess else RedDanger
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoteRateItem(label: String, rate: Double, total: Int) {
    val pct = (rate * 100).toInt()
    val barColor = when {
        pct >= 70 -> GreenSuccess
        pct >= 45 -> OrangeWarning
        else -> RedDanger
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$pct% ($total)",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { rate.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    }
}

// Custom line chart component
@Composable
fun BankrollProgressionChart(
    history: List<Double>,
    initialCapital: Double,
    targetGoal: Double,
    primaryColor: Color,
    textColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color.Transparent)
            .padding(vertical = 10.dp)
    ) {
        val width = size.width
        val height = size.height
        val N = history.size

        if (N < 2) {
            // Draw dummy horizontal baseline
            drawLine(
                color = primaryColor.copy(alpha = 0.2f),
                start = Offset(0f, height/2),
                end = Offset(width, height/2),
                strokeWidth = 2f
            )
            return@Canvas
        }

        val minVal = history.minOrNull() ?: 0.0
        val maxVal = history.maxOrNull() ?: 100.0

        // Introduce scaling bounds with padding
        val boundsMin = kotlin.math.min(initialCapital, minVal) * 0.9
        val boundsMax = kotlin.math.max(targetGoal, maxVal) * 1.1
        val deltaY = (boundsMax - boundsMin).toFloat()

        val points = history.mapIndexed { index, value ->
            val x = index.toFloat() / (N - 1) * width
            val y = height - (((value - boundsMin) / deltaY).toFloat() * height)
            Offset(x, y)
        }

        // Draw clean Grid lines (Goal and starting)
        val yGoal = height - (((targetGoal - boundsMin) / deltaY).toFloat() * height)
        val yStart = height - (((initialCapital - boundsMin) / deltaY).toFloat() * height)

        // Target goal dotted line
        drawLine(
            color = OrangeWarning.copy(alpha = 0.5f),
            start = Offset(0f, yGoal),
            end = Offset(width, yGoal),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // Capital starting dotted line
        drawLine(
            color = textColor.copy(alpha = 0.3f),
            start = Offset(0f, yStart),
            end = Offset(width, yStart),
            strokeWidth = 1f.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // Draw bezier path
        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val current = points[i]
                val conX1 = prev.x + (current.x - prev.x) / 2
                val conY1 = prev.y
                val conX2 = prev.x + (current.x - prev.x) / 2
                val conY2 = current.y
                cubicTo(conX1, conY1, conX2, conY2, current.x, current.y)
            }
        }

        // Draw elegant gradient fill under curves
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = strokePath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw circular nodes
        points.forEachIndexed { idx, point ->
            // Highlight the final point or win points
            val color = if (idx == points.size - 1) primaryColor else primaryColor.copy(alpha = 0.6f)
            val radius = if (idx == points.size - 1) 5.dp.toPx() else 4.dp.toPx()
            
            drawCircle(
                color = color,
                radius = radius,
                center = point
            )
            // Outer white glow ring for final point
            if (idx == points.size - 1) {
                drawCircle(
                    color = Color.White,
                    radius = radius * 1.5f,
                    center = point,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

// ==========================================
// 4. PARAMETRES SCREEN
// ==========================================

@Composable
fun ParametresScreen(viewModel: AppViewModel) {
    val isStadium = viewModel.isThemeStadium
    val haptic = viewModel.isHapticEnabled
    val sound = viewModel.isSoundEnabled
    val key = viewModel.geminiApiKey
    val model = viewModel.geminiModelLabel
    val activeCurrency = viewModel.activeCurrency
    var exchangeRateText by remember(viewModel.exchangeRateUsdToCdf) {
        mutableStateOf(viewModel.exchangeRateUsdToCdf.toString())
    }

    var showKeyInput by remember { mutableStateOf(false) }
    var showUserGuide by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning Card regarding secure API keys in AI Studio
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Stockage 100% Local & Sécurisé",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Tes montantes, paris et clé API sont encryptés localement dans l'appareil. Aucune donnée n'est envoyée dans un cloud. Seuls les rapports de coupons sollicitent l'API Gemini.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        // API settings section
        Text("Configuration Connexion IA", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Key Row
                Text("Clé API Google AI Studio :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = key,
                    onValueChange = { viewModel.updateGeminiApiKey(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input"),
                    visualTransformation = if (showKeyInput) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKeyInput = !showKeyInput }) {
                            Icon(
                                imageVector = if (showKeyInput) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Afficher la clé"
                            )
                        }
                    },
                    placeholder = { Text("Saisissez votre clé API...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Model Selector
                Text("Sélecteur de Modèle IA :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (model) {
                                    "gemini-1.5-pro" -> "Gemini 1.5 Pro (Raisonnement Supérieur)"
                                    "gemini-2.0-flash" -> "Gemini 2.0 Flash (Nouvelle Génération)"
                                    "gemini-2.5-flash" -> "Gemini 2.5 Flash (Plus Intelligent / Récent)"
                                    else -> "Gemini 1.5 Flash (Ultra Rapide / Recommandé)"
                                },
                                fontSize = 12.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Gemini 1.5 Flash (Recommandé / Très Stable)", fontSize = 13.sp) },
                            onClick = {
                                viewModel.updateGeminiModelLabel("gemini-1.5-flash")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Gemini 2.0 Flash (Nouvelle Génération / Récent)", fontSize = 13.sp) },
                            onClick = {
                                viewModel.updateGeminiModelLabel("gemini-2.0-flash")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Gemini 2.5 Flash (Plus Intelligent / Récent)", fontSize = 13.sp) },
                            onClick = {
                                viewModel.updateGeminiModelLabel("gemini-2.5-flash")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Gemini 1.5 Pro (Raisonnement Supérieur)", fontSize = 13.sp) },
                            onClick = {
                                viewModel.updateGeminiModelLabel("gemini-1.5-pro")
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note : Les clefs API personnelles exigent des modèles publics valides (comme gemini-1.5-flash, gemini-2.0-flash, gemini-2.5-flash ou gemini-1.5-pro).",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.error,
                    lineHeight = 13.sp
                )
            }
        }

        // Currency Settings section
        Text("Devise de l'application", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Devise active :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* Toujours CDF */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StadiumNeonGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CDF (FC) - Franc Congolais", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "L'application fonctionne exclusivement en Franc Congolais (CDF / FC). Toutes les montantes, objectifs et mises sont saisis et calculés dans cette devise.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Personalization section
        Text("Personnalisation de l'Expérience", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Theme Switsh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Thème Neon (Sombre)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Style ultra-sombre moderne avec de magnifiques accents vert électrique.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isStadium,
                        onCheckedChange = { viewModel.updateTheme(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = StadiumNeonGreen)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                // Vibration triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Retours Haptiques Tactiles", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Micro-pulsations pendant les calculs et vibrations d'importance.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = haptic,
                        onCheckedChange = { viewModel.toggleHaptic() },
                        colors = SwitchDefaults.colors(checkedThumbColor = StadiumNeonGreen)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                // Sound Effect triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sons et Alertes Audibles", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Effet Ding-Ding à l'encaissement d'un palier et échecs sonores.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = sound,
                        onCheckedChange = { viewModel.toggleSound() },
                        colors = SwitchDefaults.colors(checkedThumbColor = StadiumNeonGreen)
                    )
                }
            }
        }

        // Section Guide d'utilisation
        Text("Dossier d'Apprentissage", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showUserGuide = true }
                .testTag("show_user_guide_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(StadiumNeonGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = StadiumNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Guide d'Utilisation Complet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Domptez la montante, le simulateur bac à sable et les fonctionnalités d'analyse IA de pointe.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = StadiumNeonGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showUserGuide) {
        FullUserGuideDialog(
            isStadium = isStadium,
            onDismiss = { showUserGuide = false }
        )
    }
}

// ==========================================
// DIALOGS IMPLEMENTATIONS
// ==========================================

@Composable
fun FullUserGuideDialog(
    isStadium: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isStadium) StadiumBlack else MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header (Sticky)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isStadium) StadiumAnthracite else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("guide_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = if (isStadium) StadiumNeonGreen else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DOSSIER PÉDAGOGIQUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isStadium) StadiumNeonGreen else MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Guide d'Utilisation Complet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isStadium) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Intro Card banner
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isStadium) StadiumAnthracite else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.dp, (if (isStadium) StadiumNeonGreen else MaterialTheme.colorScheme.primary).copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "BIENVENUE SUR MONTANTE NEON",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (isStadium) StadiumNeonGreen else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Cette application est un dashboard d'ingénierie financière conçu pour éliminer l'impulsivité émotionnelle des paris sportifs. En combinant la rigueur algorithmique de récupération de capital avec le raisonnement cognitif de l'IA Gemini, vous possédez désormais un outil de gestion d'élite.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = if (isStadium) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Part 1
                    GuideSectionCard(
                        num = "1",
                        title = "La Théorie de la Montante Financière",
                        isStadium = isStadium
                    ) {
                        Text(
                            "La montante est une stratégie de gestion de capital à progression contrôlée. Contrairement aux paris classiques à mise fixe ou aux martingales destructrices (qui doublent indéfiniment sans logique), notre approche repose sur la Montante Dynamique Neon (MDN).\n\n" +
                            "Le principe fondamental est simple : chaque fois qu'un pari est perdu, l'algorithme Stadium réévalue le capital perdu et ajuste dynamiquement le montant du prochain pari. Dès qu'un pari est validé, l'ensemble des pertes accumulées sur la série en cours est intégralement remboursé, tout en sécurisant le bénéfice net que vous vous étiez fixé pour ce palier. Vous reprenez alors à l'étape initiale en toute sécurité.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = if (isStadium) StadiumTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Part 2
                    GuideSectionCard(
                        num = "2",
                        title = "Le Calculateur & L'Équation Fondamentale",
                        isStadium = isStadium
                    ) {
                        Text(
                            "Le système calcule automatiquement vos enjeux à l'aide de l'équation suivante :",
                            fontSize = 12.sp,
                            color = if (isStadium) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Show Formula Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isStadium) Color.Black else MaterialTheme.colorScheme.surface)
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Mise = (Pertes Accumulées + Objectif Gain) / (Cote du Pari - 1)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isStadium) StadiumNeonGreen else MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Illustrations et cas pratiques :\n" +
                            "• Premier pari : Vous lancez une montante avec un objectif de bénéfice de 10 $ par palier. Vous ciblez une cote de 1.50.\n" +
                            "   Mise = (0 + 10) / (1.50 - 1) = 20 $\n" +
                            "• Scénario Perdant : Ce pari de 20 $ échoue. Vos pertes cumulées sont maintenant de 20 $.\n" +
                            "• Réajustement Intelligent : Pour votre prochain pari, vous trouvez une cote de 1.80.\n" +
                            "   Mise = (20 + 10) / (1.80 - 1) = 37.5 $\n" +
                            "• Scénario Gagnant : Ce pari l'emporte. Vos gains bruts sont de 37.5 * 1.80 = 67.5 $. " +
                            "Votre bénéfice réel net final est de 67.5 $ moins les mises globales engagées (20 $ + 37.5 $ = 57.5 $), soit exactement de +10 $ ! Les pertes précédentes ont été effacées en un coup.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = if (isStadium) StadiumTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Part 3
                    GuideSectionCard(
                        num = "3",
                        title = "Simulateur / Sandbox : Mode Bac à Sable",
                        isStadium = isStadium
                    ) {
                        Text(
                            "Le simulateur de 'Crash Test' théorique (disponible sur l'onglet Statistiques sous l'onglet SIMULATEUR) est conçu pour entraîner votre résistance mentale et éprouver vos stratégies sans aucun risque financier.\n\n" +
                            "1. Entrez une cote moyenne théorique (ex: 1.50).\n" +
                            "2. Définissez un taux de réussite estimé réaliste (ex: 60%).\n" +
                            "3. Ajustez le capital simulé et lancez 100 événements d'affilée.\n\n" +
                            "Le simulateur appliquera de manière autonome la loi des probabilités et la combinaison des pertes. Si votre capital est insuffisant pour supporter une mauvaise série de pertes consécutives, l'outil indiquera en gros 'CRASH' et analysera l'étape exacte de l'effondrement. Idéal pour trouver l'équilibre parfait capital/objectif.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = if (isStadium) StadiumTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Part 4
                    GuideSectionCard(
                        num = "4",
                        title = "Activation du Conseiller Stratégique IA",
                        isStadium = isStadium
                    ) {
                        Text(
                            "Le Conseiller IA utilise le modèle Google Gemini 1.5 pour auditer vos habitudes réelles de jeu.\n\n" +
                            "Pour l'utiliser :\n" +
                            "1. Obtenez une clé API gratuite en recherchant 'Google AI Studio API Key' sur le web.\n" +
                            "2. Collez cette clé dans la section 'Configuration Connexion IA' juste ci-dessus.\n" +
                            "3. Allez sur votre écran d'Accueil, cliquez sur 'Conseiller Stratégique IA' et appuyez sur 'GÉNÉRER MON CONSEIL'.\n\n" +
                            "L'IA ne formule pas d'avis générique. Elle scanne votre historique de paris, compare vos taux de réussite réels à vos paliers validés et synthétise une feuille de route ultra-claire avec un niveau de risque et trois prescriptions de discipline d'élite.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = if (isStadium) StadiumTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Part 5
                    GuideSectionCard(
                        num = "5",
                        title = "Faire Crash-Tester vos Idées de Pronos",
                        isStadium = isStadium
                    ) {
                        Text(
                            "Vous hésitez sur votre prochain pari ? Laissez Gemini l'auditer avant de le valider.\n\n" +
                            "Dans la carte 'Crash Test Gemini' de l'écran principal, écrivez votre idée (ex: 'Arsenal gagne et les deux équipes marquent') et la cote proposée. L'IA passera au crible la probabilité théorique, les pièges du match et conclura si le rapport risque/bénéfice est aligné avec la montante.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = if (isStadium) StadiumTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Part 6
                    GuideSectionCard(
                        num = "6",
                        title = "Analyseur Visuel de Coupons par Image",
                        isStadium = isStadium
                    ) {
                        Text(
                            "Idéal pour l'analyse instantanée de billets ou de reçus en direct.\n\n" +
                            "Depuis l'écran d'Accueil ou l'onglet 'Analyse Photo', téléchargez une image de coupon (ou prenez une photo). L'IA extraira directement :\n" +
                            "• Les noms des équipes à domicile et à l'extérieur.\n" +
                            "• Le pronostic détecté et sa cote globale.\n" +
                            "• Le respect de l'intervalle autorisé de votre montante.\n" +
                            "• Un audit et une recommandation finale d'acceptation du coupon.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = if (isStadium) StadiumTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Part 7
                    GuideSectionCard(
                        num = "7",
                        title = "Devise Unique Franc Congolais (CDF / FC)",
                        isStadium = isStadium
                    ) {
                        Text(
                            "L'application est parfaitement adaptée au marché de la RDC et fonctionne exclusivement en Franc Congolais (CDF / FC) :\n" +
                            "• Vos capitaux, objectifs, paliers et mises sont toujours saisis et calculés en FC.\n" +
                            "• Cela évite toute confusion de conversion et assure une gestion 100% stable de votre montante financière.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = if (isStadium) StadiumTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Close Button in footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isStadium) StadiumAnthracite else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStadium) StadiumNeonGreen else MaterialTheme.colorScheme.primary,
                            contentColor = if (isStadium) Color.Black else Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("COMPRIS, RETOURNER AUX PARAMÈTRES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GuideSectionCard(
    num: String,
    title: String,
    isStadium: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isStadium) StadiumAnthracite.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (isStadium) StadiumNeonGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = num,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isStadium) StadiumNeonGreen else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = if (isStadium) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}
@Composable
fun CreateMontanteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, Double, Double) -> Unit,
    activeCurrency: String = "CDF"
) {
    var name by remember { mutableStateOf("") }
    var capital by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var minCote by remember { mutableStateOf("") }
    var maxCote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle Montante Financière", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Configure ton objectif. Le calculateur ajustera de manière autonome la taille de tes mises après chaque perte.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom (ex: Objectif " + (if (activeCurrency == "CDF") "250 000 FC" else "100 $") + ")") },
                    singleLine = true,
                    modifier = Modifier.testTag("dialog_name_input")
                )

                OutlinedTextField(
                    value = capital,
                    onValueChange = { capital = it },
                    label = { Text("Capital de départ (" + (if (activeCurrency == "CDF") "FC" else "$") + ")") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.testTag("dialog_capital_input")
                )

                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Objectif final (" + (if (activeCurrency == "CDF") "FC" else "$") + ")") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.testTag("dialog_goal_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minCote,
                        onValueChange = { minCote = it },
                        label = { Text("Cote Min (ex: 1.30)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dialog_min_cote_input")
                    )

                    OutlinedTextField(
                        value = maxCote,
                        onValueChange = { maxCote = it },
                        label = { Text("Cote Max (ex: 1.60)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dialog_max_cote_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val capVal = capital.toDoubleOrNull() ?: 10.0
                    val goalVal = goal.toDoubleOrNull() ?: 100.0
                    val minVal = minCote.toDoubleOrNull() ?: 1.30
                    val maxVal = maxCote.toDoubleOrNull() ?: 1.60
                    
                    if (name.isNotBlank()) {
                        onConfirm(name, capVal, goalVal, minVal, maxVal)
                    }
                },
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text("DÉBUTER LA MONTANTE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun AddBetDialog(
    activeMontante: Montante?,
    bankrollState: BankrollState?,
    onDismiss: () -> Unit,
    onCalculateMise: (Double) -> Double,
    onConfirm: (String, Double, String) -> Unit,
    activeCurrency: String = "CDF",
    exchangeRate: Float = 2500f
) {
    var matchName by remember { mutableStateOf("") }
    var coteText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("1N2") }
    val types = listOf("1N2", "Over/Under Buts", "Les deux équipes marquent", "Buteur", "Autre")

    val minC = activeMontante?.coteMin ?: 1.30
    val maxC = activeMontante?.coteMax ?: 1.60

    val inputCote = coteText.toDoubleOrNull() ?: 0.0
    val respectsInterval = inputCote in minC..maxC

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une Prédiction", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Saisis ton pari. La mise recommandée sera instantanément et localement établie.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = matchName,
                    onValueChange = { matchName = it },
                    label = { Text("Affiche / Match (ex: PSG - Real)") },
                    singleLine = true,
                    modifier = Modifier.testTag("dialog_match_input")
                )

                OutlinedTextField(
                    value = coteText,
                    onValueChange = { coteText = it },
                    label = { Text("Côte du pari (ex: 1.45)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.testTag("dialog_cote_input")
                )

                // Warning indicator if not within strict interval
                if (coteText.isNotBlank()) {
                    if (!respectsInterval) {
                        Surface(
                            color = RedDanger.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ALERTE HORS RANGE ⚠️\nCette cote ($inputCote) n'est pas dans l'intervalle autorisé [$minC - $maxC] !",
                                color = RedDanger,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Surface(
                            color = GreenSuccess.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cote Conforme à la montante ✅",
                                color = GreenSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Type picker
                Text("Type de Pari :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedType)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, fontSize = 13.sp) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Dynamic preview of computed stake
                if (respectsInterval && inputCote > 1.0 && bankrollState != null) {
                    val recommended = onCalculateMise(inputCote)
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mise Estimée :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(formatCurrency(recommended, activeCurrency, exchangeRate), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val coteVal = coteText.toDoubleOrNull() ?: 0.0
                    if (matchName.isNotBlank() && coteVal > 1.0) {
                        onConfirm(matchName, coteVal, selectedType)
                    }
                },
                modifier = Modifier.testTag("dialog_submit_button")
            ) {
                Text("AJOUTER LE PARI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun AllMontantesDialog(
    montantes: List<Montante>,
    activeMontante: Montante?,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onDelete: (Montante) -> Unit,
    activeCurrency: String = "CDF",
    exchangeRate: Float = 2500f
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Charger une Montante", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(montantes) { montante ->
                        val isCurrent = montante.id == activeMontante?.id
                        Surface(
                            onClick = { onSelect(montante.id) },
                            color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(montante.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Objectif: " + formatCurrencyShort(montante.objectifFinal, activeCurrency, exchangeRate), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                
                                IconButton(onClick = { onDelete(montante) }) {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        contentDescription = "Corbeille",
                                        tint = RedDanger.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Fermer")
                }
            }
        }
    }
}
