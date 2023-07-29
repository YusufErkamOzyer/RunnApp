package com.yusuf.kotlinruntimeapp

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu

import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.yusuf.kotlinruntimeapp.Adapter.RecordsAdapter
import com.yusuf.kotlinruntimeapp.database.Database1
import com.yusuf.kotlinruntimeapp.database.Records
import com.yusuf.kotlinruntimeapp.database.RecordsDao
import com.yusuf.kotlinruntimeapp.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var db:Database1
    private lateinit var dao:RecordsDao
    private lateinit var compositeDisposable: CompositeDisposable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        db= Room.databaseBuilder(applicationContext,Database1::class.java,"Records").build()
        dao=db.recordsDao()
        compositeDisposable=CompositeDisposable()

        compositeDisposable.add(dao.getAll().
        subscribeOn(Schedulers.io()).
        observeOn(AndroidSchedulers.mainThread()).
        subscribe(this::handleResponse))


    }
    private fun handleResponse(recordsList:List<Records>){
        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        val adapter=RecordsAdapter(recordsList)
        binding.recyclerView.adapter=adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.my_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menu_item_cronometer){
            val intent=Intent(this@MainActivity,CronometerActivtiy::class.java)
            startActivity(intent)
            println("Kronometreye bastın")
        }
        else if(item.itemId==R.id.menu_item_timer){
            val intent=Intent(this@MainActivity,TimerActivity::class.java)
            startActivity(intent)
            println("Zamanlayıcıya bastın")
        }
        return super.onOptionsItemSelected(item)
    }
    private fun registerLauncher(){
        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){  result->
            if(result){
                println("İzin verildi")
            }
            else{

                Toast.makeText(this,"Permission needed",Toast.LENGTH_LONG).show()
                onDestroy()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}