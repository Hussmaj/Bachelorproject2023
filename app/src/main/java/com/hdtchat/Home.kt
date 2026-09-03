package com.hdtchat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

//wifip2p imports
import com.hdtchat.wifip2p.WMActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.hdtchat.vhf.VhfHome
import com.hdtchat.vhf.VHF

class Home : AppCompatActivity() {
    private lateinit var btn_wifi:Button
    private lateinit var btn_vhf:Button

    //wifip2p vent på permission
    var sharedpreferences: SharedPreferences? = null
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            goToWifip2p()
        }
    private val requestRecordPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            goToVhf()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        btn_wifi = findViewById(R.id.btn_wifi)
        btn_vhf = findViewById(R.id.btn_vhf)
        btn_wifi.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    goToWifip2p()
                }
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.NEARBY_WIFI_DEVICES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
                } else {
                    goToWifip2p()
                }
            }
            // slutt wifip2p
        }
        btn_vhf.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    gotomaster()
                }
            }
        }
    }

    //wifip2p starter wifip2p aktivitet
    private fun goToWifip2p() {
        val intent = Intent(this, WMActivity::class.java)
        startActivity(intent)
    }
    private fun goToVhf() {
        val intent = Intent(this, VHF::class.java)
        startActivity(intent)
    }
    private fun gotomaster(){
        val intent = Intent(this,VhfHome::class.java)
        startActivity(intent)
    }
    //wifip2p objekt som holder på noe data
    companion object {
        const val TAG = "WiFiDirectChatHome"
        const val MyPREFERENCES = "MyPrefs"
        const val Name = "nameKey"
        var userName: String = "HDT"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                //Fjerner lagret email og passord fra sharedPreferences.
                val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    remove("email")
                    remove("password")
                    apply()
                }
                //Starter opp login activity
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
  }
