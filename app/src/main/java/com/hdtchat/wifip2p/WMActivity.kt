package com.hdtchat.wifip2p

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hdtchat.BR
import com.hdtchat.R
import com.hdtchat.wifip2p.PeerListFragment.DeviceClickListener
import com.hdtchat.wifip2p.PeerListFragment.WiFiDevicesAdapter
import com.hdtchat.wifip2p.chat.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import com.hdtchat.wifip2p.chat.CommsRepository

class WMActivity: AppCompatActivity(), DeviceClickListener,
    ConnectionInfoListener {
    var fragmentManager = supportFragmentManager
    private val intentFilter: IntentFilter = IntentFilter()
    private var manager: WifiP2pManager? = null
    private var channel: Channel? = null
    private var receiver: BroadcastReceiver? = null
    private var servicesList: PeerListFragment? = null
    private var statusTxtView: TextView? = null
    private var peers: Peers? = null
    private var peersRepository: PeersRepository? = null
    private lateinit var comms: Comms
    private var commsRepository: CommsRepository? = null
    private var chatFragment: WChatFragment? = null


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.p2pmain)
        startUpInitialisation()

        lifecycleScope.launch(Dispatchers.Main) {
            comms.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
                override fun onPropertyChanged(
                    sender: androidx.databinding.Observable?,
                    propertyId: Int
                ) {
                    if (propertyId == BR.message) {
                        Log.d(TAG,"Melding motatt: " + comms.message)
                        // Cache i databse
                        chatFragment!!.pushMessage(comms.message)
                    }
                }
            })
        }
        Log.d(TAG, "peersRepository initialisert")
        statusTxtView = findViewById(R.id.status_text)
        if (isWifiDirectSupported(this)) {
            appendStatus("WiFi Direct is Supported :)")
        } else {
            appendStatus("WiFi Direct is NOT supported :(")
        }
        servicesList = PeerListFragment()
        fragmentManager.beginTransaction().add(R.id.container_root, servicesList!!, "services")
            .commit()
    }

    private fun startUpInitialisation() {
        intentFilter.addAction(WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager?
        //Registrerer applikasjonen på Wi-Fi rammeverket.
        channel = manager!!.initialize(this, mainLooper, null)
        peers = Peers()
        comms = Comms()
        commsRepository = CommsRepository(comms)
        peersRepository = PeersRepository(manager!!, channel!!)
        receiver = WiFiDirectBroadcastReceiver(manager!!,channel!!)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.Main) {
            postPeers()
        }
        receiver?.also { receiver ->
            registerReceiver(receiver,intentFilter)
        }
        lifecycleScope.launch(Dispatchers.Main) {
            peersRepository!!.findPeers().takeWhile { it != "finished" }.collect {
                if (it != "Finished") {
                    appendStatus(it)
                }
            }
            Log.d(TAG, "Peer search initialised, related flow closed")
        }
    }

    override fun onRestart() {
        super.onRestart()
        @Suppress("DEPRECATION")
        Handler().post {
            val frag: Fragment? = fragmentManager.findFragmentByTag("services")
            fragmentManager.beginTransaction().remove((frag)!!).commit()
        }
    }
    override fun onStop() {
        super.onStop()
        lifecycleScope.launch(Dispatchers.Main) {
            peersRepository!!.disconnect().collect {
                Log.d(
                    TAG, it
                )
            }
        }
        peersRepository!!.stopP2p()
    }
    public override fun onPause() {
        super.onPause()
        receiver?.also { receiver ->
            unregisterReceiver(receiver)
        }
        lifecycleScope.launch(Dispatchers.Main) {
            peersRepository!!.disconnect().collect {
                Log.d(TAG, it)
            }
        }
        peersRepository!!.stopP2p()
    }
    private fun postPeers() {
        val fragment: PeerListFragment? =
            fragmentManager.findFragmentByTag("services") as PeerListFragment?
        if (fragment != null) {
            var adapter: WiFiDevicesAdapter? = (fragment
                .getListAdapter() as WiFiDevicesAdapter?)
            adapter?.clear()
            adapter!!.notifyDataSetChanged()
            adapter = (fragment
                .getListAdapter() as WiFiDevicesAdapter?)
            lifecycleScope.launch(Dispatchers.Main) {
                peersRepository!!.getPeers(peers!!).collect {
                    adapter!!.add(it)
                    appendStatus(it.device!!.deviceName)
                }
                adapter!!.notifyDataSetChanged()
            }
        }
    }
    override fun connectingToPeer(peers: Peers) {
        lifecycleScope.launch(Dispatchers.Main) {
            peersRepository!!.connectToPeer(peers).collect {
                appendStatus(it)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun appendStatus(status: String) {
        val current: String = statusTxtView!!.text.toString()
        statusTxtView!!.text = current + "\n" + status
    }
    private fun isWifiDirectSupported(ctx: Context): Boolean {
        val pm: PackageManager = ctx.packageManager
        val features: Array<FeatureInfo> = pm.systemAvailableFeatures
        for (info: FeatureInfo? in features) {
            if ((info != null) && (info.name != null) && info.name.equals(
                    "android.hardware.wifi.direct",
                    ignoreCase = true
                )
            ) {
                return true
            }
        }
        return false
    }
    override fun onConnectionInfoAvailable(wifip2pInfo: WifiP2pInfo) {
        lifecycleScope.launch(Dispatchers.Main) {
            commsRepository!!.startCommunication(wifip2pInfo).collect {
                Log.d(TAG, it)
                if (it == "Chat started now") {
                    if (chatFragment == null) {
                        chatFragment = WChatFragment(comms)
                        fragmentManager.beginTransaction()
                            .replace(R.id.container_root, chatFragment!!)
                            .commit()
                        statusTxtView!!.visibility = View.GONE

                    }
                }
            }
        }
    }
    companion object {
        const val TAG: String = "WifiP2pMainActivity"
        const val SERVER_PORT: Int = 4545

        fun triggerRebirth(context: Context) {
            val packageManager: PackageManager = context.packageManager
            val intent: Intent? = packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName: ComponentName? = intent!!.component
            val mainIntent: Intent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }
    }
    // ENESTE KALL PÅ MAINACTIVITY
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        0 -> {
            connectingToPeer(peers!!)
            triggerRebirth(this)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    }
}