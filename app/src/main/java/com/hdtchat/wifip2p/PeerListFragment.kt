package com.hdtchat.wifip2p

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.ListFragment
import com.hdtchat.R

class PeerListFragment : ListFragment() {
    var listAdapter: WiFiDevicesAdapter? = null
    internal interface DeviceClickListener {
        fun connectingToPeer(peers: Peers)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.devices_list, container, false)
    }

    @Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listAdapter = WiFiDevicesAdapter(
            activity,
            android.R.layout.simple_list_item_2, android.R.id.text1,
            ArrayList()
        )
        setListAdapter(listAdapter)
    }
    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        (activity as DeviceClickListener).connectingToPeer(
            l
                .getItemAtPosition(position) as Peers
        )
    }


    inner class WiFiDevicesAdapter constructor(
        context: Context?, resource: Int,
        textViewResourceId: Int, private val items: List<Peers?>
    ) : ArrayAdapter<Peers?>((context)!!, resource, textViewResourceId, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v: View? = convertView
            if (v == null) {
                val vi: LayoutInflater = requireActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = vi.inflate(android.R.layout.simple_list_item_2, null)
            }
            val service: Peers? = items.get(position)
            if (service != null) {
                val nameText: TextView? = v!!.findViewById(android.R.id.text1)
                if (nameText != null) {
                    nameText.text = service.device!!.deviceName
                }
                val statusText: TextView = v.findViewById(android.R.id.text2)
                statusText.text = getDeviceStatus(service.device!!.status)
            }
            return (v)!!
        }
    }

    companion object {
        fun getDeviceStatus(statusCode: Int): String {
            when (statusCode) {
                WifiP2pDevice.CONNECTED -> return "Connected" // 0
                WifiP2pDevice.INVITED -> return "Invited"
                WifiP2pDevice.FAILED -> return "Failed"
                WifiP2pDevice.AVAILABLE -> return "Available"
                WifiP2pDevice.UNAVAILABLE -> return "Unavailable"
                else -> return "Unknown"
            }
        }
    }
}