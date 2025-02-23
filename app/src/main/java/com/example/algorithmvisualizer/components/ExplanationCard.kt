package com.example.algorithmvisualizer.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algorithmvisualizer.ui.theme.BackgroundCard
import com.example.algorithmvisualizer.ui.theme.TextColor

@Composable
fun ExplanationCard(
    explanationText: String,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 350.dp
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCard)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = explanationText,
                color = TextColor,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                textAlign = TextAlign.Justify
            )
        }
    }
}
