package com.sehentak.printer.model

import com.google.gson.annotations.SerializedName

data class PrintDataMdl(
    @SerializedName("print_address")
    var address: String? = null,
    @SerializedName("print_logo")
    var logo: String? = null,
    @SerializedName("print_header")
    var header: PrintHeaderMdl? = null,
    @SerializedName("print_receipt")
    var receipts: List<PrintReceiptMdl>? = null,
    @SerializedName("print_calculation")
    var calculation: PrintCalculationMdl? = null,
    @SerializedName("print_footer")
    var footer: PrintFooterMdl? = null,
    @SerializedName("print_text")
    var text: List<String> = mutableListOf()
)