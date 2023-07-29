package com.yusuf.kotlinruntimeapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.yusuf.kotlinruntimeapp.database.Database1
import com.yusuf.kotlinruntimeapp.database.Records
import com.yusuf.kotlinruntimeapp.database.RecordsDao
import com.yusuf.kotlinruntimeapp.databinding.ActivityTimerBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*


class TimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimerBinding
    private lateinit var timer: CustomCountdownTimer
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var db: Database1
    private lateinit var dao: RecordsDao
    private lateinit var compositeDisposable: CompositeDisposable
    var myHour=0
    var myMinute=0
    var mySecond=0
    var id=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.hoursnum.minValue=0
        binding.hoursnum.maxValue=23
        binding.minutesnum.minValue=0
        binding.minutesnum.maxValue=59
        binding.secnum.minValue=0
        binding.secnum.maxValue=59
        var startCounter=0
        registerLauncher()
        db= Room.databaseBuilder(applicationContext,Database1::class.java,"Records").build()
        dao=db.recordsDao()
        compositeDisposable= CompositeDisposable()

        val intent =intent
        val info=intent.getStringExtra("timerInfo")
        if(info.equals("old")) {
            val recordsFromMain = intent.getSerializableExtra("timerRecords") as? Records
            recordsFromMain?.let {
                var myTime=it.time
                var hour=myTime!!/3600
                var minute=(myTime!!-hour*3600)/60
                var second=myTime!!-hour*3600-minute*60
                val realDistance=String.format("%.2f",it.distance)
                val realSpeed=String.format("%.2f",it.speed)
                id=it.id
                binding.timerSpeedTextView.text=realSpeed+" km/h"
                binding.timerDistanceTextView.text=realDistance+" m"
                binding.timerSpeedTextView.visibility=View.VISIBLE
                binding.hoursnum.value=hour.toInt()
                binding.minutesnum.value=minute.toInt()
                binding.secnum.value=second.toInt()
                binding.hoursnum.isEnabled=false
                binding.minutesnum.isEnabled=false
                binding.secnum.isEnabled=false
                binding.timerSwitchButton.isChecked=true
                binding.timerSwitchButton.isEnabled=false
                binding.timerDelete.visibility=View.VISIBLE
                convertToHHMMSS(myTime!!)

            }
        }
        else{
            binding.timerSpeedTextView.visibility=View.GONE
        }

        binding.hoursnum.setOnValueChangedListener { picker, oldVal, newVal ->
            if(newVal/10==0){
                binding.timerHourCounterTextView.text="0"+newVal.toString()+":"
            }else{
                binding.timerHourCounterTextView.text=newVal.toString()+":"
            }

        }
        binding.minutesnum.setOnValueChangedListener { picker, oldVal, newVal ->
            if(newVal/10==0){
                binding.timerMinuteCounterTextView.text="0"+newVal.toString()+":"
            }else{
                binding.timerMinuteCounterTextView.text=newVal.toString()+":"
            }

        }
        binding.secnum.setOnValueChangedListener { picker, oldVal, newVal ->
            if(newVal/10==0){
                binding.timerSecondCounterTextView.text="0"+newVal.toString()
            }
            else{
                binding.timerSecondCounterTextView.text=newVal.toString()
            }

        }
        var myListenerCounter=0
        var firstLatitude:Double=0.0
        var firstLongitude:Double=0.0
        var distanceTraveled =0f
        var currentDistance=0f
        var checkForStopButtonCount=0
        locationManager=this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener= object :LocationListener{
            override fun onLocationChanged(location: Location) {
                //latitude -> enlem
                //longitude-> boylam
                if(myListenerCounter==0|| checkForStopButtonCount==1){
                    checkForStopButtonCount=0
                    firstLatitude=location.latitude
                    firstLongitude=location.longitude
                }
                else{
                    var secondLatitude=location.latitude
                    var secondLongitude=location.longitude
                    val distance = calculateDistance(firstLatitude, firstLongitude, secondLatitude, secondLongitude)
                    firstLatitude=secondLatitude
                    firstLongitude=secondLongitude
                    distanceTraveled=distanceTraveled+distance.toFloat()
                    val formattedValue=String.format("%.2f", distanceTraveled+currentDistance)
                    binding.timerDistanceTextView.text=""+formattedValue+" m"
                }
                myListenerCounter=myListenerCounter+1



            }

        }



        binding.timerStartButton.setOnClickListener {
            if(binding.hoursnum.value==0 &&binding.minutesnum.value==0 &&binding.secnum.value==0){
                Toast.makeText(this,"Select a time",Toast.LENGTH_SHORT).show()
            }
            else{
                var hour=binding.hoursnum.value
                var minute=binding.minutesnum.value
                var second=binding.secnum.value
                var currentTime=second+minute*60+hour*60*60
                timer=CustomCountdownTimer((currentTime*1000).toLong(),1000)
                timer.start()
                binding.hoursnum.isEnabled=false
                binding.hoursnum.isClickable=false
                binding.minutesnum.isEnabled=false
                binding.minutesnum.isClickable=false
                binding.secnum.isEnabled=false
                binding.secnum.isClickable=false
                startCounter=1
                binding.timerStartButton.visibility=View.INVISIBLE
                binding.timerStopButton.visibility=View.VISIBLE
                if (binding.timerSwitchButton.isChecked){
                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0f,locationListener)
                }
                binding.timerSwitchButton.isEnabled=false
                binding.timerSpeedTextView.visibility=View.GONE
                binding.timerFinishButton.isEnabled=false

            }

        }
        binding.timerStopButton.setOnClickListener {
            timer.pause()
            binding.timerResumeButton.visibility=View.VISIBLE
            binding.timerStopButton.visibility=View.INVISIBLE
            startCounter=0
            if (binding.timerSwitchButton.isChecked) {
                locationManager.removeUpdates(locationListener)
                currentDistance = distanceTraveled + currentDistance
                checkForStopButtonCount = 1
                distanceTraveled = 0f

            }
        }
        binding.timerResumeButton.setOnClickListener {
            timer.resume()
            binding.timerResumeButton.visibility=View.INVISIBLE
            binding.timerStopButton.visibility=View.VISIBLE
            startCounter=1
            if (binding.timerSwitchButton.isChecked){
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0f,locationListener)
            }


        }
        binding.timerRefreshButton.setOnClickListener {
            if(startCounter==1){
                timer.finish()
                startCounter=0
            }
            if(info.equals("old")){
                var hour=binding.hoursnum.value
                var minute=binding.minutesnum.value
                var second=binding.secnum.value
                var currentTime=second+minute*60+hour*60*60
                convertToHHMMSS(currentTime.toLong())
            }
            else{
                binding.hoursnum.value=0
                binding.minutesnum.value=0
                binding.secnum.value=0
                binding.hoursnum.isEnabled=true
                binding.hoursnum.isClickable=true
                binding.minutesnum.isEnabled=true
                binding.minutesnum.isClickable=true
                binding.secnum.isEnabled=true
                binding.secnum.isClickable=true
                binding.timerHourCounterTextView.text="00:"
                binding.timerMinuteCounterTextView.text="00:"
                binding.timerSecondCounterTextView.text="00"
                binding.timerSwitchButton.isEnabled=true
            }
            binding.timerDistanceTextView.text="0.00 m"
            binding.timerStopButton.visibility=View.INVISIBLE
            binding.timerResumeButton.visibility=View.INVISIBLE
            binding.timerStartButton.visibility=View.VISIBLE
            if (binding.timerSwitchButton.isChecked){
                locationManager.removeUpdates(locationListener)
            }

        }
        binding.timerFinishButton.setOnClickListener {
            if(binding.timerSwitchButton.isChecked){
                var myTime=myHour*60*60+myMinute*60+mySecond
                var avarageSpeedMeterSec=(currentDistance+distanceTraveled)/myTime.toFloat()
                var avarageSpeedKmHour=avarageSpeedMeterSec*3.6
                val currentDate = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("tr", "TR"))
                val formattedDate = dateFormat.format(currentDate)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(currentDate)
                val records=Records((currentDistance+distanceTraveled),myTime.toLong(),avarageSpeedKmHour,formattedDate+" "+formattedTime,"2")
                compositeDisposable.add(dao.insert(records).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(this::handlerResponse))






            }
        }
        binding.timerDelete.setOnClickListener {
            compositeDisposable.add(dao.deleteRecord(id).
            subscribeOn(Schedulers.io()).
            observeOn(AndroidSchedulers.mainThread()).
            subscribe(this::handlerResponse))
        }
        binding.timerSwitchButton.setOnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked){
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                        Snackbar.make(binding.root,"Permission needed for location", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        }).show()
                    }
                    else{
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
                else{
                    //izin alındı konum al
                }
            }
            else{
                //koşu modundan çoık
                locationManager.removeUpdates(locationListener)

            }

        }
    }
    private fun registerLauncher(){
        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                println("İzin verildi")
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            }
            else{
                Toast.makeText(this,"Permission needed", Toast.LENGTH_LONG).show()
                binding.timerSwitchButton.isChecked=false
            }
        }
    }
    private fun handlerResponse(){
        Toast.makeText(this,"Succeed",Toast.LENGTH_SHORT).show()
        val intent= Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    inner class CustomCountdownTimer(private val totalTime: Long, private val interval: Long) {
        private var remainingTime: Long = totalTime
        private var isPaused: Boolean = false
        private var timer: CountDownTimer? = null

        fun start() {
            timer = object : CountDownTimer(remainingTime, interval) {
                override fun onTick(millisUntilFinished: Long) {
                    if (!isPaused) {
                        remainingTime = millisUntilFinished
                        val seconds = millisUntilFinished / 1000
                        convertToHHMMSS(seconds)
                    }
                }

                override fun onFinish() {
                    Toast.makeText(this@TimerActivity,"Times up",Toast.LENGTH_LONG).show()
                    binding.timerSwitchButton.isEnabled=true
                    myHour=binding.hoursnum.value
                    myMinute=binding.minutesnum.value
                    mySecond=binding.secnum.value
                    binding.hoursnum.value=0
                    binding.minutesnum.value=0
                    binding.secnum.value=0
                    binding.hoursnum.isEnabled=true
                    binding.hoursnum.isClickable=true
                    binding.minutesnum.isEnabled=true
                    binding.minutesnum.isClickable=true
                    binding.secnum.isEnabled=true
                    binding.secnum.isClickable=true
                    binding.timerHourCounterTextView.text="00:"
                    binding.timerMinuteCounterTextView.text="00:"
                    binding.timerSecondCounterTextView.text="00"
                    binding.timerStopButton.visibility=View.INVISIBLE
                    binding.timerResumeButton.visibility=View.INVISIBLE
                    binding.timerStartButton.visibility=View.VISIBLE
                    locationManager.removeUpdates(locationListener)
                    if(binding.timerSwitchButton.isChecked)
                        binding.timerFinishButton.isEnabled=true
                }
            }.start()
        }

        fun pause() {
            isPaused = true
            timer?.cancel()
        }

        fun resume() {
            isPaused = false
            start()
        }
        fun finish() {
            timer?.cancel()
        }
    }
    fun convertToHHMMSS(seconds:Long){
        var time=seconds
        var hour=time/3600
        time=time-hour*3600
        var minute=time/60
        var seconds=time-minute*60
        if(hour.toInt()/10==0){
            binding.timerHourCounterTextView.text="0"+hour.toString()+":"
        }
        else{
            binding.timerHourCounterTextView.text=hour.toString()+":"
        }
        if(minute.toInt()/10==0){
            binding.timerMinuteCounterTextView.text="0"+minute.toString()+":"
        }
        else{
            binding.timerMinuteCounterTextView.text=minute.toString()+":"
        }
        if(seconds.toInt()/10==0){
            binding.timerSecondCounterTextView.text="0"+seconds.toString()
        }
        else{
            binding.timerSecondCounterTextView.text=seconds.toString()
        }

    }
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Dünya'nın ortalama yarıçapı (km)

        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c*1000
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}