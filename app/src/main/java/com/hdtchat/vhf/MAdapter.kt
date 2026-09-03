package com.hdtchat.vhf

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hdtchat.R

class MAdapter(val context: Context, private val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_R = 1
    private val ITEM_S = 2

    data class Message(val message: String, val isSent: Boolean,val image: Uri?)

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].isSent) {
            ITEM_S
        } else {
            ITEM_R
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_R -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recive_message, parent, false)
                Receiveviewholder(view)
            }
            ITEM_S -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chatsent, parent, false)
                Sentviewholder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }


    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (getItemViewType(position) == ITEM_S) {
            // sent
            val viewHolder = holder as Sentviewholder
            if (viewHolder.SentMessage != null) {
                viewHolder.SentMessage.text = currentMessage.message
            }
            if (viewHolder.SentImage != null) {
                currentMessage.image?.let {
                    viewHolder.SentImage.visibility = View.VISIBLE
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    viewHolder.SentImage.setImageBitmap(bitmap)
                } ?: run {
                    viewHolder.SentImage.visibility = View.GONE
                }
            }
        } else {
            // receive
            val viewHolder = holder as Receiveviewholder
            viewHolder.ReceiveMessage.text = currentMessage.message
        }
    }



    class Sentviewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val SentMessage = itemView.findViewById<TextView>(R.id.text_s)
        val SentImage = itemView.findViewById<ImageView>(R.id.imageView)
    }

    class Receiveviewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ReceiveMessage: TextView = itemView.findViewById(R.id.text_R)
    }
}


