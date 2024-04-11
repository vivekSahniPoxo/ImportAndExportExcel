package com.example.importandexportexcel.inventory

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.importandexportexcel.Cons
import com.example.importandexportexcel.MainActivity
import com.example.importandexportexcel.R
import com.example.importandexportexcel.data.AllDataItemEntity
import com.example.importandexportexcel.databinding.ActivityInentoryBinding
import com.example.importandexportexcel.localDb.DataBaseHandler
import com.example.importandexportexcel.localDb.ExcelViewModel
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import kotlinx.android.synthetic.main.activity_inentory.*
import kotlinx.coroutines.*
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InventoryActivity : AppCompatActivity() {
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var binding:ActivityInentoryBinding
    private var soundId = 0
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0
    var rfidNo = ""
    lateinit var temList:ArrayList<String>
   // lateinit var rfidNoList:ArrayList<Rfidnumber>
    private val handlerthread = Handler(Looper.getMainLooper())
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    lateinit var progressDialog: ProgressDialog
    lateinit var inventoryAdapter: InventoryAdapter
    lateinit var tempList:ArrayList<String>
    lateinit var mList:ArrayList<AllDataItemEntity>
    val temp = arrayListOf<String>()
    private var scanningJob: Job? = null
     var suggestion = arrayListOf<String>()

    var filePath: File?=null
    lateinit var allDataItem: ArrayList<AllDataItemEntity>

    lateinit var filterItem: ArrayList<AllDataItemEntity>
    val excelViewModel: ExcelViewModel by viewModels()
    var found =  arrayListOf<AllDataItemEntity>()
    var notFound = arrayListOf<AllDataItemEntity>()
    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityInentoryBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mList= arrayListOf()
        filterItem =  arrayListOf()
        allDataItem = arrayListOf()
        suggestion =  arrayListOf()
        //rfidNoList = arrayListOf()
        temList = arrayListOf()
        inventoryAdapter = InventoryAdapter(mList)
        tempList = arrayListOf()
        progressDialog = ProgressDialog(this)
        try {
            iuhfService = UHFManager.getUHFService(this)
        } catch (e:Exception){
            e.printStackTrace()
        }



        binding.etUserId.clearFocus()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        suggestion.add("All")
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            Toast.makeText(applicationContext, selectedItem, Toast.LENGTH_SHORT).show()
            if (selectedItem.isNotEmpty()){
                filterItem.clear()
            }
            if (selectedItem == "All") {
                allDataItem.clear()
                inventoryData()
            } else {
                val getBookItems = allDataItem.filter { it.title == selectedItem }

                if (getBookItems.isNotEmpty()) {
                    for (item in getBookItems) {
                        Log.d("itm", item.title)
                        filterItem.add(
                            AllDataItemEntity(
                                item.idd,
                                item.rfidNo,
                                item.accessNo,
                                item.author,
                                item.title,
                                item.volume,
                                item.place,
                                item.year,
                                item.pages,
                                item.source,
                                item.cost,
                                item.billNo,
                                item.registrationDate,
                                item.userName,
                                item.issue,
                                item.rackNo,
                                item.status
                            )
                        )
                    }
                } else {
                    // Handle case where no items were found with the specified title
                    Log.d("itm", "No items found with title: $selectedItem")
                }
                inventoryAdapter = InventoryAdapter(filterItem)
                binding.recyclerview.adapter = inventoryAdapter
                inventoryAdapter.notifyDataSetChanged()
                binding.tvTotalCount.text = filterItem.size.toString()
            }
        }


        binding.llFoundHeading.setOnClickListener {
            if (found.isNotEmpty()){
                found.clear()
            }

            allDataItem.forEach { item ->
                if (item.status == Cons.FOUND) {
                    found.add(
                        AllDataItemEntity(
                            item.idd,
                            item.rfidNo,
                            item.accessNo,
                            item.author,
                            item.title,
                            item.volume,
                            item.place,
                            item.year,
                            item.pages,
                            item.source,
                            item.cost,
                            item.billNo,
                            item.registrationDate,
                            item.userName,
                            item.issue,
                            item.rackNo,
                            Cons.FOUND
                        )
                    )


                }

            }


            binding.llFoundHeading.setBackgroundResource(R.drawable.txt_background)
            binding.llNotHeading.setBackgroundResource(R.drawable.gray_back)
            binding.llTotalHeading.setBackgroundResource(R.drawable.gray_back)
            inventoryAdapter = InventoryAdapter(found)
            binding.recyclerview.adapter = inventoryAdapter
            inventoryAdapter.notifyDataSetChanged()

        }

        binding.llNotHeading.setOnClickListener {
            if (notFound.isNotEmpty()){
                notFound.clear()
            }
            allDataItem.forEach { item ->
                if (item.status == Cons.NOTFOUND) {
                    notFound.add(
                        AllDataItemEntity(
                            item.idd,
                            item.rfidNo,
                            item.accessNo,
                            item.author,
                            item.title,
                            item.volume,
                            item.place,
                            item.year,
                            item.pages,
                            item.source,
                            item.cost,
                            item.billNo,
                            item.registrationDate,
                            item.userName,
                            item.issue,
                            item.rackNo,
                            Cons.NOTFOUND
                        )
                    )

                }

                inventoryAdapter.notifyDataSetChanged()
                inventoryAdapter = InventoryAdapter(notFound)
                binding.recyclerview.adapter = inventoryAdapter
                inventoryAdapter.notifyDataSetChanged()
            }

            binding.llNotHeading.setBackgroundResource(R.drawable.txt_background)
            binding.llFoundHeading.setBackgroundResource(R.drawable.gray_back)
            binding.llTotalHeading.setBackgroundResource(R.drawable.gray_back)


        }


        binding.llTotalHeading.setOnClickListener {
            val textColor = ContextCompat.getColor(this, R.color.white)
            binding.tvTotal.setTextColor(textColor)
            binding.llTotalHeading.setBackgroundResource(R.drawable.txt_background)
            val notFoundColor = ContextCompat.getColor(this, R.color.white)
            binding.notFoundHeading.setTextColor(notFoundColor)
            binding.llNotHeading.setBackgroundResource(R.drawable.gray_back)
            val totalColor = ContextCompat.getColor(this, R.color.white)
            binding.FoundHeading.setTextColor(totalColor)
            binding.llFoundHeading.setBackgroundResource(R.drawable.gray_back)
            inventoryData()
        }









        binding.imBack.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }


            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }


        binding.SubmitButton.setOnClickListener {
            try {
                if (binding.etUserId.text?.isEmpty() == true){
                    binding.etUserId.error = "Name field should not be empty"
                } else {
                    updateFile()

                }
            } catch (e:Exception){

            }

        }

        binding.NewButton.setOnClickListener {
            inventoryAdapter.clearAllData()
            if (isInventoryRunning==true) {
                stopSearching()
            }
            //rfidNoList.clear()
            temList.clear()
            inventoryAdapter.refreshAdapter()
           // binding.Total.text = ""
            binding.tvTotalCount.text = ""
//            binding.Found.text=""
//            binding.notFound.text=""
            binding.tvFountCount.text=""
            binding.tvNotFoundCount.text=""


        }

        binding.BackButton.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            allDataItem.clear()
            inventoryData()
//            val intent = Intent(this, MainActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//            //iuhfService.closeDev()
//            finish()
        }



        binding.btnStart.setOnClickListener {
            //val selectedItem = binding.spType.selectedItemPosition

            if (allDataItem.isEmpty() ) {
                Snackbar.make(binding.root,"No item found for search", Snackbar.LENGTH_SHORT).show()
            } else {
                if (!isInventoryRunning) {
                    startSearching()

                    scanningJob = coroutineScope.launch(Dispatchers.IO) {
                        iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                            override fun getInventoryData(var1: SpdInventoryData) {

                                handlerthread.post {
                                    temp.add(var1.getEpc())
                                    binding.tvcount.text = temp.size.toString()
                                    temp.clear()
                                }
                                coroutineScope.launch{
                                    // Log.d("vccc",var1.getEpc())
                                    handleInventoryData(var1)
                                }

                            }

                            override fun onInventoryStatus(status: Int) {
                                threadpooling(status)
                            }


                        })
                    }
                } else {
                    stopSearching()
                    scanningJob?.cancel()
                }
            }
        }

    }

    fun threadpooling(p0:Int) {
        // Create a thread pool with 4 threads
        val threadPool: ExecutorService = Executors.newFixedThreadPool(4)

        // Define a Runnable with your code
        val runnable = kotlinx.coroutines.Runnable {
            Looper.prepare()
            if (p0 == 65277) {
                // Log.d("p0", p0.toString())
                iuhfService.closeDev()
                SystemClock.sleep(100)
                startSearching()
            } else {
                iuhfService.inventoryStart()
            }
            Looper.loop()
        }

        // Submit the Runnable to the thread pool
        threadPool.submit(runnable)

        // Shutdown the thread pool when done
        threadPool.shutdown()
    }


    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true
        initSoundPool()
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30


        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }
        binding.btnStart.text = "Stop"
        iuhfService.inventoryStart()
    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        soundPool!!.release()
        binding.btnStart.text = "Start"
        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
        runOnUiThread(Runnable {
            inventoryAdapter.moveFoundItemsToTop(binding.loader,binding.recyclerview)
            inventoryAdapter.refreshAdapter()
        })
    }

    fun initSoundPool() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attributes = AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build()
        } else {
            SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        }
        soundId = soundPool!!.load(this@InventoryActivity, R.raw.beep, 0)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun inventoryData(){
        if (allDataItem.isNotEmpty()){
            val textColor = ContextCompat.getColor(this, R.color.white)
            binding.tvTotal.setTextColor(textColor)
            inventoryAdapter = InventoryAdapter(allDataItem)
            binding.recyclerview.adapter = inventoryAdapter
            inventoryAdapter.notifyDataSetChanged()

        } else {

            excelViewModel.getAllDataItem.observe(this, Observer { items ->
                if (items.isNotEmpty()) {
                    items.forEach { items ->
                        autoCompleteTextView.setText("")
                        allDataItem.add(
                            AllDataItemEntity(
                                items.idd,
                                items.rfidNo,
                                items.accessNo,
                                items.author,
                                items.title,
                                items.volume,
                                items.place,
                                items.year,
                                items.pages,
                                items.source,
                                items.cost,
                                items.billNo,
                                items.registrationDate,
                                items.userName,
                                items.issue,
                                items.rackNo,
                                Cons.NOTFOUND
                            )
                        )

                        if (!suggestion.contains(items.title)) {
                            suggestion.add(items.title)
                        }


                    }

                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestion)
                autoCompleteTextView.setAdapter(adapter)
                inventoryAdapter = InventoryAdapter(allDataItem)
                binding.recyclerview.adapter = inventoryAdapter
                inventoryAdapter.notifyDataSetChanged()
                binding.tvTotalCount.text = allDataItem.size.toString()
                // progressDialog.dismiss()
            })
        }


    }

    private suspend fun handleInventoryData(var1: SpdInventoryData) {
        try {
            val timeMillis = System.currentTimeMillis()
            val l: Long = timeMillis - lastTimeMillis
            if (l < 100) {
                return
            }
            lastTimeMillis = System.currentTimeMillis()
            soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)

            val epc = var1.getEpc().uppercase().trim()
            if (!temList.contains(epc)) {
                temList.add(epc)

                withContext(Dispatchers.Main) {
                    val foundItem = allDataItem.find { it.rfidNo == epc }
                    if (foundItem != null) {
                        foundItem.status = "Found"


                        val foundValue = allDataItem.count { it.status.equals( "Found") }
                        // binding.Found.text = foundValue.toString()
                        binding.tvFountCount.text=foundValue.toString()


//                        val total = rfidNoList.count()
                        try {
                            val notFound = allDataItem.size - foundValue
                            //binding.notFound.text = notFound.toString()

                            binding.tvNotFoundCount.text=notFound.toString()
                        } catch (e: Exception) {
                            // Handle NumberFormatException
                        }
                        inventoryAdapter.refreshAdapter()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("handleInventoryData", "Exception: ${e.message}", e)
        }
    }




    private fun stopInventoryService() {
        if (isInventoryRunning) {
            // Stop inventory service
            iuhfService.closeDev()
            isInventoryRunning = false
        }
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
        try {
            soundPool?.release()
        } catch (e:Exception){

        }
        iuhfService.closeDev()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

        inventoryData()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode==131) {

            if (allDataItem.isEmpty()){
                Snackbar.make(binding.root,"No item found for search", Snackbar.LENGTH_SHORT).show()
            } else
                if (!isInventoryRunning) {
                    startSearching()

                    // Start inventory service
                    iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                        @RequiresApi(Build.VERSION_CODES.O)
                        @SuppressLint("NotifyDataSetChanged")
                        override fun getInventoryData(var1: SpdInventoryData) {
                            handlerthread.post {
                                temp.add(var1.getEpc())
                                binding.tvcount.text = temp.size.toString()
                                temp.clear()
                            }
                            coroutineScope.launch{
                                // Log.d("vccc",var1.getEpc())
                                handleInventoryData(var1)
                            }


                        }


                        override fun onInventoryStatus(p0: Int) {
                            Looper.prepare()
                            if (p0 == 65277) {
                                iuhfService.closeDev()
                                SystemClock.sleep(100)
                                iuhfService.openDev()
                                iuhfService.inventoryStart()
                            } else {
                                iuhfService.inventoryStart()
                            }
                            Looper.loop()
                        }
                    })

                } else {
                    stopSearching()
                }
        }
        return super.onKeyDown(keyCode, event)
    }



    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateFile() {
        if (allDataItem.isNotEmpty()) {

            filePath = File(
                Environment.getExternalStorageDirectory().toString() + "/" + "File_Inventory${binding.etUserId.text.toString()}${Cons.getDatetime("gettingdate").toString()}" + ".xls"
            )
            try {
                if (!filePath?.exists()!!) {
                    // Create a new workbook and sheet if the file doesn't exist
                    val hssfWorkbook = HSSFWorkbook()
                    val hssfSheet = hssfWorkbook.createSheet("MySheet")

                    // Create the header row
                    val headerRow = hssfSheet.createRow(0)

                    headerRow.createCell(0).setCellValue(Cons.TiME)
                    headerRow.createCell(1).setCellValue(Cons.ID)
                    headerRow.createCell(2).setCellValue(Cons.RFIdNO)
                    headerRow.createCell(3).setCellValue(Cons.AUTHOR)
                    headerRow.createCell(4).setCellValue(Cons.TITILE)
                    headerRow.createCell(5).setCellValue(Cons.PLACE)
                    headerRow.createCell(6).setCellValue(Cons.YEAR)
                    headerRow.createCell(7).setCellValue(Cons.Source)
                    headerRow.createCell(8).setCellValue(Cons.PAGES)
                    headerRow.createCell(9).setCellValue(Cons.STATAUS)





                    allDataItem.forEachIndexed { index, item ->
                        val lastRowNum = hssfSheet.lastRowNum + 1

                        val dataRow = hssfSheet.createRow(lastRowNum)
                        dataRow.createCell(0).setCellValue(Cons.getDatetime("gettingdate").toString())
                        dataRow.createCell(1).setCellValue(item.idd.toString())
                        dataRow.createCell(2).setCellValue(item.rfidNo)
                        dataRow.createCell(3).setCellValue(item.author)
                        dataRow.createCell(4).setCellValue(item.title)
                        dataRow.createCell(5).setCellValue(item.place)
                        dataRow.createCell(6).setCellValue(item.year)
                        dataRow.createCell(7).setCellValue(item.source)
                        dataRow.createCell(8).setCellValue(item.pages)
                        dataRow.createCell(9).setCellValue(item.status)



                    }

                    filePath!!.createNewFile()
                    val fileOutputStream = FileOutputStream(filePath)
                    hssfWorkbook.write(fileOutputStream)

                    Toast.makeText(applicationContext, "File Created", Toast.LENGTH_SHORT)
                        .show()
                    fileOutputStream.flush()
                    fileOutputStream.close()


                } else {
                    // If the file exists, open it and update the data
                    val fileInputStream = FileInputStream(filePath)
                    val hssfWorkbook = HSSFWorkbook(fileInputStream)
                    val hssfSheet: HSSFSheet = hssfWorkbook.getSheetAt(0)

                    allDataItem.forEach { item ->
                        // Calculate the next available row number
                        var lastRowNum = hssfSheet.lastRowNum + 1


                        val dataRow = hssfSheet.createRow(lastRowNum)
                        dataRow.createCell(0).setCellValue(Cons.getDatetime("gettingdate").toString())

                        dataRow.createCell(1).setCellValue(item.idd.toString())
                        dataRow.createCell(2).setCellValue(item.rfidNo)
                        dataRow.createCell(3).setCellValue(item.author)
                        dataRow.createCell(4).setCellValue(item.title)
                        dataRow.createCell(5).setCellValue(item.place)
                        dataRow.createCell(6).setCellValue(item.year)
                        dataRow.createCell(7).setCellValue(item.source)
                        dataRow.createCell(8).setCellValue(item.pages)
                        dataRow.createCell(9).setCellValue(item.status)






                        fileInputStream.close()
                        val fileOutputStream = FileOutputStream(filePath)
                        hssfWorkbook.write(fileOutputStream)

                        Toast.makeText(this@InventoryActivity, "File Updated", Toast.LENGTH_SHORT)
                            .show()
                        fileOutputStream.close()
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("exception", e.toString())
            }
        }else{
            Toast.makeText(applicationContext,"No Item For Export", Toast.LENGTH_SHORT).show()
        }
    }



}