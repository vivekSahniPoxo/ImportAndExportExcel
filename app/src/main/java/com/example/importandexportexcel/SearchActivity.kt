package com.example.importandexportexcel


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.*
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.importandexportexcel.data.AllDataItemEntity
import com.example.importandexportexcel.data.GetUtils
import com.example.importandexportexcel.databinding.ActivitySearchBinding
import com.example.importandexportexcel.localDb.DataBaseHandler
import com.example.importandexportexcel.localDb.ExcelViewModel
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SearchActivity : AppCompatActivity() {
    lateinit var binding:ActivitySearchBinding
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


    var handler: Handler? = null
    lateinit var iuhfService: IUHFService
    lateinit var  handlerr: Handler
    private var soundPool: SoundPool? = null
    private var soundId = 0
    private var soundId1 = 0
    val READ_REQUEST_CODE: Int = 42
    lateinit var allDataItem: ArrayList<AllDataItemEntity>
    var isInventoryRunning = false
    var filePath: File?=null
    var rfidNo  = ""
    var isSearchingStart = false

    private val excelViewModel: ExcelViewModel by viewModels()
    companion object {
        private const val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 123
    }

    @SuppressLint("SuspiciousIndentation", "ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        allDataItem = arrayListOf()




       try{
            iuhfService = UHFManager.getUHFService(this)
        } catch (e:Exception){
              e.printStackTrace()
        }

        binding.imExport.setOnClickListener {
            if (binding.tvRFidNo.text.isNotEmpty()) {
                updateFile()
            } else{
                Snackbar.make(binding.root,"No Book Details For Export",Snackbar.LENGTH_SHORT).show()
            }
        }

//        binding.importButton.setOnClickListener {
//            requestStoragePermission()
//        }

        handlerr = Handler()

        binding.imBack.setOnClickListener {
            stopInventoryService()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            iuhfService.closeDev()
            finish()
        }


        binding.tvBtnSearch.setOnClickListener {
            val DataHandler = DataBaseHandler.getDatabase(this)
            val allItemDao = DataHandler.allDataItem()
            val accessno = binding.etSearchFile.text.toString()
            if (binding.tvBtnSearch.text=="Search") {
                hideKeyboard()
                if (binding.etSearchFile.text.isNotEmpty()) {


                    val getBookItem = allItemDao.getDatafromaccessno(accessno)
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
                            rfidNo = item.rfidNo.toString()



//                            tvId.text = item.idd.toString()
//                            tvRFidNo.text = item.rfidNo ?: "NA"
//                            tvAccessNo.text = item.accessNo ?: "NA"
//                            tvAuthor.text = item.author ?: "NA"
//                            tvTitle.text = item.title ?: "NA"
//                            tvPlace.text = item.place ?: "NA"
//                            tvYear.text = item.year ?: "NA"
//                            tvPages.text = item.pages ?: "NA"
//                            tvSouce.text = item.source ?: "NA"
//                            rfidNo = item?.rfidNo.toString()

                            setvalues(item.idd,item.rfidNo,item.accessNo,item.author,item.title,item.volume,item.place,item.year,item.pages,item.source,item.cost,item.billNo,item.registrationDate,item.userName,item.issue,item.rackNo)

                        }

                    }

                }else{

                  Toast.makeText(applicationContext,"No Data Found",Toast.LENGTH_SHORT).show()

                }

                } else {
                    binding.etSearchFile.error = "Input field should not empty"
                }
            } else if(binding.tvBtnSearch.text=="Stop"){
                     stopSearching()
            }
        }




        binding.btnStart.setOnClickListener {

            try {
                if (rfidNo.isEmpty()){
                    Snackbar.make(binding.root,"No data found for search", Snackbar.LENGTH_SHORT).show()
                } else {
                    initSoundPool()

                    iuhfService.inventoryStart()
                    iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                        override fun getInventoryData(var1: SpdInventoryData) {
                            handler?.sendMessage(handler!!.obtainMessage(1, var1))
                            Log.d("as3992_6C", "id is $soundId")
                        }

                        override fun onInventoryStatus(status: Int) {
//                        iuhfService.inventoryStart()
                            threadpooling(status)
                        }
                    })
                }
            } catch (e:Exception){

            }

        }

        binding.btnStop.setOnClickListener {
            isInventoryRunning = false
            iuhfService.inventoryStop()
            //stopInventoryService()
        }

        excelViewModel.getAllDataItem.observe(this, androidx.lifecycle.Observer {item->
            if (item.isNotEmpty()) {
                item.forEach {items->
                    allDataItem.add(AllDataItemEntity(items.idd, items.rfidNo, items.accessNo, items.author, items.title, items.volume, items.place, items.year, items.pages, items.source, items.cost, items.billNo, items.registrationDate, items.userName, items.issue, items.rackNo,""))
                }
            }
        })


        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == 1 && isSearchingStart==true) {
                    if (!TextUtils.isEmpty(rfidNo)) {
                        val spdInventoryData = msg.obj as SpdInventoryData
                        val epc = spdInventoryData.getEpc()
                        Log.d("wkfpkp",epc)
                        if (epc == rfidNo) {
                            // binding.mCardView.setBackgroundColor(ContextCompat.getColor(this@SearchActivity, R.color.green2))
                            val rssi = spdInventoryData.getRssi().toInt()
                            Log.d("rssi",rssi.toString())
                            val i = -60
                            val j = -40
                            if (rssi > i) {
                                if (rssi > j) {
                                    Log.d("rssiSound1",rssi.toString())
                                    soundPool!!.play(soundId1, 1f, 1f, 0, 0, 3f)
                                } else {
                                    Log.d("rssiSound2",rssi.toString())
                                    soundPool!!.play(soundId1, 0.6f, 0.6f, 0, 0, 2f)
                                }
                            } else {
                                Log.d("rssiSound3",rssi.toString())
                                soundPool!!.play(soundId1, 0.3f, 0.3f, 0, 0, 1f)
                            }
                        } else{
//                        binding.gifImage.isVisible = true
//                        binding.mCardView.isVisible = false
                        }
                    }
                }

            }
        }

//        run()

    }

    fun threadpooling(p0:Int) {
        // Create a thread pool with 4 threads
        val threadPool: ExecutorService = Executors.newFixedThreadPool(4)

        // Define a Runnable with your code
        val runnable = kotlinx.coroutines.Runnable {
          //  Looper.prepare()
            if (p0 == 65277) {
                // Log.d("p0", p0.toString())
                iuhfService.closeDev()
                SystemClock.sleep(100)
                startSearching()
            } else {
                iuhfService.inventoryStart()
            }
          //  Looper.loop()
        }

        // Submit the Runnable to the thread pool
        threadPool.submit(runnable)

        // Shutdown the thread pool when done
        threadPool.shutdown()
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

//    private fun requestStoragePermission() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//
//        // Set the MIME type to Excel files
//        intent.type = "application/vnd.ms-excel"
//
//        startActivityForResult(intent, READ_REQUEST_CODE)
//    }


//    private fun requestStoragePermission() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        intent.type = "*/*"  // You can specify the MIME type of files you want to access
//
//        startActivityForResult(intent, READ_REQUEST_CODE)
//    }








    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_BUTTON_R2) {
            if (rfidNo.isNotEmpty()) {
                if (isSearchingStart == false) {
                    startSearching()
                    iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                        override fun getInventoryData(var1: SpdInventoryData) {
                            handler?.sendMessage(handler!!.obtainMessage(1, var1))
                            Log.d("as3992_6C", "id is $soundId")
                        }

                        override fun onInventoryStatus(status: Int) {
                           // runOnUiThread(kotlinx.coroutines.Runnable {
//                                iuhfService.inventoryStart()
                            //})





                        }
                    })

                } else {
                    stopSearching()
                }
            } else{
                Snackbar.make(binding.root,"No Data for search", Snackbar.LENGTH_SHORT).show()
            }

            return true
        }
        else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isSearchingStart = true
        initSoundPool()
        runOnUiThread(kotlinx.coroutines.Runnable {
            binding.gifImage.isVisible = true
            binding.imBook.isVisible = false
            binding.tvBtnSearch.text = "Stop"
            binding.tvBtnSearch.setBackgroundColor(ContextCompat.getColor(this,R.color.red))
        })

        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30
            iuhfService.inventoryStart()

        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }


    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        try {
            soundPool!!.release()
            isSearchingStart = false


        } catch (e:Exception){
            e.printStackTrace()
        }finally {
            iuhfService.inventoryStop()
            iuhfService.closeDev()
        }


        runOnUiThread(kotlinx.coroutines.Runnable {
            binding.gifImage.isVisible = false
            binding.imBook.isVisible = true

            binding.tvBtnSearch.text = "Search"

            binding.tvBtnSearch.setBackgroundColor(ContextCompat.getColor(this,R.color.btn_color))
        })


    }



    private fun stopInventoryService() {
        if (isInventoryRunning) {
            // Stop inventory service
            iuhfService.inventoryStop()
            isInventoryRunning = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // stopInventoryService()


        // Create an intent for the target activity
        val intent = Intent(this, MainActivity::class.java)

        // Add flags to clear the activity stack and start a new task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        // Start the target activity
        startActivity(intent)
        // iuhfService.inventoryStop()
        // Finish the current activity
        iuhfService.closeDev()
        finish()
    }




    fun initSoundPool() {
        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        soundId = soundPool!!.load("/system/media/audio/ui/VideoRecord.ogg", 0)
        Log.w("as3992_6C", "id is $soundId")
        soundId1 = soundPool!!.load(this, R.raw.scankey, 0)

    }

    fun initData() {
        // Play the sound here
        initSoundPool()

    }






    override fun onPause() {
        super.onPause()
        if (isSearchingStart==true){
            stopSearching()
        }
        iuhfService.closeDev()
    }






/*    @SuppressLint("HandlerLeak")
    private val : Handler = object : Handler() {
        @SuppressLint("SetJavaScriptEnabled")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1 && isSearchingStart==true) {
                if (!TextUtils.isEmpty(rfidNo)) {
                    val spdInventoryData = msg.obj as SpdInventoryData
                    val epc = spdInventoryData.getEpc()
                    Log.d("wkfpkp",epc)
                    if (epc == rfidNo) {
                        // binding.mCardView.setBackgroundColor(ContextCompat.getColor(this@SearchActivity, R.color.green2))
                        val rssi = spdInventoryData.getRssi().toInt()
                        Log.d("rssi",rssi.toString())
                        val i = -60
                        val j = -40
                        if (rssi > i) {
                            if (rssi > j) {
                                Log.d("rssiSound1",rssi.toString())
                                soundPool!!.play(soundId1, 1f, 1f, 0, 0, 3f)
                            } else {
                                Log.d("rssiSound2",rssi.toString())
                                soundPool!!.play(soundId1, 0.6f, 0.6f, 0, 0, 2f)
                            }
                        } else {
                            Log.d("rssiSound3",rssi.toString())
                            soundPool!!.play(soundId1, 0.3f, 0.3f, 0, 0, 1f)
                        }
                    } else{
//                        binding.gifImage.isVisible = true
//                        binding.mCardView.isVisible = false
                    }
                }
            }
        }
    }*/



    override fun onStop() {
        super.onStop()
        Log.w("stop", "im stopping")
        try {
            soundPool!!.release()
            iuhfService.inventoryStop()
            // super.onStop()
        } catch (e:Exception){
            Log.d("eee",e.toString())
        }
    }




    fun Activity.hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }

    fun updateFile() {

        filePath = File(
            Environment.getExternalStorageDirectory().toString() + "/" + "SearchedBook" + ".xls"
        )
        try {
            if (!filePath?.exists()!!) {
                // Create a new workbook and sheet if the file doesn't exist
                val hssfWorkbook = HSSFWorkbook()
                val hssfSheet = hssfWorkbook.createSheet("MySheet")

                // Create the header row
                val headerRow = hssfSheet.createRow(0)
                headerRow.createCell(0).setCellValue("Timestamp")
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

                filePath!!.createNewFile()
                val fileOutputStream = FileOutputStream(filePath)
                hssfWorkbook.write(fileOutputStream)

                Toast.makeText(this@SearchActivity, "File Created", Toast.LENGTH_SHORT).show()
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

                Toast.makeText(this@SearchActivity, "File Updated", Toast.LENGTH_SHORT).show()
                fileOutputStream.close()

            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("exception", e.toString())
        }
    }



}