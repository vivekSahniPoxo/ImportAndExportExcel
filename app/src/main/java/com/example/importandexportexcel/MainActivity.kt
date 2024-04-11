package com.example.importandexportexcel


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.importandexportexcel.data.AllDataItemEntity
import com.example.importandexportexcel.databinding.ActivityMainBinding
import com.example.importandexportexcel.inventory.InventoryActivity
import com.example.importandexportexcel.localDb.ExcelViewModel
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*


class MainActivity : AppCompatActivity() {
    private var file: File? = null
    lateinit var workbook: Workbook
    lateinit var binding: ActivityMainBinding
    val READ_REQUEST_CODE: Int = 42
//    private val sharedViewModel: SharedViewModel by viewModels()

    private val PICK_FILE_REQUEST_CODE = 123
    private val REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 1
    private val REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 2
    var filePath: File?=null
    lateinit var dialogTag:Dialog
    private lateinit var adapter: GetItemFromExcelAdapter
    lateinit var allDataItem: ArrayList<AllDataItemEntity>
    private val excelViewModel: ExcelViewModel by viewModels()
    private val STORAGE_PERMISSION_REQUEST_CODE = 100



    companion object {
        private const val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 123
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        //requestStoragePermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // Permission granted, proceed with writing to external storage
            } else {
                // Request the permission
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        } else {
            // For versions below Android 11, handle accordingly
        }

        binding.imImport.setOnClickListener {
            requestStoragePermission()
        }
        allDataItem = arrayListOf<AllDataItemEntity>()






        //  requestReadExternalStoragePermission()

        binding.mIdentiryCard.setOnClickListener {
            val intent = Intent(this,IdentifyActivity::class.java)

                startActivity(intent)


        }

        binding.searchBook .setOnClickListener {
            val intent = Intent(this,SearchActivity::class.java)
            startActivity(intent)
            //updateFile()
        }

        binding.importButton.setOnClickListener {
            //pickFile()
            //requestStoragePermission()

            val intent = Intent(this,IdentifyActivity::class.java)
            startActivity(intent)
        }

        binding.mCardInventory.setOnClickListener {
            //pickFile()
            //requestStoragePermission()

            val intent = Intent(this,InventoryActivity::class.java)
            startActivity(intent)
        }

        init()
    }

    private fun requestReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    private fun requestStoragePermission() {
        // Start the file picker activity
        val mimeTypes = arrayOf(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xls & .xlsx

        )

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
            if (mimeTypes.size > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
        } else {
            var mimeTypesStr = ""
            for (mimeType in mimeTypes) {
                mimeTypesStr += "$mimeType|"
            }
            intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
        }
        startActivityForResult(Intent.createChooser(intent, "ChooseFile"), READ_REQUEST_CODE)
    }



    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            binding.progressBar.isVisible = true
            resultData?.data?.let { uri ->
                excelViewModel.deleteAllItem()
                // Use the content URI (uri) to access the selected file
                // Perform read operations using a content resolver
                val contentResolver = contentResolver
                val inputStream: InputStream? = contentResolver.openInputStream(uri)

                // Read the file using the InputStream



//                val fileName = getFileName(uri)
//                Log.d("PickedFile", "Picked file name: $fileName")
//                val filePath = intent.extras?.getString("excellPath").orEmpty()

                // Read the file using the InputStream
                val pickedFileData = inputStream?.let { importFromExcel(uri) }

                pickedFileData?.let { it ->

                    if(!it.isNullOrEmpty() && it.size>0) {
                        var count=0
                        var indexcount=0;
                        it as ArrayList
                        for (row in it) {
                            indexcount=row.lastIndex
                            if(indexcount<1){
                                Toast.makeText(applicationContext, "Invalid Index count : "+indexcount.toString(), Toast.LENGTH_SHORT).show()
                            }else {

                                var id:String = row[0]
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

                            excelViewModel.insertAllDataItems(AllDataItemEntity(count.toLong(),rfidNo,accessNo.toDoubleOrNull()?.toInt().toString(), author, title, volume, place, year, pages, source, cost, billNo, registrationDate, userName, issue, rackNo,""))
                            count++

                            }
                        }
                        Toast.makeText(applicationContext, "Total Rows found : "+count.toString(), Toast.LENGTH_SHORT).show()

                    }
                    else{
                        Log.e("IdentifyActivity", "Row doesn't have enough elements")
                        Toast.makeText(applicationContext, "No value found", Toast.LENGTH_SHORT).show()

                    }
                }

                inputStream?.close()
            }
            binding.progressBar.isVisible = false
        }
    }


    private fun init() {


        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        );


    }



    @SuppressLint("SuspiciousIndentation")
    private fun importFromExcel(fileUri: Uri, password: String = ""): List<List<String>> {
        val data = mutableListOf<List<String>>()


            try {
                val inputStream = contentResolver.openInputStream(fileUri)

                if (inputStream!=null) {
                    workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)



                    for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                        val row: Row = sheet.getRow(rowIndex) ?: continue
                        val rowData = mutableListOf<String>()

                        for (cellIndex in 0 until 16) {


                            val cell = row.getCell(cellIndex)

                            if (cell == null) {
                                rowData.add("Null")
                            } else {
                                when (cell.cellTypeEnum) {
                                    CellType.STRING -> rowData.add(cell.stringCellValue)
                                    CellType.NUMERIC -> rowData.add(cell.numericCellValue.toString())
                                    CellType.BOOLEAN -> rowData.add(cell.booleanCellValue.toString())
                                    CellType.BLANK -> rowData.add("NA")
                                    CellType.ERROR -> rowData.add("NA")
                                    else -> rowData.add("NA")
                                }
                            }

                        }

                        data.add(rowData)
                    }
                }

            } catch (e: Exception) {
                    // Log the exception for debugging purposes
                Log.e("ImportFromExcel", "Error importing data from Excel", e)
                throw IOException("Error importing data from Excel", e)
            } finally {

            }

        return data
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








    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted
            // Perform operations requiring the permission
            // Example: Launch file picker, read from storage, etc.
        } else {
            // Permission is not granted, request it
            requestStoragePermission2()
        }
    }

    private fun requestStoragePermission2() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, perform necessary operations
                    // Example: Launch file picker, read from storage, etc.
                } else {
                    // Permission denied, handle accordingly
                    // Example: Show a message, disable functionality, etc.
                }
            }
            // Handle other permissions if needed
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        excelViewModel.deleteAllItem()
//    }

    init {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        );
    }
}









