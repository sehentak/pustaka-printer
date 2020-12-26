package com.sehentak.printer.service

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresPermission
import com.sehentak.printer.BuildConfig
import com.sehentak.printer.R
import com.sehentak.printer.base.func.BluetoothService
import com.sehentak.printer.base.func.Command
import com.sehentak.printer.base.func.PrinterCommand
import com.sehentak.printer.model.PrintDataMdl
import org.davidmoten.text.utils.WordWrap
import java.text.NumberFormat
import java.util.*

class PrinterService: Service() {
    private val mTagClass = this::class.java.simpleName
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var instancePrinterService: BluetoothService
    private lateinit var instanceHandler: Handler
    private lateinit var instanceHandler2: Handler

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (BuildConfig.DEBUG) Log.e(mTagClass, "start: $startId")
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mService.stop()
    }

    fun init(bluetoothAddress: String?): BluetoothService {
        return init(applicationContext, bluetoothAddress)
    }

    @RequiresPermission(anyOf = [Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN])
    fun init(context: Context, bluetoothAddress: String?): BluetoothService {
        if (bluetoothAddress != null && !TextUtils.isEmpty(bluetoothAddress)) {
            context.saveAddress(bluetoothAddress)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (mBluetoothAdapter.isEnabled) {
                mService.start()
                mHandler2.postDelayed({
                    val device = mBluetoothAdapter.getRemoteDevice(bluetoothAddress)
                    mService.connect(device)
                }, 500)
            }
        }

        return mService
    }

    @Throws(Exception::class)
    fun sendData(printData: PrintDataMdl) {
        try {
            sendData(applicationContext, printData)
        } catch (e: Exception) {
            throw e
        }
    }

    @Throws(Exception::class)
    fun sendData(context: Context, printData: PrintDataMdl) {
        try {
            val d132 = context.getString(R.string.const_d132)
            val d232 = context.getString(R.string.const_d232)

            var title = printData.header?.title
            if (title != null && !TextUtils.isEmpty(title)) {
                val flagCabang = context.getString(R.string.label_cabang)
                val flagBranch = context.getString(R.string.label_branch)
                val branchFlag: String? = when {
                    title.contains(flagCabang) -> flagCabang
                    title.contains(flagBranch) -> flagBranch
                    else -> null
                }

                if (branchFlag != null && title.contains(branchFlag)) {
                    val titles = title.split(branchFlag)
                    var subTitle = "$branchFlag ${titles[1]}".trim()

                    title = titles[0].trim()
                    title = center(title, 16)

                    subTitle = center(subTitle, 32).toString()

                    context.sendData(title)

                    val align: Byte = 0x00
                    context.sendData(align)
                    context.sendData(subTitle.toByteArray(charset("GBK")))
                    context.sendData("\n")
                } else {
                    title = center(title, 16)
                    context.sendData(title)
                }
            }

            val align: Byte = 0x00
            context.sendData(align)

            var address = printData.header?.subtitle
            if (address != null && !TextUtils.isEmpty(address)) {
                address = center(address, 32)
                context.sendData(address?.toByteArray(charset("GBK")))
                context.sendData(d232.toByteArray(charset("GBK")))
                context.sendData("\n".toByteArray(charset("GBK")))
            }

            var invoice = printData.header?.invoice
            if (invoice != null && !TextUtils.isEmpty(invoice)) {
                val fLength = 32 - invoice.length
                invoice = "${filler(fLength)}$invoice"
                context.sendData(invoice.toByteArray(charset("GBK")))
            }

            var dateTrx = printData.header?.date
            if (dateTrx != null && !TextUtils.isEmpty(dateTrx)) {
                val fLength = 32 - dateTrx.length
                dateTrx = "${filler(fLength)}$dateTrx"
                context.sendData(dateTrx.toByteArray(charset("GBK")))
            }

            var operator = printData.header?.operator
            if (operator != null && !TextUtils.isEmpty(operator)) {
                val fLength = 32 - operator.length
                operator = "${filler(fLength)}$operator"
                context.sendData(operator.toByteArray(charset("GBK")))
            }

            context.sendData("\n")
            context.sendData(align)
            val products = printData.receipts
            if (!products.isNullOrEmpty()) for (item in products) {
                val tPrice = item.priceTotal.toCurrencyFormat()
                val fPrice = 10 - tPrice.length
                val cPrice = "${filler(fPrice)}$tPrice"

                val tQuantity = "x${item.quantity}"
                val fQuantity = 4 - tQuantity.length
                val cQuantity = "$tQuantity${filler(fQuantity)}"

                var tIndex = item.index.toString()
                if (tIndex.length > 15) {
                    tIndex = tIndex.substring(0, 15)
                }
                val fIndex = 18 - tIndex.length
                val cIndex = "$tIndex${filler(fIndex)}"

                val compose = "$cIndex$cQuantity$cPrice"
                context.sendData(compose.toByteArray(charset("GBK")))
            }

            context.sendData(d132.toByteArray(charset("GBK")))
            context.sendData("\n".toByteArray(charset("GBK")))

            val zero: Long = 0
            val tax = printData.calculation?.priceTax ?: 0
            val service = printData.calculation?.priceService ?: 0
            val discount = printData.calculation?.priceDiscount ?: 0

            if (tax == zero && service == zero && discount == zero) {
                val total = printData.calculation?.priceFinal
                if (total != null && !TextUtils.isEmpty(total.toString())) {
                    var label = context.getString(R.string.label_total)
                    val lengthS = 18 - label.length
                    label = "$label${filler(lengthS)}"

                    var price = total.toCurrencyFormat()
                    val lengthT = 10 - price.length
                    price = "${filler(lengthT)}$price"

                    val currency = context.getString(R.string.label_currency)
                    val mSubTotal = "$label  $currency$price"

                    context.sendData(mSubTotal.toByteArray(charset("GBK")))
                    context.sendData(d132.toByteArray(charset("GBK")))
                    context.sendData("\n".toByteArray(charset("GBK")))
                }
            } else {
                val subTotal = printData.calculation?.priceBasic
                if (subTotal != null && !TextUtils.isEmpty(subTotal.toString())) {
                    var label = context.getString(R.string.label_total_sub)
                    val lengthS = 18 - label.length
                    label = "$label${filler(lengthS)}"

                    var price = subTotal.toCurrencyFormat()
                    val lengthT = 10 - price.length
                    price = "${filler(lengthT)}$price"

                    val currency = context.getString(R.string.label_currency)
                    val mSubTotal = "$label  $currency$price"

                    context.sendData(mSubTotal.toByteArray(charset("GBK")))
                    context.sendData(d132.toByteArray(charset("GBK")))
                    context.sendData("\n".toByteArray(charset("GBK")))
                }

            }

            var footer = printData.footer?.note
            if (footer != null && !TextUtils.isEmpty(footer)) {
                context.sendData("\n".toByteArray(charset("GBK")))
                context.sendData(d232.toByteArray(charset("GBK")))

                footer = center(footer, 32)
                context.sendData(footer?.toByteArray(charset("GBK")))
            }
            context.sendData(48)
            context.sendData("\n\n")
        } catch (e: Exception) {
            throw e
        }
    }

    @Throws(Exception::class)
    fun Context.sendData(paperFeed: Int) {
        try {
            sendData(PrinterCommand.POS_Set_PrtAndFeedPaper(paperFeed))
            sendData(Command.GS_V_m_n)
        } catch (e: Exception) {
            throw e
        }
    }

    @Throws(Exception::class)
    fun Context.sendData(byteAlign: Byte) {
        try {
            Command.ESC_Align[2] = byteAlign
            sendData(Command.ESC_Align)
            Command.GS_ExclamationMark[2] = byteAlign
            sendData(Command.GS_ExclamationMark)
        } catch (e: Exception) {
            throw e
        }
    }

    @Throws(Exception::class)
    fun Context.sendData(content: String?) {
        try {
            if (content != null && content.isNotEmpty()) {
                sendData(content, "GBK")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    @Throws(Exception::class)
    fun Context.sendData(content: String?, encoding: String) {
        try {
            if (content != null && content.isNotEmpty()) {
                val data = PrinterCommand.POS_Print_Text("$content\n", encoding, 0, 1, 1, 0)
                sendData(data)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    @Throws(Exception::class)
    fun Context.sendData(data: ByteArray?) {
        try {
            if (data != null && mBluetoothAdapter.isEnabled) {
                if (mService.state == 3) mService.write(data)
                else {
                    init(getAddress())
                    if (BuildConfig.DEBUG) {
                        Log.e(mTagClass, "Device not connected")
                    }

                    if (mService.state == 3) mService.write(data)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun center(text: String, len: Int): String? {
        return if (text.length >= len) {
            var result = ""
            val wrapped = WordWrap.from(text)
                .newLine("\n")
                .maxWidth(len)
                .insertHyphens(true) // true is the default
                .wrap()

            if (wrapped.contains("\n")) for (item in wrapped.split("\n")){
                var itemString = item.trim()
                if (itemString.length < len) {
                    itemString = textCenter(itemString, len) ?: ""
                }
                result = if (result.isEmpty()) itemString
                else "$result\n$itemString"
            }
            result
        } else textCenter(text, len)
    }

    private fun textCenter(text: String, max: Int): String? {
        if (max <= text.length) return text.substring(0, max)
        val before = (max - text.length) / 2
        if (before == 0) return String.format("%-" + max + "s", text)
        val rest = max - before
        return String.format("%" + before + "s%-" + rest + "s", "", text)
    }

    private fun filler(length: Int): String {
        var result = ""
        if (length > 0) for (x in 0 until length) {
            result += " "
        }
        return result
    }

    private fun Double.toCurrencyFormat(): String {
        return this.toLong().toCurrencyFormat()
    }

    private fun Int.toCurrencyFormat(): String {
        return this.toLong().toCurrencyFormat()
    }

    private fun Long.toCurrencyFormat(): String {
        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
        var currency: String = format.format(this.toDouble())
        if (currency.contains("€")) currency = currency.replace("€", "")
        if (currency.contains(",")) currency = currency.split(",")[0]
        return currency.trim()
    }

    @Suppress("DEPRECATION")
    private val mHandler: Handler get() {
        if (!::instanceHandler.isInitialized) {
            instanceHandler = Handler()
        }
        return instanceHandler
    }

    @Suppress("DEPRECATION")
    private val mHandler2: Handler get() {
        if (!::instanceHandler.isInitialized) {
            instanceHandler2 = Handler()
        }
        return instanceHandler2
    }

    private val mService: BluetoothService get() {
        if (!::instancePrinterService.isInitialized) {
            instancePrinterService = BluetoothService(baseContext, mHandler)
        }
        return instancePrinterService
    }

    private fun Context.saveAddress(address: String?) {
        if (address != null) {
            val editor = getSharedPreferences(mTagClass, Context.MODE_PRIVATE).edit()
            editor.putString("address", address)
            editor.apply()
        }
    }

    private fun Context.getAddress(): String? {
        val prefs = getSharedPreferences(mTagClass, Context.MODE_PRIVATE)
        return prefs.getString("address", null)
    }
}