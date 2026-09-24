package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductPreset
import com.example.data.model.ProductPresets
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandTeal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateCampaignScreen(
    onNavigateBack: () -> Unit,
    onCreateCampaign: (
        title: String,
        category: String,
        tagline: String,
        description: String,
        primaryColorHex: String,
        accentColorHex: String,
        materialFinish: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Beverage") }
    var tagline by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var primaryColorHex by remember { mutableStateOf("#0EA5E9") }
    var accentColorHex by remember { mutableStateOf("#F59E0B") }
    var materialFinish by remember { mutableStateOf("Matte frosted finish with metallic embossed details") }

    val categories = listOf(
        "Beverage", "Tech", "Cosmetics", "Luxury", "Food", "Fashion", "Audio", "Home"
    )

    val colorSwatches = listOf(
        Pair("#0EA5E9", "#F59E0B"), // Sky Blue & Amber
        Pair("#6366F1", "#EC4899"), // Indigo & Pink
        Pair("#10B981", "#FBBF24"), // Emerald & Gold
        Pair("#1E293B", "#38BDF8"), // Obsidian & Cyan
        Pair("#78350F", "#D97706"), // Amber & Ochre
        Pair("#BE185D", "#FDE047")  // Crimson & Lemon
    )

    fun applyPreset(preset: ProductPreset) {
        title = preset.name
        category = preset.category
        tagline = preset.tagline
        description = preset.description
        primaryColorHex = preset.primaryColorHex
        accentColorHex = preset.accentColorHex
        materialFinish = preset.materialFinish
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Product Concept",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("create_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Nano-Banana & Consistency Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BrandIndigo.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BrandIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Powered by Nano-Banana (gemini-2.5-flash-image)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandIndigo
                        )
                        Text(
                            text = "Maintains strict product consistency across all mediums. People are strictly excluded.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Preset Bar
            Text(
                text = "QUICK INSPIRATION PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProductPresets.presets.forEach { preset ->
                    Card(
                        modifier = Modifier
                            .clickable { applyPreset(preset) }
                            .testTag("preset_${preset.name.lowercase().replace(" ", "_")}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = preset.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Product Name
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Name *") },
                placeholder = { Text("e.g. Lumina Hydrating Elixir") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips
            Text(
                text = "Product Category",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = category.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandIndigo.copy(alpha = 0.2f),
                            selectedLabelColor = BrandIndigo
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            OutlinedTextField(
                value = tagline,
                onValueChange = { tagline = it },
                label = { Text("Campaign Tagline / Slogan") },
                placeholder = { Text("e.g. Pure Radiance, Zero Compromise") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_tagline_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Product Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Visual & Physical Product Specs *") },
                placeholder = { Text("Describe the packaging shape, bottle/can/box dimensions, logo typography, unique physical traits...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_description_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Color Palette Selector
            Text(
                text = "Colorway Palette",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colorSwatches.forEach { (pri, acc) ->
                    val isSelected = primaryColorHex.equals(pri, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) BrandIndigo else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable {
                                primaryColorHex = pri
                                accentColorHex = acc
                            }
                            .padding(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .fillMaxSize()
                                    .background(
                                        Color(android.graphics.Color.parseColor(pri))
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxSize()
                                    .background(
                                        Color(android.graphics.Color.parseColor(acc))
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Material and Texture
            OutlinedTextField(
                value = materialFinish,
                onValueChange = { materialFinish = it },
                label = { Text("Material Finish & Texture") },
                placeholder = { Text("e.g. Heavy frosted glass with brushed rose gold metallic cap") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_material_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onCreateCampaign(
                            title,
                            category,
                            tagline,
                            description,
                            primaryColorHex,
                            accentColorHex,
                            materialFinish
                        )
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("create_campaign_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create & Launch Brand Suite",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
