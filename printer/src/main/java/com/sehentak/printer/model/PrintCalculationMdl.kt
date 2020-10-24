package com.sehentak.printer.model

import com.google.gson.annotations.SerializedName

data class PrintCalculationMdl(
    @SerializedName("calculation_basic")
    var priceBasic: Long = 0,
    @SerializedName("calculation_discount")
    var priceDiscount: Long = 0,
    @SerializedName("calculation_tax")
    var priceTax: Long = 0,
    @SerializedName("calculation_tax_percent")
    var priceTaxPercent: Int = 0,
    @SerializedName("calculation_service")
    var priceService: Long = 0,
    @SerializedName("calculation_service_percent")
    var priceServicePercent: Long = 0,
    @SerializedName("calculation_final")
    var priceFinal: Long = 0
)