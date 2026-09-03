package com.hdtchat.wifip2p.chat

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.hdtchat.R


class WChatFragment(private var comms: Comms) : Fragment() {
    private var view: View? = null
    private var textBox: TextView? = null
    private var listView: ListView? = null
    var adapter: ChatMessageAdapter? = null
    private val items: List<String> = ArrayList()
    var sharedpreferences: SharedPreferences? = null
    private lateinit var brukername: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedpreferences = this.requireActivity()
            .getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE)
        view = inflater.inflate(R.layout.fragment_chat, container, false)
        textBox = view!!.findViewById(R.id.txtChatLine)
        listView = view!!.findViewById(android.R.id.list)
        adapter = ChatMessageAdapter(
            activity, android.R.id.text1,
            items
        )
        listView!!.adapter = adapter


        view!!.findViewById<View>(R.id.button1).setOnClickListener {
                brukername = sharedpreferences?.getString("username", "")!!
                comms.sendMessage = brukername+": "+ textBox!!.text.toString()
                pushMessage(brukername+": "+textBox!!.text.toString())
                textBox!!.text = ""
                textBox!!.clearFocus()
            }
        return view
    }

    fun pushMessage(readMessage: String) {
        adapter?.add(readMessage)
        adapter?.notifyDataSetChanged()
    }

    inner class ChatMessageAdapter(
        context: Context?, textViewResourceId: Int,
        items: List<String>?
    ) : ArrayAdapter<String?>(
        (context)!!, textViewResourceId, (items)!!
    ) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            brukername = sharedpreferences?.getString("username", "")!!
            var v = convertView
            if (v == null) {
                val vi = activity!!
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = vi.inflate(android.R.layout.simple_list_item_1, null)
            }
            val message: String = items[position]
            if (!message.isEmpty()) {
                val nameText = v!!.findViewById<TextView>(android.R.id.text1)
                if (nameText != null) {
                    val parts = message.split(":")
                    if (parts.size >= 2) {
                        val senderName = parts[0]
                        val messageText = parts[1]
                        nameText.text = "$senderName: $messageText"
                    } else {
                        // handle the case where the message string does not contain a colon
                        nameText.text = message
                    }
                        if (message.startsWith(brukername+":")) {
                            @Suppress("DEPRECATION")
                            nameText.setTextAppearance(activity, R.style.SentMessage)
                        } else {
                            @Suppress("DEPRECATION")
                            nameText.setTextAppearance(activity, R.style.incomingMessage)
                        }
                    }
                }
                return v!!
            }


    }

    companion object {
        const val TAG = "WifiChatFragment"
        val MyPREFERENCES = "MyPrefs"
        val Name = "nameKey"
    }
}