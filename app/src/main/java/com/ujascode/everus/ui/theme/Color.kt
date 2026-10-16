package com.ujascode.everus.ui.theme

import androidx.compose.ui.graphics.Color

// Everus Color Palette - Premium Dark Theme
// Background colors
val EverusBackground = Color(0xFF08090B)   // #08090B - main background
val LightEverusBackground = Color(0xFFFFFFFF) // White - kept for light theme compatibility

// Surface colors for cards, sheets, etc.
val EverusSurface = Color(0xFF111216)      // #111216 - cards, surfaces
val LightEverusSurface = Color(0xFFFFFFFF) // White - light theme surface

// Elevated surface colors
val EverusElevatedSurface = Color(0xFF17191E) // #17191E - elevated surfaces
val LightEverusElevatedSurface = Color(0xFFFFFFFF) // White - light theme elevated

// Primary text colors
val EverusPrimaryText = Color(0xFFF5F7FA)   // #F5F7FA - primary text
val LightEverusPrimaryText = Color(0xFF000000) // Black - light theme primary text

// Secondary text colors
val EverusSecondaryText = Color(0xFF9AA0AA) // #9AA0AA - secondary text
val LightEverusSecondaryText = Color(0xFF666666) // Dark gray - light theme secondary text

// Primary colors for branding and key actions
val EverusPrimary = Color(0xFF00C7B4)      // Teal/Cyan - secure, trustworthy (keeping brand color)
val LightEverusPrimary = Color(0xFF00C7B4) // Same for light theme

// Secondary colors for less prominent actions
val EverusSecondary = Color(0xFF6C63FF)    // Purple - secondary actions (keeping brand color)
val LightEverusSecondary = Color(0xFF6C63FF)

// Accent/Tertiary colors for highlights
val EverusAccent = Color(0xFFFF6B6B)       // Coral red - alerts, important notices (keeping brand color)
val LightEverusAccent = Color(0xFFFF6B6B)

// Border/outline colors - subtle and restrained
val EverusBorder = Color(0xFF2A2F38)       // Dark gray with blue tint - borders, dividers
val LightEverusBorder = Color(0xFFE0E0E0)  // Light gray - light theme border

// Disabled states
val EverusDisabled = Color(0xFF4A4F58)     // Medium dark gray for disabled elements
val LightEverusDisabled = Color(0xFFB0B0B0) // Light gray for light theme disabled

// Error/success colors (keeping semantic meaning but adjusting for dark theme)
val EverusError = Color(0xFFFF6B6B)        // Coral red - errors
val LightEverusError = Color(0xFFFF6B6B)
val EverusSuccess = Color(0xFF34C759)      // Green - success
val LightEverusSuccess = Color(0xFF34C759)
val EverusWarning = Color(0xFFFFD166)      // Yellow - warnings
val LightEverusWarning = Color(0xFFFFD166)
