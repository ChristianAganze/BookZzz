package com.example.shared.engine

/**
 * Shared Pricing & Currency Calculation Engine for BookZzz
 */
object PricingEngine {
    // Standard exchange rate 1 USD = ~2850 CDF (customizable per transaction/hotel)
    const val DEFAULT_USD_TO_CDF_RATE = 2850.0

    /**
     * Calculates total booking price in USD
     */
    fun calculateTotalUSD(
        pricePerNightUSD: Double,
        nights: Int,
        includeTaxiTransfer: Boolean = false,
        taxiTransferFeeUSD: Double = 25.0
    ): Double {
        val baseTotal = pricePerNightUSD * nights
        return if (includeTaxiTransfer) baseTotal + taxiTransferFeeUSD else baseTotal
    }

    /**
     * Converts USD amount to CDF with integer rounding
     */
    fun convertUSDToCDF(amountUSD: Double, rate: Double = DEFAULT_USD_TO_CDF_RATE): Long {
        return (amountUSD * rate).toLong()
    }

    /**
     * Formats prices with clear thousand separators
     */
    fun formatUSD(amount: Double): String {
        return "$${"%.2f".format(amount)}"
    }

    fun formatCDF(amountCDF: Long): String {
        return "%,d FC".format(amountCDF).replace(',', ' ')
    }
}
