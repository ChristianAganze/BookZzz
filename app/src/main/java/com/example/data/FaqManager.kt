package com.example.data

import android.content.Context
import org.json.JSONObject

data class FaqItem(
    val question: String,
    val answer: String
)

object FaqManager {
    private var cachedFaqs: Map<String, List<FaqItem>>? = null

    fun getFaqs(context: Context, language: String): List<FaqItem> {
        val allFaqs = cachedFaqs ?: loadFaqsFromJson(context).also { cachedFaqs = it }
        
        val normalizedLang = when {
            language.contains("Eng", ignoreCase = true) -> "English"
            language.contains("Swa", ignoreCase = true) -> "Swahili"
            else -> "Français"
        }

        return allFaqs[normalizedLang] 
            ?: allFaqs["Français"] 
            ?: getFallbackFaqs(normalizedLang)
    }

    private fun loadFaqsFromJson(context: Context): Map<String, List<FaqItem>> {
        val result = mutableMapOf<String, List<FaqItem>>()
        try {
            val jsonString = context.assets.open("faq_data.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            
            val keys = root.keys()
            while (keys.hasNext()) {
                val langKey = keys.next()
                val jsonArray = root.getJSONArray(langKey)
                val list = mutableListOf<FaqItem>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    list.add(
                        FaqItem(
                            question = item.optString("question"),
                            answer = item.optString("answer")
                        )
                    )
                }
                result[langKey] = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun getFallbackFaqs(language: String): List<FaqItem> {
        return when (language) {
            "English" -> listOf(
                FaqItem("How to book a room?", "Select a hotel from the home screen, choose your room, select your dates, provide your Mobile Money transaction reference, then confirm."),
                FaqItem("Which Mobile Money operators are accepted?", "We accept M-Pesa, Airtel Money, and Orange Money."),
                FaqItem("How does instant validation work?", "Our AI assistant verifies your payment reference or screenshot in seconds."),
                FaqItem("How to request a taxi?", "Once your booking is confirmed, click the 'Request Taxi' button on your receipt."),
                FaqItem("How to contact support?", "Use the built-in AI assistant or call reception.")
            )
            "Swahili" -> listOf(
                FaqItem("Jinsi ya kuhifadhi chumba?", "Chagua hoteli, chagua chumba, weka tarehe, weka kumbukumbu ya malipo ya Mobile Money, kisha thibitisha."),
                FaqItem("Ni mitandao gani inakubaliwa?", "Tunakubali M-Pesa, Airtel Money na Orange Money."),
                FaqItem("Uthibitishaji unafanya kazi vipi?", "AI yetu inakagua kumbukumbu au picha ya malipo kwa sekunde chache."),
                FaqItem("Jinsi ya kuomba teksi?", "Baada ya uthibitisho, bonyeza 'Omba Teksi' kwenye risiti yako."),
                FaqItem("Jinsi ya kupata msaada?", "Tumia msaidizi wa AI au piga simu mapokezi.")
            )
            else -> listOf(
                FaqItem("Comment réserver une chambre ?", "Sélectionnez un hôtel depuis l'accueil, choisissez votre catégorie de chambre préférée, entrez vos dates, copiez la référence de votre transaction Mobile Money, puis validez."),
                FaqItem("Quels opérateurs Mob Money acceptez-vous ?", "Nous acceptons M-Pesa, Airtel Money et Orange Money."),
                FaqItem("Comment fonctionne la validation instantanée ?", "Notre assistant de preuve IA analyse la référence ou la capture d'écran du paiement Mobile Money et approuve automatiquement la réservation en quelques secondes !"),
                FaqItem("Comment demander le service de taxi ?", "Une fois votre réservation validée, un bouton 'Demander Taxi' apparaît sur votre reçu."),
                FaqItem("Comment contacter le service clientèle ?", "Vous pouvez soit utiliser notre assistant IA interactif intégré, soit contacter directement notre réception.")
            )
        }
    }
}
