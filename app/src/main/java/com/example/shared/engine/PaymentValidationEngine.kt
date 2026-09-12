package com.example.shared.engine

/**
 * Validates Phone Numbers and Transaction IDs for Mobile Money Operators in DRC (Vodacom M-Pesa, Airtel Money, Orange Money)
 */
object PaymentValidationEngine {

    enum class Operator(val displayName: String, val prefixList: List<String>) {
        MPESA("M-Pesa (Vodacom)", listOf("081", "082", "083", "+24381", "+24382", "+24383", "81", "82", "83")),
        AIRTEL_MONEY("Airtel Money", listOf("097", "098", "099", "+24397", "+24398", "+24399", "97", "98", "99")),
        ORANGE_MONEY("Orange Money", listOf("084", "085", "089", "+24384", "+24385", "+24389", "84", "85", "89"))
    }

    /**
     * Identifies the operator based on the phone number prefix
     */
    fun detectOperator(rawPhone: String): Operator? {
        val cleaned = rawPhone.replace(" ", "").replace("-", "")
        return Operator.values().firstOrNull { op ->
            op.prefixList.any { prefix -> cleaned.startsWith(prefix) }
        }
    }

    /**
     * Validates transaction reference codes
     */
    fun isValidTransactionId(txId: String): Boolean {
        val clean = txId.trim()
        return clean.length >= 6 && clean.matches(Regex("^[A-Za-z0-9._-]+$"))
    }
}
