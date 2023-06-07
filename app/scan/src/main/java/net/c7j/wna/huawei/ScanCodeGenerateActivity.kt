package net.c7j.wna.huawei

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import com.google.android.material.button.MaterialButton
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.hmsscankit.WriterException
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import net.c7j.wna.huawei.scan.R
import java.io.File
import java.io.FileOutputStream
import java.util.Objects

/**
 * This example shows how to generate QR, bar or whatever code for the further use
 * To generate QR, bar or whatever code for the further
 * @see generateCode()
 * To save previously generated qr bar or whatever code on disk
 * @see saveCode()
 */
class ScanCodeGenerateActivity : BaseActivity() {

    private var inputContent: EditText? = null
    private var generateType: Spinner? = null
    private var generateMargin: Spinner? = null
    private var generateColor: Spinner? = null
    private var generateBackground: Spinner? = null
    private var barcodeImage: ImageView? = null
    private var barcodeWidth: EditText? = null
    private var barcodeHeight:EditText? = null
    private var content: String? = null
    private var width = 0
    private var height:Int = 0
    private var resultImage: Bitmap? = null
    private var type = 0
    private var margin = 1
    private var color = Color.BLACK
    private var background = Color.WHITE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_generate)
        findViewById<MaterialButton>(R.id.btnGenerateScanCode).setOnClickListener { generateCode() }
        findViewById<MaterialButton>(R.id.btnSaveScanCode).setOnClickListener { saveCode() }

        inputContent = findViewById(R.id.barcode_content)
        generateMargin = findViewById(R.id.generate_margin)
        generateColor = findViewById(R.id.generate_color)
        generateBackground = findViewById(R.id.generate_backgroundcolor)
        barcodeImage = findViewById(R.id.barcode_image)
        barcodeWidth = findViewById(R.id.barcode_width)
        barcodeHeight = findViewById(R.id.barcode_height)
        generateType = findViewById(R.id.generate_type)

        // Set a type of code to generate (like qr code, bar code or other)
        generateType?.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                type = BARCODE_TYPES[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                type = BARCODE_TYPES[0]
            }
        }

        // Set the border width of a barcode (Default value is 1 (if not set)
        generateMargin?.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                margin = position + 1
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                margin = 1
            }
        }

        // Set the barcode color. If you do not call this API, black (Color.BLACK) is used by default
        generateColor?.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                color = COLOR[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                color = COLOR[0]
            }
        }

        // Set the background color of a barcode. If you do not call this API, white (Color.WHITE) is used by default
        generateBackground?.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                background = BACKGROUND[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                background = BACKGROUND[0]
            }
        }
    }

    // This method generates qr, bar or whatever code for the further use
    private fun generateCode() {
        content = ""
        content = inputContent?.text.toString()
        val inputWidth = barcodeWidth?.text.toString()
        val inputHeight: String = barcodeHeight?.text.toString()
        if (inputWidth.isEmpty() || inputHeight.isEmpty()) {
            width = 700
            height = 700
        } else {
            width = inputWidth.toInt()
            height = inputHeight.toInt()
        }
        if ((content as String).isEmpty()) {
            toast("Please input content first!")
            return
        }
        if (color == background) {
            toast("The color and background cannot be the same!")
            return
        }
        try {
            val options = HmsBuildBitmapOption.Creator()
                .setBitmapMargin(margin).setBitmapColor(color).setBitmapBackgroundColor(background).create()
            resultImage = ScanUtil.buildBitmap(content, type, width, height, options)
            barcodeImage?.setImageBitmap(resultImage)
        } catch (e: WriterException) { toast("Parameter Error! ${e.message}") }
    }

    // This method saves previously generated qr bar or whatever code on disk
    private fun saveCode() {
        if (resultImage == null) {
            toast("Please generate barcode first!")
            return
        }
        try {
            val fileName = System.currentTimeMillis().toString() + ".jpg"
            val storePath: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                applicationContext.getExternalFilesDir(null)?.absolutePath ?: ""
            } else {
                Environment.getExternalStorageDirectory().absolutePath
            }
            val appDir = File(storePath)
            if (appDir.exists()) {
                appDir.mkdir()
            }
            val file = File(appDir, fileName)
            val fileOutputStream = FileOutputStream(file)
            val isSuccess =
                resultImage?.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream) ?: false
            fileOutputStream.flush()
            fileOutputStream.close()
            toast(if (isSuccess) "Saved at: $storePath/$fileName" else "Barcode save failed")
        } catch (e: Exception) {
            log(Objects.requireNonNull(e.message)).also { toast( "Error: ${e.message}") }
        }
    }


    companion object {
        val BARCODE_TYPES = intArrayOf(HmsScan.QRCODE_SCAN_TYPE,
            HmsScan.DATAMATRIX_SCAN_TYPE, HmsScan.PDF417_SCAN_TYPE, HmsScan.AZTEC_SCAN_TYPE,
            HmsScan.EAN8_SCAN_TYPE, HmsScan.EAN13_SCAN_TYPE, HmsScan.UPCCODE_A_SCAN_TYPE,
            HmsScan.UPCCODE_E_SCAN_TYPE, HmsScan.CODABAR_SCAN_TYPE, HmsScan.CODE39_SCAN_TYPE,
            HmsScan.CODE93_SCAN_TYPE, HmsScan.CODE128_SCAN_TYPE, HmsScan.ITF14_SCAN_TYPE)

        val COLOR =
            intArrayOf(Color.BLACK, Color.BLUE, Color.GRAY, Color.GREEN, Color.RED, Color.YELLOW)
        val BACKGROUND =
            intArrayOf(Color.WHITE, Color.YELLOW, Color.RED, Color.GREEN, Color.GRAY, Color.BLUE, Color.BLACK)
    }
}