package com.sehentak.printer.model

import com.google.gson.annotations.SerializedName

data class PrintHeaderMdl(
    @SerializedName("header_id")
    var id: String? = null,
    @SerializedName("header_title")
    var title: String? = null,
    @SerializedName("header_subtitle")
    var subtitle: String? = null,
    @SerializedName("header_highlight")
    var highlight: String? = null,
    @SerializedName("header_date")
    var date: String? = null,
    @SerializedName("header_invoice")
    var invoice: String? = null,
    @SerializedName("header_operator")
    var operator: String? = null,
    @SerializedName("header_consumer")
    var consumer: String? = null
)