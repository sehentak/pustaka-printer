package com.sehentak.printer.model

import com.google.gson.annotations.SerializedName

data class PrinterDeviceMdl(
    @SerializedName("printer_id")
    var id: String? = null,
    @SerializedName("printer_name")
    var name: String? = null,
    @SerializedName("printer_address")
    var address: String? = null
)