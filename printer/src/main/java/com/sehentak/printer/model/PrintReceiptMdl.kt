package com.sehentak.printer.model

import com.google.gson.annotations.SerializedName

data class PrintReceiptMdl(
    @SerializedName("receipt_index")
    var index: String? = null,
    @SerializedName("receipt_quantity")
    var quantity: Int = 0,
    @SerializedName("receipt_price")
    var priceBasic: Int = 0,
    @SerializedName("receipt_total")
    var priceTotal: Int = 0
)