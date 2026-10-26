package com.usj.news_application.models

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>?
)

data class Article(
    val source: Source?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
) {
    fun toNewsItem(): News {
        val defaultId = url ?: java.util.UUID.randomUUID().toString()
        val defaultTitle = title ?: "No Title"
        val defaultDesc = description ?: ""
        
        // Smarter Map Coordinates: Only generate map pins for security/bombing/strike events
        val fullText = (defaultTitle + " " + defaultDesc).lowercase()
        val isAttackOrBombing = fullText.contains("bomb") || fullText.contains("strike") || 
                                fullText.contains("attack") || fullText.contains("explosion") || 
                                fullText.contains("raid") || fullText.contains("drone") || fullText.contains("idf")
        
        val generatedMapLocation = if (isAttackOrBombing) {
            when {
                fullText.contains("dahieh") || fullText.contains("southern suburbs") -> "33.8404,35.5031" // Beirut Dahieh
                fullText.contains("tyre") || fullText.contains("sour") -> "33.2705,35.1963"
                fullText.contains("nabatieh") -> "33.3770,35.4838"
                fullText.contains("sidon") || fullText.contains("saida") -> "33.5571,35.3729"
                fullText.contains("baalbek") -> "34.0058,36.2047"
                fullText.contains("bekaa") -> "33.8463,35.9020"
                fullText.contains("hermel") -> "34.3942,36.3842"
                fullText.contains("kfarshouba") || fullText.contains("border") -> "33.2386,35.7369"
                fullText.contains("khiam") -> "33.3323,35.5902"
                fullText.contains("marjayoun") -> "33.3592,35.5898"
                fullText.contains("bint jbeil") -> "33.1214,35.4389"
                fullText.contains("naqoura") -> "33.1189,35.1389"
                fullText.contains("tripoli") -> "34.4346,35.8362" // Unlikely but possible
                else -> "" // Don't plot random places just because they exist
            }
        } else {
            "" // Not a security event, don't show on Map
        }

        return News(
            id = defaultId,
            title = defaultTitle,
            content = defaultDesc,
            location = source?.name ?: "Unknown Source",
            mapLocation = generatedMapLocation,
            datetime = publishedAt ?: "",
            detailedDescription = content ?: "No detailed description available.",
            imageUrl = urlToImage
        )
    }
}

data class Source(
    val id: String?,
    val name: String?
)
