package com.yusuf.kotlinruntimeapp

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.yusuf.kotlinruntimeapp.database.Database1
import com.yusuf.kotlinruntimeapp.database.Records
import com.yusuf.kotlinruntimeapp.database.RecordsDao
import com.yusuf.kotlinruntimeapp.databinding.ActivityCronometerBinding
import com.yusuf.kotlinruntimeapp.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class CronometerActivtiy : AppCompatActivity() {
    private lateinit var binding: ActivityCronometerBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var db:Database1
    private lateinit var dao:RecordsDao
    var requestLocation=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCronometerBinding.inflate(layoutInflater)
        val intent =intent
        val info=intent.getStringExtra("chronometerInfo")
        var view=binding.root
        setContentView(binding.root)
        var stop_timer:Long=0
        var start_sayac=0
        var totalSpeed=0f
        var distanceTraveled =0f
        var currentDistance=0f
        var firstLatitude:Double=0.0
        var firstLongitude:Double=0.0
        var myListenerCounter=0
        var checkForStopButtonCount=0
        var recordID=0
        if(info.equals("old")){
            println("Eski")
            binding.chronometer.visibility=View.INVISIBLE
            binding.cronometerTimeTextView.visibility=View.VISIBLE
            binding.cronometerSpeedTextView.visibility=View.VISIBLE
            binding.cronometerAverageSpeedTextView.visibility=View.VISIBLE
            binding.chronometerDelete.visibility=View.VISIBLE

            val recordsFromMain=intent.getSerializableExtra("chronometerRecords") as? Records
            recordsFromMain?.let {
                val realDistance=String.format("%.2f",it.distance)
                val realSpeed=String.format("%.2f",it.speed)
                val realTime=it.time.toString().toLong()
                recordID=it.id
                if(realTime/3600<1){
                    var minute=(realTime/60).toString()
                    var second=(realTime-minute.toInt()*60).toString()
                    if(minute.toInt()/10==0){
                        minute="0"+minute
                    }
                    if(second.toInt()/10==0){
                        second="0"+second
                    }

                    binding.cronometerTimeTextView.text=""+minute+":"+second
                }
                else{
                    var hour=(realTime/3600).toString()
                    var minute=((realTime-hour.toInt()*3600)/60).toString()
                    var second=(realTime-hour.toInt()*3600-minute.toInt()*60).toString()
                    if(hour.toInt()/10==0){
                        hour="0"+hour
                    }
                    if(minute.toInt()/10==0){
                        minute="0"+minute
                    }
                    if(second.toInt()/10==0){
                        second="0"+second
                    }

                    binding.cronometerTimeTextView.text=""+hour+":"+minute+":"+second

                }


                binding.cronometerSpeedTextView.text=realDistance+" m"
                binding.cronometerAverageSpeedTextView.text=realSpeed+" km/h"
                binding.cronometreSwitch.isChecked=true
                binding.chronometerFinish.text="UPDATE"
            }

        }
        else{
            println("Yeni")
        }
        compositeDisposable=CompositeDisposable()
        binding.chronometer.format="%s"
        locationManager=this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener= object :LocationListener{
            override fun onLocationChanged(location: Location) {
                //latitude -> enlem
                //longitude-> boylam

                if(myListenerCounter==0 || checkForStopButtonCount==1){
                    checkForStopButtonCount=0
                    firstLatitude=location.latitude
                    firstLongitude=location.longitude
                }else{
                    var secondLatitude=location.latitude
                    var secondLongitude=location.longitude
                    val distance = calculateDistance(firstLatitude, firstLongitude, secondLatitude, secondLongitude)
                    firstLatitude=secondLatitude
                    firstLongitude=secondLongitude
                    distanceTraveled=distanceTraveled+distance.toFloat()
                    val formattedValue=String.format("%.2f", distanceTraveled+currentDistance)
                    binding.cronometerSpeedTextView.text=formattedValue+" m"
                }
                myListenerCounter=myListenerCounter+1

            }

        }
        db= Room.databaseBuilder(applicationContext,Database1::class.java,"Records").build()
        dao=db.recordsDao()
        registerLauncher()
        binding.chronometerStart.setOnClickListener {
            start_sayac=start_sayac+1
            binding.chronometer.base=SystemClock.elapsedRealtime()+stop_timer
            binding.chronometer.start()
            binding.chronometerStart.visibility= View.INVISIBLE
            binding.chronometerStop.visibility=View.VISIBLE
            binding.chronometerFinish.isEnabled=false
            if(start_sayac>1){
                binding.waitingAnimation.resumeAnimation()
            }else{
                binding.waitingAnimation.playAnimation()
            }
            binding.cronometreSwitch.isEnabled=false
            if(binding.cronometreSwitch.isChecked){
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0f,locationListener)
            }
            if(info.equals("old")){
                binding.chronometer.visibility=View.VISIBLE
                binding.cronometerTimeTextView.visibility=View.INVISIBLE
                binding.cronometerAverageSpeedTextView.visibility=View.INVISIBLE
                binding.cronometerSpeedTextView.text="0.00 m"
                if(binding.cronometreSwitch.isChecked){
                    binding.cronometerSpeedTextView.visibility=View.VISIBLE
                }
                else{
                    binding.cronometerSpeedTextView.visibility=View.GONE
                }
            }




        }
        binding.chronometerStop.setOnClickListener {
            stop_timer=binding.chronometer.base-SystemClock.elapsedRealtime()
            println(stop_timer)
            binding.chronometer.stop()
            binding.chronometerStop.visibility=View.INVISIBLE
            binding.chronometerStart.visibility=View.VISIBLE
            binding.waitingAnimation.pauseAnimation()
            if(binding.cronometreSwitch.isChecked){
                locationManager.removeUpdates(locationListener)
                currentDistance=distanceTraveled+currentDistance
                checkForStopButtonCount=1
                distanceTraveled=0f
            }
            binding.chronometerFinish.isEnabled=true


        }
        binding.chronometerReset.setOnClickListener {
            binding.chronometer.base=SystemClock.elapsedRealtime()
            stop_timer=0
            binding.chronometer.stop()
            binding.chronometerStop.visibility=View.INVISIBLE
            binding.chronometerStart.visibility=View.VISIBLE
            binding.waitingAnimation.pauseAnimation()
            binding.cronometreSwitch.isEnabled=true
            binding.chronometerFinish.isEnabled=false
            if(binding.cronometreSwitch.isChecked){
                locationManager.removeUpdates(locationListener)
                distanceTraveled=0f
                currentDistance=0f
                binding.cronometerSpeedTextView.text="0.00 m"
            }
        }
        binding.chronometerFinish.setOnClickListener {
            var time=-stop_timer/1000
            if(binding.cronometreSwitch.isChecked){
                var avarageSpeedMeterSec=currentDistance/time.toFloat()
                var avarageSpeedKmHour=avarageSpeedMeterSec*3.6
                val currentDate = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("tr", "TR"))
                val formattedDate = dateFormat.format(currentDate)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(currentDate)
                //chronometer takes value of 1 ---- timer takes value of 2
                var record=Records(currentDistance,time,avarageSpeedKmHour,formattedDate+" "+formattedTime,"1")
                if(info.equals("old")){
                    compositeDisposable.add(
                        dao.deleteRecord(recordID).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(this::handleUpgradeResponse)
                    )
                    compositeDisposable.add(
                        dao.insert(record).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(this::handleResponse)
                    )
                }
                else{
                    try{
                        compositeDisposable.add(
                            dao.insert(record).
                            subscribeOn(Schedulers.io()).
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribe(this::handleResponse))
                    }catch (e:Exception){
                        println(e.message)
                    }
                }



            }
            else{

            }
        }
        binding.chronometerDelete.setOnClickListener{
            compositeDisposable.add(dao.deleteRecord(recordID).
            subscribeOn(Schedulers.io()).
            observeOn(AndroidSchedulers.mainThread()).
            subscribe(this::handleResponse))
        }

        binding.cronometreSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                println("Koşu modu açık")
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                        Snackbar.make(binding.root,"Permission needed for location",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        }).show()
                    }
                    else{
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
                else{
                    //permission granted
                    println("konum iste")

                    requestLocation=1
                }
                binding.cronometerSpeedTextView.visibility=View.VISIBLE



            }
            else{
                locationManager.removeUpdates(locationListener)
                println("Koşu modu kapalı")
                binding.cronometerSpeedTextView.visibility=View.GONE
                if(info.equals("old")){
                    binding.chronometer.visibility=View.VISIBLE
                    binding.cronometerTimeTextView.visibility=View.INVISIBLE
                    binding.cronometerAverageSpeedTextView.visibility=View.INVISIBLE
                }
            }
        }





    }

    fun handleResponse(){
        Toast.makeText(this,"Succeed",Toast.LENGTH_SHORT).show()
        val intent= Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

    }
    fun handleUpgradeResponse(){
        Toast.makeText(this,"Updated",Toast.LENGTH_SHORT).show()
    }
    private fun registerLauncher(){
        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->
            if(result){
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    println("İzin verildi")
                    requestLocation=1
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                }


            }
            else{

                Toast.makeText(this,"Permission needed", Toast.LENGTH_LONG).show()
                binding.cronometreSwitch.isChecked=false
            }

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