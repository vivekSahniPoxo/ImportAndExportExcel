package com.example.importandexportexcel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.example.importandexportexcel.data.AllDataItemEntity
import com.example.importandexportexcel.data.GetUtils
import com.example.importandexportexcel.databinding.ActivityIdentifyBinding
import com.example.importandexportexcel.localDb.DataBaseHandler
import com.example.importandexportexcel.localDb.ExcelViewModel
import com.google.android.material.snackbar.Snackbar

import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.utils.ErrorStatus
import com.speedata.libuhf.utils.StringUtils
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.xmlbeans.impl.xb.xsdschema.NamedGroup.All


import java.io.*
import java.time.LocalDateTime

class IdentifyActivity : AppCompatActivity() {

    var o_idd: Long=0
    var o_rfidNo            :String=""
    var o_accessNo          :String=""
    var o_author            :String=""
    var o_title             :String=""
    var o_volume            :String=""
    var o_place             :String=""
    var o_year              :String=""
    var o_pages             :String=""
    var o_source            :String=""
    var o_cost              :String=""
    var o_billNo            :String=""
    var o_registrationDate  :String=""
    var o_userName          :String=""
    var o_issue             :String=""
    var o_rackNo            :String=""

    lateinit var binding:ActivityIdentifyBinding
    lateinit var  handler: Handler
    lateinit var iuhfService: IUHFService
    var isSearchingStart = false
    var isInventoryRunning = false
    val READ_REQUEST_CODE: Int = 42
    lateinit var allDataItem: ArrayList<AllDataItemEntity>
    var filePath: File?=null
    //private val sharedViewModel: SharedViewModel by viewModels()
    companion object {
        private const val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 123
    }
    private val excelViewModel: ExcelViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityIdentifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        allDataItem = arrayListOf()
        handler = Handler()
        iuhfService  = UHFManager.getUHFService(this)



        excelViewModel.getAllDataItem.observe(this, Observer {item->
            if (item.isNotEmpty()) {
                item.forEach {items->
                    allDataItem.add(AllDataItemEntity(items.idd, items.rfidNo, items.accessNo, items.author, items.title, items.volume, items.place, items.year, items.pages, items.source, items.cost, items.billNo, items.registrationDate, items.userName, items.issue, items.rackNo,""))
                }
            }


        })




        binding.importButton.setOnClickListener {
           // requestStoragePermission()

        }

        binding.imExport.setOnClickListener {
            if (binding.tvRFidNo.text.isNotEmpty()) {
                updateFile()
            } else{
                Snackbar.make(binding.root,"No Book Details For Export",Snackbar.LENGTH_SHORT).show()
            }
        }

        try{
            iuhfService = UHFManager.getUHFService(this)
            startSearching()
        } catch (e:Exception){

        }
        binding.imBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        binding.button.setOnClickListener {
            val DataHandler = DataBaseHandler.getDatabase(this)
            val allItemDao = DataHandler.allDataItem()
//            if (!isInventoryRunning) {
//                startSearching()
//

                iuhfService.setOnReadListener { var1 ->
                    //iuhfService.inventoryStart()
                    val stringBuilder = StringBuilder()
                    val epcData = var1.epcData
                    val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
                    if (!TextUtils.isEmpty(hexString)) {
                        stringBuilder.append("EPC：").append(hexString).append("\n")
                    }
                    if (var1.status == 0) {
                        val readData = var1.readData
                        val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
                        stringBuilder.append("ReadData:").append(readHexString).append("\n")
                        Toast.makeText(this, readHexString, Toast.LENGTH_SHORT).show()
                        val getBookItem = allItemDao.getBookDataByRfidNuber(readHexString)
                        if(getBookItem!=null){
                            getBookItem.let { item ->
                                binding.apply {

                                    binding.tvId.text = item.idd.toString()
                                    binding.tvRFidNo.text = item.rfidNo.toString()
                                    binding.tvAccessNo.text = item.accessNo.toDoubleOrNull()?.toInt().toString()
                                    binding.tvAuthor.text = item.author.toString()
                                    binding.tvTitle.text = item.title.toString()
                                    binding.tvPlace.text = item.place.toString()
                                    binding.tvYear.text = item.year.toDoubleOrNull()?.toInt().toString()
                                    binding.tvSouce.text = item.source.toDoubleOrNull()?.toInt().toString()
                                    binding.tvPages.text = item.pages.toDoubleOrNull()?.toInt().toString()




                                    setvalues(item.idd,item.rfidNo,item.accessNo,item.author,item.title,item.volume,item.place,item.year,item.pages,item.source,item.cost,item.billNo,item.registrationDate,item.userName,item.issue,item.rackNo)

                                }

                            }
//                        val allDataMap: Map<String, AllDataItemEntity> = allDataItem.associateBy { it.rfidNo }
//
//                        val desiredRfidNo = readHexString
//
//                        val foundItem: AllDataItemEntity? = allDataMap[desiredRfidNo]
//
//                        if (foundItem != null) {
//                            binding.apply {
//                                tvId.text = foundItem.idd.toString()
//                                tvRFidNo.text = foundItem.rfidNo ?: "NA"
//                                tvAccessNo.text = foundItem.accessNo?: "NA"
//                                tvAuthor.text = foundItem.author?: "NA"
//                                tvTitle.text =  foundItem.title?: "NA"
//                                tvPlace.text = foundItem.place?:"NA"
//                                tvYear.text = foundItem.year?:"NA"
//                                tvPages.text = foundItem.pages?:"NA"
//                                tvSouce.text = foundItem.source?:"NA"
//
//
//                            }
                        } else {
                            // Item not found
                            Toast.makeText(this,"Item with RfidNo $readHexString not found.",Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        stringBuilder.append(this.resources.getString(R.string.read_fail))
                            .append(":").append(this, ErrorStatus.getErrorStatus(this,var1.status)
                            ).append("\n")
                    }
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder))

                }
                val readArea = iuhfService.readArea(1, 2, 6, "00000000")
                if (readArea != 0) {
                    val err: String = this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(this,
                        readArea
                    ) + "\n"
                    handler.sendMessage(handler.obtainMessage(1, err))

                }



//            } else{
//               // stopSearching()
//            }
        }



    }





    override fun onBackPressed() {
        super.onBackPressed()

        // Create an intent for the target activity
        val intent = Intent(this, MainActivity::class.java)

        // Add flags to clear the activity stack and start a new task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        // Start the target activity
        startActivity(intent)
        iuhfService.closeDev()
        // Finish the current activity
        finish()


    }


    override fun onPause() {
        super.onPause()
        if (isInventoryRunning==true){
            stopSearching()

        }

        iuhfService.closeDev()
    }

    override fun onStop() {
        super.onStop()
        iuhfService.closeDev()
    }





    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
    }


    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true

        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 15


        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }

       // iuhfService.inventoryStart()
    }


//    private fun requestStoragePermission() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        intent.type = "*/*"  // You can specify the MIME type of files you want to access
//
//        startActivityForResult(intent, READ_REQUEST_CODE)
//    }


    private fun requestStoragePermission() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // Set the MIME type to Excel files
        intent.type = "application/vnd.ms-excel"

        startActivityForResult(intent, READ_REQUEST_CODE)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let { uri ->
                // Use the content URI (uri) to access the selected file
                // Perform read operations using a content resolver
                val contentResolver = contentResolver
                val inputStream: InputStream? = contentResolver.openInputStream(uri)

                // Read the file using the InputStream


                Log.d("PickedFile", "Picked file URI: $uri")
                Log.d("PickedFile", "Picked file path: ${uri.path}")

                // You may also want to get additional information about the file, such as its name
                val fileName = getFileName(uri)
                Log.d("PickedFile", "Picked file name: $fileName")

                // Read the file using the InputStream
                val pickedFileData = inputStream?.let { importFromExcel(uri) }

                pickedFileData?.let {
                    if(it!=null&&it.size>1){
                        it.drop(0)
                    for (row in it) {
                        if (row.size < 9) {
                            val id = row[0]?:"NA"
                            val rfidNo = row[1]?:"NA"
                            val accessNo = row[2]?:"NA"
                            val author = row[3]?:"NA"
                            val title = row[4]?:"NA"
                            val volume = row[5]?:"NA"
                            val place = row[6]?:"NA"
                            val year = row[7]?:"NA"
                            val pages = row[8]?:"NA"
                            val source = row[9]?:"NA"
                            val cost = row[10]?:"NA"
                            val billNo = row[11]?:"NA"
                            val registrationDate = row[12]?:"NA"
                            val userName = row[13]?:"NA"
                            val issue = row[14]?:"NA"
                            val rackNo = row[15]?:"NA"
                           // Log.d("Cost",cost)



                            val originalID: String = accessNo.toString().substring(0,4)

                           // Log.d("PickedFile", "Rfid No = $id, Name = $rfidNo")
                            allDataItem.add(AllDataItemEntity(0, rfidNo, accessNo, author, title, volume, place, year, pages, source, cost, billNo, registrationDate, userName, issue, rackNo,""))
                            //itemList.add(GetItemFromExcel(rfidNo,name))

                        } else{
                            Log.e("IdentifyActivity", "Row doesn't have enough elements")
                        }
                    }
                        }else
                    {

                    }
                }



                // Initialize the adapter with the data



                inputStream?.close()
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "Unknown"
    }

    private fun importFromExcel(fileUri: Uri): List<List<String>> {
        val inputStream: InputStream = contentResolver.openInputStream(fileUri)
            ?: throw IOException("Unable to open input stream for Uri: $fileUri")

        try {
            val workbook: Workbook = if (fileUri.path?.endsWith(".xls") == true) {
                HSSFWorkbook(inputStream) // For Excel 2003 (.xls) format
            } else {
                XSSFWorkbook(inputStream) // For Excel 2007 and later (.xlsx) format
            }

            val sheet: Sheet = workbook.getSheetAt(0)

            val data = mutableListOf<List<String>>()

            for (rowIndex in 0 until sheet.physicalNumberOfRows) {
                val row: Row = sheet.getRow(rowIndex) ?: continue
                val rowData = mutableListOf<String>()

                for (cellIndex in 0 until row.physicalNumberOfCells) {
                    val cell = row.getCell(cellIndex) ?: continue

                    when (cell.cellType) {
//                        CellType.STRING -> rowData.add(cell.stringCellValue)
//                        CellType.NUMERIC -> rowData.add(cell.numericCellValue.toString())
//                        CellType.BOOLEAN -> rowData.add(cell.booleanCellValue.toString())
                        else -> rowData.add("")
                    }
                }

                data.add(rowData)
            }

            return data
        } catch (e: Exception) {
            // Log the exception for debugging purposes
            Log.e("ImportFromExcel", "Error importing data from Excel", e)
            throw IOException("Error importing data from Excel", e)
        } finally {
            inputStream.close()
        }
    }






    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val DataHandler = DataBaseHandler.getDatabase(this)
        val allItemDao = DataHandler.allDataItem()
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
//            if (!isInventoryRunning) {
//                startSearching()

            iuhfService.setOnReadListener { var1 ->
                val stringBuilder = StringBuilder()
                val epcData = var1.epcData
                val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
                if (!TextUtils.isEmpty(hexString)) {
                    stringBuilder.append("EPC：").append(hexString).append("\n")
                } else if (var1.status == 0) {
                    val readData = var1.readData
                    val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
                    stringBuilder.append("ReadData:").append(readHexString).append("\n")
                    Toast.makeText(this, readHexString, Toast.LENGTH_SHORT).show()
                    val getBookItem = allItemDao.getBookDataByRfidNuber(readHexString)
                    if(getBookItem!=null){
                        getBookItem.let { item ->
                            binding.apply {

                                binding.tvId.text = item.idd.toString()
                                binding.tvRFidNo.text = item.rfidNo.toString()
                                binding.tvAccessNo.text = item.accessNo.toDoubleOrNull()?.toInt().toString()
                                binding.tvAuthor.text = item.author.toString()
                                binding.tvTitle.text = item.title.toString()
                                binding.tvPlace.text = item.place.toString()
                                binding.tvYear.text = item.year.toDoubleOrNull()?.toInt().toString()
                                binding.tvSouce.text = item.source.toDoubleOrNull()?.toInt().toString()
                                binding.tvPages.text = item.pages.toDoubleOrNull()?.toInt().toString()


//                                tvId.text = item.idd.toString()
//                                tvRFidNo.text = item.rfidNo ?: "NA"
//                                tvAccessNo.text = item.accessNo.toDoubleOrNull()?.toInt().toString() ?: "NA"
//                                tvAuthor.text = item.author.toDoubleOrNull()?.toInt().toString() ?: "NA"
//                                tvTitle.text = item.title ?: "NA"
//                                tvPlace.text = item.place ?: "NA"
//                                tvYear.text = item.year.toDoubleOrNull()?.toInt().toString() ?: "NA"
//                                tvPages.text = item.pages.toDoubleOrNull()?.toInt().toString() ?: "NA"
//                                tvSouce.text = item.source ?: "NA"


                                setvalues(item.idd,item.rfidNo,item.accessNo,item.author,item.title,item.volume,item.place,item.year,item.pages,item.source,item.cost,item.billNo,item.registrationDate,item.userName,item.issue,item.rackNo)

                            }

                        }
//                    val allDataMap: Map<String, AllDataItemEntity> = allDataItem.associateBy { it.rfidNo }
//
//                    val desiredRfidNo = readHexString
//
//                    val foundItem: AllDataItemEntity? = allDataMap[desiredRfidNo]
//
//                    if (foundItem != null) {
//                        binding.apply {
//                            tvId.text = foundItem.idd.toString()
//                            tvRFidNo.text = foundItem.rfidNo
//                            tvAccessNo.text  = foundItem.accessNo.toString()
//                            tvAuthor.text = foundItem.author
//                            tvTitle.text =  foundItem.title?:"NA"
//                            tvPlace.text = foundItem.place?:"NA"
//                            tvYear.text = foundItem.year?:"NA"
//                            tvPages.text = foundItem.pages?:"NA"
//                            tvSouce.text = foundItem.source?:"NA"
//
//                        }

                    } else {
                        // Item not found
                        Toast.makeText(applicationContext,"Item with RfidNo $readHexString not found.",Toast.LENGTH_SHORT).show()
                    }
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder))
                } else {
                    stringBuilder.append(this.resources.getString(R.string.read_fail))
                        .append(":").append(ErrorStatus.getErrorStatus(this, var1.status))
                        .append("\n")
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder))
                }
            }

            // This line initiates the readArea operation
            val readArea = iuhfService.readArea(1, 2, 6, "00000000")
            if (readArea != 0) {
                val err: String =
                    this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(
                        this,
                        readArea
                    ) + "\n"
                handler.sendMessage(handler.obtainMessage(1, err))
            }

            // Return true to indicate that you have handled the event
            return true
//            } else {
//                stopSearching()
//            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Handle back button press
            // startActivity(Intent(this, MainActivity::class.java))
            finish()
            return true // Return true to indicate that you have handled the event
        }

        // If the event is not handled, call the superclass implementation
        return super.onKeyDown(keyCode, event)
    }

    fun updateFile() {

        filePath = File(
            Environment.getExternalStorageDirectory().toString() + "/" + "IdentifiedBooks" + ".xls"
        )
        try {
            if (!filePath?.exists()!!) {
                // Create a new workbook and sheet if the file doesn't exist
                val hssfWorkbook = HSSFWorkbook()
                val hssfSheet = hssfWorkbook.createSheet("MySheet")

                // Create the header row
                val headerRow = hssfSheet.createRow(0)
                headerRow.createCell(0).setCellValue("Date&Time")
                headerRow.createCell(1).setCellValue("RfidNo")
                headerRow.createCell(2).setCellValue("AccessNo")
                headerRow.createCell(3).setCellValue("author")
                headerRow.createCell(4).setCellValue("Title")
                headerRow.createCell(5).setCellValue("Volume")
                headerRow.createCell(6).setCellValue("Place")
                headerRow.createCell(7).setCellValue("Year")
                headerRow.createCell(8).setCellValue("pages")
                headerRow.createCell(9).setCellValue("Source")
                headerRow.createCell(10).setCellValue("Cost")
                headerRow.createCell(11).setCellValue("BillNo")
                headerRow.createCell(12).setCellValue("RegistrationDate")
                headerRow.createCell(13).setCellValue("UserName")
                headerRow.createCell(14).setCellValue("Issue")
                headerRow.createCell(15).setCellValue("RackNo")

                // Create a data row
                val dataRow = hssfSheet.createRow(1)
                dataRow.createCell(0).setCellValue(GetUtils.getDatetime("gettingdate").toString())
                dataRow.createCell(1).setCellValue(o_rfidNo )
                dataRow.createCell(2).setCellValue(o_accessNo )
                dataRow.createCell(3).setCellValue(o_author      )
                dataRow.createCell(4).setCellValue(o_title       )
                dataRow.createCell(5).setCellValue(o_volume      )
                dataRow.createCell(6).setCellValue(o_place       )
                dataRow.createCell(7).setCellValue(o_year        )
                dataRow.createCell(8).setCellValue(o_pages       )
                dataRow.createCell(9).setCellValue(o_source      )
                dataRow.createCell(10).setCellValue(o_cost       )
                dataRow.createCell(11).setCellValue(o_billNo     )
                dataRow.createCell(12).setCellValue(o_registrationDate)
                dataRow.createCell(13).setCellValue(o_userName   )
                dataRow.createCell(14).setCellValue(o_issue      )
                dataRow.createCell(15).setCellValue(o_rackNo     )

                filePath!!.createNewFile()
                val fileOutputStream = FileOutputStream(filePath)
                hssfWorkbook.write(fileOutputStream)

                Toast.makeText(this@IdentifyActivity, "File Created", Toast.LENGTH_SHORT).show()
                fileOutputStream.flush()
                fileOutputStream.close()

            } else {
                // If the file exists, open it and update the data
                val fileInputStream = FileInputStream(filePath)
                val hssfWorkbook = HSSFWorkbook(fileInputStream)

                val hssfSheet: HSSFSheet = hssfWorkbook.getSheetAt(0)

                // Calculate the next available row number
                var lastRowNum = hssfSheet.lastRowNum + 1

                // Create a new data row
                val dataRow = hssfSheet.createRow(lastRowNum)
                dataRow.createCell(0).setCellValue(GetUtils.getDatetime("gettingdate").toString()         )
                dataRow.createCell(1).setCellValue(o_rfidNo      )
                dataRow.createCell(2).setCellValue(o_accessNo    )
                dataRow.createCell(3).setCellValue(o_author      )
                dataRow.createCell(4).setCellValue(o_title       )
                dataRow.createCell(5).setCellValue(o_volume      )
                dataRow.createCell(6).setCellValue(o_place       )
                dataRow.createCell(7).setCellValue(o_year        )
                dataRow.createCell(8).setCellValue(o_pages       )
                dataRow.createCell(9).setCellValue(o_source      )
                dataRow.createCell(10).setCellValue(o_cost       )
                dataRow.createCell(11).setCellValue(o_billNo     )
                dataRow.createCell(12).setCellValue(o_registrationDate)
                dataRow.createCell(13).setCellValue(o_userName   )
                dataRow.createCell(14).setCellValue(o_issue      )
                dataRow.createCell(15).setCellValue(o_rackNo     )


                fileInputStream.close()
                val fileOutputStream = FileOutputStream(filePath)
                hssfWorkbook.write(fileOutputStream)
//

                Toast.makeText(this@IdentifyActivity, "File Updated", Toast.LENGTH_SHORT).show()
                fileOutputStream.close()

            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("exception", e.toString())
        }
    }


    fun setvalues (oidd         :Long
                   , orfidNo           :String
                   , oaccessNo         :String
                   , oauthor           :String
                   , otitle            :String
                   , ovolume           :String
                   , oplace            :String
                   , oyear             :String
                   , opages            :String
                   , osource           :String
                   , ocost             :String
                   , obillNo           :String
                   , oregistrationDate :String
                   , ouserName         :String
                   , oissue            :String
                   , orackNo          :String)
    {
        o_idd=oidd
        o_rfidNo           =orfidNo
        o_accessNo         =oaccessNo
        o_author           =oauthor
        o_title            =otitle
        o_volume           =ovolume
        o_place            =oplace
        o_year             =oyear
        o_pages            =opages
        o_source           =osource
        o_cost             =ocost
        o_billNo           =obillNo
        o_registrationDate =oregistrationDate
        o_userName         =ouserName
        o_issue            =oissue
        o_rackNo           =orackNo
    }




}