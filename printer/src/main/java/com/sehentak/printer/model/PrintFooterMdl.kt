package com.sehentak.printer.model

import com.google.gson.annotations.SerializedName

data class PrintFooterMdl(
    @SerializedName("footer_id")
    var id: String? = null,
    @SerializedName("footer_note")
    var note: String? = null

)