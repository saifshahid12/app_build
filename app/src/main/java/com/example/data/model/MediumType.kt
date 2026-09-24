package com.example.data.model

enum class MediumType(
    val id: String,
    val displayName: String,
    val description: String,
    val defaultAspectRatio: String,
    val iconName: String
) {
    BILLBOARD(
        id = "BILLBOARD",
        displayName = "Highway Billboard",
        description = "Massive illuminated outdoor advertising billboard at twilight, dramatic urban skyline angle.",
        defaultAspectRatio = "16:9",
        iconName = "ViewDay"
    ),
    NEWSPAPER(
        id = "NEWSPAPER",
        displayName = "Printed Newspaper",
        description = "Authentic printed broadsheet newspaper advertisement with halftone ink texture and editorial columns.",
        defaultAspectRatio = "3:4",
        iconName = "Newspaper"
    ),
    SOCIAL_POST(
        id = "SOCIAL_POST",
        displayName = "Social Media Post",
        description = "Modern Instagram/social feed clean product flatlay with soft shadows and organic studio props.",
        defaultAspectRatio = "1:1",
        iconName = "Share"
    ),
    MAGAZINE(
        id = "MAGAZINE",
        displayName = "Glossy Magazine",
        description = "Double-page luxury magazine editorial advertisement with glossy sheen and minimal typography.",
        defaultAspectRatio = "3:4",
        iconName = "MenuBook"
    ),
    SUBWAY(
        id = "SUBWAY",
        displayName = "Transit Lightbox",
        description = "Illuminated backlit poster display in a sleek contemporary glass-and-steel transit pavilion.",
        defaultAspectRatio = "9:16",
        iconName = "DirectionsTransit"
    ),
    STOREFRONT(
        id = "STOREFRONT",
        displayName = "Storefront Vitrine",
        description = "Boutique flagship retail window display with spotlighting and geometric terrazzo podium.",
        defaultAspectRatio = "4:3",
        iconName = "Storefront"
    ),
    CUSTOM(
        id = "CUSTOM",
        displayName = "Custom Medium",
        description = "Custom ad medium configured by you (e.g. coffee sleeve, airport wall, vehicle wrap).",
        defaultAspectRatio = "1:1",
        iconName = "AutoAwesome"
    );

    companion object {
        fun fromId(id: String): MediumType = entries.find { it.id == id } ?: CUSTOM
    }
}
