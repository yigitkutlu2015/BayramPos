package com.example.ui

import androidx.compose.ui.graphics.Color

data class Bank(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val textColor: Color = Color.White,
    val logoText: String,
    val tcmbCode: String, // Bank FAST standard code
    val description: String = ""
)

val BankList = listOf(
    Bank("ziraat", "Ziraat Bankası", Color(0xFFE10514), Color.White, "Ziraat", "0010", "T.C. Ziraat Bankası A.Ş."),
    Bank("isbank", "Türkiye İş Bankası", Color(0xFF0033A0), Color.White, "İş Bank", "0046", "Türkiye İş Bankası A.Ş."),
    Bank("garanti", "Garanti BBVA", Color(0xFF007A33), Color.White, "Garanti", "0062", "T. Garanti Bankası A.Ş."),
    Bank("akbank", "Akbank", Color(0xFFE30613), Color.White, "Akbank", "0046", "Akbank T.A.Ş."),
    Bank("yapi-kredi", "Yapı Kredi", Color(0xFF002F6C), Color(0xFFFFD400), "YapıKredi", "0067", "Yapı ve Kredi Bankası A.Ş."),
    Bank("vakifbank", "VakıfBank", Color(0xFFF2BC1B), Color(0xFF1B2A4A), "VakıfBank", "0015", "Türkiye Vakıflar Bankası T.A.O."),
    Bank("halkbank", "Halkbank", Color(0xFF0091EA), Color.White, "Halkbank", "0012", "Türkiye Halk Bankası A.Ş."),
    Bank("qnb", "QNB Finansbank", Color(0xFF0B2B5C), Color.White, "QNB", "0111", "QNB Finansbank A.Ş."),
    Bank("denizbank", "DenizBank", Color(0xFF004B87), Color(0xFFFFD400), "Deniz", "0134", "DenizBank A.Ş."),
    Bank("teb", "TEB", Color(0xFF009639), Color.White, "TEB", "0032", "Türk Ekonomi Bankası A.Ş."),
    Bank("papara", "Papara", Color(0xFF111111), Color(0xFFD4AF37), "Papara", "8001", "Papara Elektronik Para A.Ş."),
    Bank("enpara", "Enpara.com", Color(0xFFE20074), Color.White, "enpara", "0111", "QNB Finansbank A.Ş. (Enpara)")
)
