package com.example.data.model

data class ProductPreset(
    val name: String,
    val category: String,
    val tagline: String,
    val description: String,
    val primaryColorHex: String,
    val accentColorHex: String,
    val materialFinish: String
)

object ProductPresets {
    val presets = listOf(
        ProductPreset(
            name = "AeroPulse Spark",
            category = "Beverage",
            tagline = "Pure Electric Hydration",
            description = "Sleek 330ml slim aluminum beverage can with matte textured finish. Minimalist vertical typography reading 'AEROPULSE', crowned with an embossed thunderbolt emblem. Micro-condensation droplets clinging to the cold surface.",
            primaryColorHex = "#0EA5E9",
            accentColorHex = "#F59E0B",
            materialFinish = "Matte anodized aluminum with gloss UV embossed accents"
        ),
        ProductPreset(
            name = "Aura Bloom Serum",
            category = "Cosmetics / Skincare",
            tagline = "Radiance From Within",
            description = "Heavy frosted cylindrical glass bottle with a brushed rose gold dropper dispenser. Soft peach-tinted translucent botanical serum visible through the glass. Delicate serif branding along the lower rim.",
            primaryColorHex = "#F472B6",
            accentColorHex = "#FDE68A",
            materialFinish = "Heavy frosted fluted glass with brushed rose gold metallic dropper"
        ),
        ProductPreset(
            name = "Zenith Horizon ANC",
            category = "Consumer Audio",
            tagline = "Silence Speaks Loudest",
            description = "Sculptural over-ear wireless headphones featuring seamless matte obsidian earcups, memory foam magnetic cushions, and laser-etched geometric titanium headband adjustment arms.",
            primaryColorHex = "#1E293B",
            accentColorHex = "#6366F1",
            materialFinish = "Satin matte carbon composite with precision-machined titanium"
        ),
        ProductPreset(
            name = "Komorebi Cold Brew",
            category = "Artisan Coffee",
            tagline = "Steeped in Stillness",
            description = "Amber pharmaceutical-grade glass bottle with a cork stopper and a textured unbleached washi paper label. Minimalist Japanese calligraphy and botanical branch motif in rich sumi ink.",
            primaryColorHex = "#78350F",
            accentColorHex = "#D97706",
            materialFinish = "Vintage amber glass bottle with textured Japanese washi paper label"
        ),
        ProductPreset(
            name = "Veloce Chrono-X",
            category = "Luxury Goods",
            tagline = "Precision Defined",
            description = "Minimalist luxury chronograph watch featuring a deep sunburst emerald dial, forged carbon case, and an integrated fluoroelastomer strap. Faceted sapphire crystal with anti-reflective coating.",
            primaryColorHex = "#064E3B",
            accentColorHex = "#10B981",
            materialFinish = "Forged carbon fiber case with anti-reflective sapphire glass"
        )
    )
}
