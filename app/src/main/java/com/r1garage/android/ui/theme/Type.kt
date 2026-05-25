package com.r1garage.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.r1garage.android.R

/**
 * Inter via downloadable Google Fonts.
 *
 * Inter is the closest free analogue to Rivian's house typeface ("Hoover")
 * — geometric humanist sans with tight tracking and a thin Light cut that
 * works for huge telemetry numerals. Fonts are fetched at runtime by the
 * GMS provider so they don't bloat the APK; falls back gracefully to the
 * system sans if Play Services isn't available.
 */
private val GoogleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val InterGF = GoogleFont("Inter")

private val Sans = FontFamily(
    Font(googleFont = InterGF, fontProvider = GoogleFontsProvider, weight = FontWeight.Light),
    Font(googleFont = InterGF, fontProvider = GoogleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = InterGF, fontProvider = GoogleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = InterGF, fontProvider = GoogleFontsProvider, weight = FontWeight.SemiBold),
)

val AppTypography = Typography(
    // Huge numerals (battery %, range). Light weight for that "official
    // Rivian app" telemetry feel.
    displayLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Light,
        fontSize = 96.sp, lineHeight = 100.sp, letterSpacing = (-2.0).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Light,
        fontSize = 56.sp, lineHeight = 64.sp, letterSpacing = (-1.0).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp
    )
)
