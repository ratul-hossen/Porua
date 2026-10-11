package com.porua.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Corner radius matching Figma Design System
// sm: 8dp, md: 12dp, lg: 16dp, xl: 24dp, full: CircleShape
val PoruaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val ShapeSm = RoundedCornerShape(8.dp)
val ShapeMd = RoundedCornerShape(12.dp)
val ShapeLg = RoundedCornerShape(16.dp)
val ShapeXl = RoundedCornerShape(24.dp)
val ShapeFull = CircleShape
