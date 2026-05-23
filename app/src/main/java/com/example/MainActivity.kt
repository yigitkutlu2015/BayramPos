package com.example

import android.os.Bundle
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Payment
import com.example.data.Profile
import com.example.ui.Bank
import com.example.ui.BankList
import com.example.ui.PosViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: PosViewModel = viewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (profile == null || profile?.ownerName.isNullOrBlank() || profile?.iban.isNullOrBlank()) {
            ProfileSetupScreen(
                onSave = { name, iban ->
                    viewModel.saveProfile(name, iban)
                }
            )
        } else {
            PosTerminalScreen(profile = profile!!, viewModel = viewModel)
        }
    }
}

// 1. PROFILE SETUP / KURULUM EKRANI
@Composable
fun ProfileSetupScreen(onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var ibanInput by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var ibanError by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8F5E9), // Soft Eid Green
                        Color(0xFFC8E6C9),
                        Color(0xFFE8F5E9)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF81C784), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Festive Welcome Icon/Header
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍬", fontSize = 38.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Bayram Harçlık POS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Lütfen harçlıkların yatırılacağı banka bilgilerini gir ve sanal POS cihazını hemen kurun!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.isBlank()) "Lütfen hesap sahibinin adını girin" else null
                    },
                    label = { Text("Hesap Sahibi (Ad Soyadı/Veli Adı)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User", tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.fillMaxWidth().testTag("setup_name_input"),
                    isError = nameError != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        focusedLabelColor = Color(0xFF4CAF50)
                    )
                )
                
                if (nameError != null) {
                    Text(
                        text = nameError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, start = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // IBAN input field with auto formatter suggestion or TR prefixing auto handles
                OutlinedTextField(
                    value = ibanInput,
                    onValueChange = {
                        var cleaned = it.uppercase().replace(" ", "")
                        if (cleaned.length > 26) cleaned = cleaned.take(26)
                        
                        // Formatter visual input simulation helper
                        val formatted = StringBuilder()
                        for (i in cleaned.indices) {
                            formatted.append(cleaned[i])
                            if ((i == 1 || i == 5 || i == 9 || i == 13 || i == 17 || i == 21) && i < cleaned.lastIndex) {
                                formatted.append(" ")
                            }
                        }
                        ibanInput = formatted.toString()
                        
                        ibanError = when {
                            cleaned.isBlank() -> "Lütfen bir IBAN numarası girin"
                            cleaned.length < 26 -> "Eksik karakter! Bir IBAN 26 haneli olmalıdır"
                            else -> null
                        }
                    },
                    label = { Text("IBAN Numarası") },
                    placeholder = { Text("TR00 0000 0000 0000 0000 0000 00") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = "IBAN", tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.fillMaxWidth().testTag("setup_iban_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    isError = ibanError != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        focusedLabelColor = Color(0xFF4CAF50)
                    )
                )
                
                if (ibanError != null) {
                    Text(
                        text = ibanError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, start = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        val cleanIban = ibanInput.replace(" ", "")
                        var hasError = false
                        
                        // Real Validations
                        if (name.trim().isBlank()) {
                            nameError = "Hesap sahibinin adı boş bırakılamaz"
                            hasError = true
                        }
                        if (cleanIban.isBlank() || cleanIban.length < 26) {
                            ibanError = "Geçersiz veya eksik IBAN!"
                            hasError = true
                        }
                        
                        if (!hasError) {
                            onSave(name.trim(), ibanInput)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_setup_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Sanal POS Cihazını Kur 🚀",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

// 2. MAIN POS DEVICE / SANAL POS CIHAZI EKRANI (BEKO X30TR PREMIUM DETAILED BODY & INTERFACE)
@Composable
fun PosTerminalScreen(profile: Profile, viewModel: PosViewModel) {
    val haptic = LocalHapticFeedback.current
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    
    // Formatting helper
    val formattedIban = remember(profile.iban) {
        val clean = profile.iban.replace(" ", "")
        val formatted = StringBuilder()
        for (i in clean.indices) {
            formatted.append(clean[i])
            if ((i == 1 || i == 5 || i == 9 || i == 13 || i == 17 || i == 21) && i < clean.lastIndex) {
                formatted.append(" ")
            }
        }
        formatted.toString()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141517)) // Slate terminal ambient room backdrop
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant top menu for POS features (Settings/History)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info block
                Column {
                    Text(
                        text = "POS OPERATÖRÜ",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = profile.ownerName,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // History Screen Trigger
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.showHistoryScreen = true
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF27292D))
                    ) {
                        BadgedBox(
                            badge = {
                                if (payments.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFF4CAF50)) {
                                        Text(payments.size.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = "Records", tint = Color.White)
                        }
                    }
                    
                    // Reset Profil Trigger
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteProfile()
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit Settings", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // THE 3D BEKO X30TR POS DEVICE CORE ASSEMBLY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2B2D2F), Color(0xFF191A1B)) // Matte charcoal grey plastic
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(2.dp, Color(0xFF434548), RoundedCornerShape(28.dp))
                    .shadow(20.dp, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 12.dp, start = 12.dp, end = 12.dp, top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Roll compartment cover at the very top (Yazıcı Kağıt Yuvası Bombesi)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .height(20.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF121314), Color(0xFF282A2C), Color(0xFF18191A))
                                ),
                                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
                            )
                            .border(1.dp, Color(0xFF343638), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 3.dp, bottomEnd = 3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // High Sheen highlighted line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.12f))
                        )
                    }

                    // 2. Paper Feed / Tear Slot (Fatura Kesme Yuvası)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(14.dp)
                            .background(Color(0xFF0C0D0E), RoundedCornerShape(3.dp))
                            .border(1.dp, Color(0xFF2B2C2E), RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .height(2.dp)
                                .background(Color.Black)
                        )
                    }

                    // Sliding Receipt Paper Component Animation
                    AnimatedVisibility(
                        visible = viewModel.isPrintingReceipt || viewModel.lastPrintedPayment != null,
                        enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        ReceiptRollView(
                            payment = viewModel.lastPrintedPayment,
                            profile = profile,
                            formattedIban = formattedIban,
                            isPrinting = viewModel.isPrintingReceipt,
                            onCloseReceipt = {
                                viewModel.onCancelPressed()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. THE SLEEK TOUCHSCREEN GLASS PANEL
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f) // Prominent Android Tablet Glass Bezel
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF090A0A)) // Glossy pitch-black bezel framework
                            .border(2.5.dp, Color(0xFF212224), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // THE INTERACTIVE BEKO Android App Display Screen (Token Partner Soft layout)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF0F2F5)) // Premium white/light gray app background style
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.selectBankDirectly()
                                }
                                .testTag("pos_lcd_display"),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // 3A. Android OS Status Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(18.dp)
                                        .background(Color(0xFF101316))
                                        .padding(horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Wifi,
                                            contentDescription = "WiFi Online",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "WiFi GÜÇLÜ",
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFECEFF1),
                                            fontSize = 8.sp
                                        )
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Güvenli Alt Yapı",
                                            tint = Color(0xFF2196F3),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "TR-POS",
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF90A4AE),
                                            fontSize = 8.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "98%",
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFECEFF1),
                                            fontSize = 8.sp
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            Icons.Default.BatteryFull,
                                            contentDescription = "Battery Full",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }

                                // 3B. Dynamic Application View states
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    if (viewModel.isPrintingReceipt) {
                                        // Awaiting receipt output
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            CircularProgressIndicator(
                                                color = Color(0xFF1E293B),
                                                strokeWidth = 3.dp,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "FİŞ YAZDIRILIYOR",
                                                fontFamily = FontFamily.SansSerif,
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "Makbuz hazırlanıyor, rulo dönüyor...",
                                                fontFamily = FontFamily.SansSerif,
                                                color = Color.Gray,
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else if (viewModel.isQrGenerated) {
                                        // QR FAST payment instruction state
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val bankColor = viewModel.selectedBank?.primaryColor ?: Color(0xFF1E293B)
                                            
                                            // Selected Bank Banner Bar
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(bankColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                    .border(1.dp, bankColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = viewModel.selectedBank?.name ?: "",
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontWeight = FontWeight.Bold,
                                                    color = bankColor,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(3.dp),
                                                    color = bankColor,
                                                    modifier = Modifier.wrapContentSize()
                                                ) {
                                                    Text(
                                                        text = (viewModel.selectedBank?.logoText ?: "TR").take(2).uppercase(),
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = viewModel.selectedBank?.textColor ?: Color.White,
                                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            // Row containing receiver information and QR
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(0.55f),
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "Ödeme Alıcı:",
                                                        fontFamily = FontFamily.SansSerif,
                                                        color = Color.Gray,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = profile.ownerName,
                                                        fontFamily = FontFamily.SansSerif,
                                                        color = Color(0xFF0F172A),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    
                                                    Text(
                                                        text = "Ödenecek Tutar:",
                                                        fontFamily = FontFamily.SansSerif,
                                                        color = Color.Gray,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = viewModel.getFormattedAmount(),
                                                        fontFamily = FontFamily.SansSerif,
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 16.sp
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.height(5.dp))
                                                    
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(3.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.5.dp)
                                                    ) {
                                                        Text(
                                                            text = "FAST TR-QR",
                                                            color = Color(0xFF2E7D32),
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 7.5.sp,
                                                            fontFamily = FontFamily.SansSerif
                                                        )
                                                    }
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .weight(0.45f)
                                                        .aspectRatio(1f)
                                                        .background(Color.White, RoundedCornerShape(6.dp))
                                                        .border(1.5.dp, bankColor, RoundedCornerShape(6.dp))
                                                        .padding(4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    viewModel.qrBitmap?.let { bitmap ->
                                                        androidx.compose.foundation.Image(
                                                            bitmap = bitmap.asImageBitmap(),
                                                            contentDescription = "FAST Ödeme QR Kod",
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            }

                                            // Blinking confirmation action tag
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF2E7D32).copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                                    .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Ödemeyi tamamlayınca YEŞİL (GİRİŞ) tuşuna basın.",
                                                    color = Color(0xFF1B5E20),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        // General Sale numeric input screen
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "SATIŞ İŞLEMİ",
                                                    color = Color(0xFF475569),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp,
                                                    letterSpacing = 0.5.sp
                                                )
                                                
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(Color(0xFF4CAF50), CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "ÇEVRİMİÇİ",
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }

                                            // Elegant Numeric Input Display Card
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .shadow(1.5.dp, RoundedCornerShape(8.dp)),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "TAHSİL EDİLECEK TUTAR",
                                                        color = Color(0xFF64748B),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 8.sp,
                                                        letterSpacing = 1.sp
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = viewModel.getFormattedAmount(),
                                                            color = Color(0xFF0F172A),
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 24.sp
                                                        )
                                                        
                                                        // Soft indicator cursor
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .width(2.dp)
                                                                .height(20.dp)
                                                                .background(Color(0xFF1E293B))
                                                        )
                                                    }
                                                }
                                            }

                                            // Quick Amount Select Chips (Very clever playground addition!)
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = "Hızlı Harçlık Ekle:",
                                                    color = Color(0xFF64748B),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    val quickAmounts = listOf(
                                                        5000 to "50 ₺",
                                                        10000 to "100 ₺",
                                                        20000 to "200 ₺",
                                                        50000 to "500 ₺"
                                                    )
                                                    quickAmounts.forEach { (cents, label) ->
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(26.dp)
                                                                .background(Color.White, RoundedCornerShape(4.dp))
                                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                                                .clickable {
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    viewModel.setQuickAmount(cents.toString())
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = label,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF1E293B),
                                                                fontSize = 9.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Selected Bank status card
                                            if (viewModel.selectedBank != null) {
                                                val bank = viewModel.selectedBank!!
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(bank.primaryColor.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                        .border(1.dp, bank.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            viewModel.isSelectingBank = true
                                                        }
                                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(22.dp)
                                                                .background(bank.primaryColor, RoundedCornerShape(4.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = bank.logoText.take(2).uppercase(),
                                                                color = bank.textColor,
                                                                fontSize = 7.sp,
                                                                fontWeight = FontWeight.ExtraBold
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Column {
                                                            Text(
                                                                text = bank.name,
                                                                color = Color(0xFF0F172A),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 9.sp
                                                            )
                                                            Text(
                                                                text = "Alıcı banka seçildi, değiştirmek için dokunun",
                                                                color = Color.Gray,
                                                                fontSize = 7.sp
                                                            )
                                                        }
                                                    }
                                                    Icon(
                                                        Icons.Default.ChevronRight,
                                                        contentDescription = "Değiştir",
                                                        tint = bank.primaryColor,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            } else {
                                                // Bank not selected alert state
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            viewModel.isSelectingBank = true
                                                        }
                                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(22.dp)
                                                                .background(Color(0xFFEF4444), RoundedCornerShape(4.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("TL", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Column {
                                                            Text(
                                                                text = "LÜTFEN BANKA SEÇİNİZ",
                                                                color = Color(0xFFB91C1C),
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 9.sp
                                                            )
                                                            Text(
                                                                text = "Tahsilat için alıcı bankayı buraya tıklayarak seçin",
                                                                color = Color.Gray,
                                                                fontSize = 7.sp
                                                            )
                                                        }
                                                    }
                                                    Icon(
                                                        Icons.Default.ArrowDropDown,
                                                        contentDescription = "Seç",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3C. Android OS Screen Touch Footer Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                        .background(Color(0xFFE2E8F0))
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val timeString = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("tr", "TR")).format(Date()) }
                                    Text(
                                        text = timeString,
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 7.sp
                                    )
                                    Text(
                                        text = if (viewModel.isQrGenerated) "TEMASSIZ QR MODU" else "BEKO X30TR AKILLI SISTEM",
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 7.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. THE SIGNATURE BEKO X30TR METAL BEZEL DIVIDER STRIP
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF343638), Color(0xFF222325))
                                )
                            )
                            .drawBehind {
                                drawLine(Color(0xFF4A4D51), start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                                drawLine(Color(0xFF131415), start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                            }
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Operational Status indicator LED diodes
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // LED 1: Active system power (vibrant blinking green)
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                                    .shadow(elevation = 2.dp, shape = CircleShape)
                            )
                            // LED 2: Safe communication channels active (flashing teal blue)
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(Color(0xFF00B0FF), CircleShape)
                            )
                        }

                        // Centered lowercase 'beko' logo exactly corresponding to real hardware branding
                        Text(
                            text = "beko",
                            color = Color(0xFFECEFF1),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = (-0.5).sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )

                        // Model code sign-off
                        Text(
                            text = "X30TR",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. THE CLASSIC RECESSED PHYSICAL KEYBOARD PANEL
                    KeyboardPanelGrid(
                        onKeyClick = { key ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            when (key) {
                                'C' -> viewModel.onClearPressed() // Yellow clear key
                                'X' -> viewModel.onCancelPressed() // Red cancel key
                                'E' -> viewModel.onEnterPressed() // Green confirm key
                                '0' -> viewModel.onDigitPressed('0')
                                '1' -> viewModel.onDigitPressed('1')
                                '2' -> viewModel.onDigitPressed('2')
                                '3' -> viewModel.onDigitPressed('3')
                                '4' -> viewModel.onDigitPressed('4')
                                '5' -> viewModel.onDigitPressed('5')
                                '6' -> viewModel.onDigitPressed('6')
                                '7' -> viewModel.onDigitPressed('7')
                                '8' -> viewModel.onDigitPressed('8')
                                '9' -> viewModel.onDigitPressed('9')
                                'D' -> viewModel.onDoubleZeroPressed() // 00 key
                                'M' -> {
                                    // Open Menu / History Dialog using virtual MENU key
                                    viewModel.showHistoryScreen = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // BANK SELECTION POPUP OVERLAY
    if (viewModel.isSelectingBank) {
        BankSelectionDialog(
            onDismiss = { viewModel.isSelectingBank = false },
            onBankSelected = { bank ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.onBankSelected(bank)
            }
        )
    }

    // PAYMENTS AND REPORTS DIALOG
    if (viewModel.showHistoryScreen) {
        HistoryRecordsDialog(
            payments = payments,
            onDismiss = { viewModel.showHistoryScreen = false },
            onClearAll = {
                viewModel.clearHistory()
                viewModel.showHistoryScreen = false
            }
        )
    }
}

// 3. SELECTION OF TURKISH BANKS GRID
@Composable
fun BankSelectionDialog(
    onDismiss: () -> Unit,
    onBankSelected: (Bank) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .shadow(16.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2124)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Banka Seçiniz 🏦",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Lütfen harçlığın yatacağı bankayı seçin.",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BankList) { bank ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clickable { onBankSelected(bank) }
                                .testTag("bank_tile_${bank.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2F33)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Programmatic stylized Bank Logo avatar
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(bank.primaryColor, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bank.logoText.take(2).uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = bank.textColor
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = bank.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Kapat", color = Color.White)
                }
            }
        }
    }
}

// 4. ANIMATED PRINTING RECEIPT COMPONENT
@Composable
fun ReceiptRollView(
    payment: Payment?,
    profile: Profile,
    formattedIban: String,
    isPrinting: Boolean,
    onCloseReceipt: () -> Unit
) {
    if (payment == null) return
    
    val dateString = remember(payment.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("tr", "TR")).format(Date(payment.timestamp))
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .shadow(12.dp, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .testTag("printed_receipt"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), // Retro thermal paper off-white
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Draw vector tear jagged edge at the top representing sliced receipt
                    drawTearEdge(Color(0xFF263238), isTop = true)
                }
                .padding(top = 18.dp) // Offset top tearing edge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Receipt Header
                Text(
                    text = "★ BAYRAM MOBİL POS ★",
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "MÜŞTERİ BİLGİ FİŞİ",
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "------------------------------------",
                    fontFamily = FontFamily.Monospace, color = Color.LightGray, fontSize = 10.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TARIH:", fontFamily = FontFamily.Monospace, color = Color.Black, fontSize = 9.sp)
                    Text(dateString, fontFamily = FontFamily.Monospace, color = Color.Black, fontSize = 9.sp)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ALICI:", fontFamily = FontFamily.Monospace, color = Color.Black, fontSize = 9.sp)
                    Text(profile.ownerName.uppercase(), fontFamily = FontFamily.Monospace, color = Color.Black, fontSize = 9.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("BANKA:", fontFamily = FontFamily.Monospace, color = Color.Black, fontSize = 9.sp)
                    Text(payment.bankName, fontFamily = FontFamily.Monospace, color = Color.Black, fontSize = 9.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "ALICI IBAN:",
                    fontFamily = FontFamily.Monospace, color = Color.Black, fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = formattedIban,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Text(
                    text = "------------------------------------",
                    fontFamily = FontFamily.Monospace, color = Color.LightGray, fontSize = 10.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOPLAM HARÇLIK:",
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = String.format(Locale("tr", "TR"), "%,.2f ₺", payment.amount),
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "DURUM: ONAYLANDI (SUCCESS)",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "------------------------------------",
                    fontFamily = FontFamily.Monospace, color = Color.LightGray, fontSize = 10.sp
                )
                
                // Eid Greeting inside thermal slip
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Şeker tadında bir bayram dileriz!\nBayramınız Mübarek Olsun! 🍬🐑",
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "------------------------------------",
                    fontFamily = FontFamily.Monospace, color = Color.LightGray, fontSize = 10.sp
                )
                
                // Copy/Close action
                if (!isPrinting) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onCloseReceipt,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tamam (Yeni İşlem)", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Jagged thermal receipt tearing edge
fun DrawScope.drawTearEdge(color: Color, isTop: Boolean) {
    val path = Path()
    val triangleWidth = 10.dp.toPx()
    val triangleHeight = 6.dp.toPx()
    val width = size.width
    val y = if (isTop) 0f else size.height
    val direction = if (isTop) 1f else -1f
    
    path.moveTo(0f, y)
    var x = 0f
    while (x < width) {
        val nextX = x + triangleWidth
        val apexY = y + direction * triangleHeight
        path.lineTo(x + triangleWidth / 2, apexY)
        path.lineTo(nextX, y)
        x = nextX
    }
    path.lineTo(width, if (isTop) size.height else 0f)
    path.lineTo(0f, if (isTop) size.height else 0f)
    path.close()
    drawPath(path, color)
}

// 5. CLASSIC 3D TACTILE KEYS PAD COMPONENT
@Composable
fun KeyboardPanelGrid(
    onKeyClick: (Char) -> Unit
) {
    // 3 Columns + 1 functional sidebar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left Column contains numbers 1-9, C (Yellow Clear), 0, 00
        Column(
            modifier = Modifier.weight(0.75f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = listOf(
                listOf('1', '2', '3'),
                listOf('4', '5', '6'),
                listOf('7', '8', '9'),
                listOf('C', '0', 'D') // C is Backspace, D is "00"
            )
            
            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowKeys.forEach { key ->
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            PosTactileKey(
                                label = when(key) {
                                    'C' -> "SİL"
                                    'D' -> "00"
                                    else -> key.toString()
                                },
                                keyColor = when (key) {
                                    'C' -> Color(0xFFFBC02D) // Yellow backspace key colour
                                    else -> Color(0xFFCFD8DC) // Default tactile beige key color
                                },
                                textColor = when(key) {
                                    'C' -> Color.Black
                                    else -> Color(0xFF263238)
                                },
                                onClick = { onKeyClick(key) }
                            )
                        }
                    }
                }
            }
        }
        
        // Right static Column contains IPTAL (Red) and GIRIS (Green)
        Column(
            modifier = Modifier.weight(0.25f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cancel Button - Red
            Box(modifier = Modifier.weight(1f)) {
                PosTactileKey(
                    label = "İPTAL",
                    keyColor = Color(0xFFD32F2F), // Bright POS Cancel Red color
                    textColor = Color.White,
                    onClick = { onKeyClick('X') }
                )
            }
            
            // Enter Button - Green
            Box(modifier = Modifier.weight(1f)) {
                PosTactileKey(
                    label = "GİRİŞ",
                    keyColor = Color(0xFF388E3C), // Bright POS Green OK button
                    textColor = Color.White,
                    onClick = { onKeyClick('E') }
                )
            }
        }
    }
}

// 3D tactile POS keyboard key custom drawing block
@Composable
fun PosTactileKey(
    label: String,
    keyColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val dynamicShadowOffset by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        label = "elevation_anim"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = dynamicShadowOffset,
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .background(
                Brush.verticalGradient(
                    listOf(keyColor, keyColor.darken(0.15f))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, keyColor.lighten(0.2f), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                letterSpacing = 0.5.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

// Core color blending helper extensions (darken/lighten) to create authentic 3D plastics
fun Color.darken(value: Float): Color {
    return Color(
        red = (this.red * (1f - value)).coerceIn(0f, 1f),
        green = (this.green * (1f - value)).coerceIn(0f, 1f),
        blue = (this.blue * (1f - value)).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}

fun Color.lighten(value: Float): Color {
    return Color(
        red = (this.red + (1f - this.red) * value).coerceIn(0f, 1f),
        green = (this.green + (1f - this.green) * value).coerceIn(0f, 1f),
        blue = (this.blue + (1f - this.blue) * value).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}

// 6. HISTORY LOGS / "İşlem Listesi" SHEET COMPONENT
@Composable
fun HistoryRecordsDialog(
    payments: List<Payment>,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .shadow(16.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2124)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gün Sonu Raporu (Geçmiş) 📈",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = "Bu bayram boyunca toplanan toplam harçlıklar",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )
                
                val totalCollected = remember(payments) {
                    payments.sumOf { it.amount }
                }

                // Header Stats card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TOPLAM BİRİKİM", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = String.format(Locale("tr", "TR"), "%,.2f ₺", totalCollected),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "${payments.size} adet harçlık ödemesi alındı",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (payments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🤔", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Henüz hiç işlem kaydı yok", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                            Text("Cihaz ile ilk tahsilatınızı yapın!", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(payments) { payment ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2F33)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(payment.bankName, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        val formattedDate = SimpleDateFormat("dd MMM, HH:mm", Locale("tr", "TR")).format(Date(payment.timestamp))
                                        Text(formattedDate, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                    }
                                    
                                    Text(
                                        text = String.format(Locale("tr", "TR"), "+%,.2f ₺", payment.amount),
                                        color = Color(0xFF81C784),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (payments.isNotEmpty()) {
                        Button(
                            onClick = onClearAll,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Sıfırla 🛑", color = Color.White)
                        }
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("Kapat", color = Color.White)
                    }
                }
            }
        }
    }
}
