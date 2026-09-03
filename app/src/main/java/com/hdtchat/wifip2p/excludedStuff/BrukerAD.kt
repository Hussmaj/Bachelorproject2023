package com.hdtchat.wifip2p

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hdtchat.R
import com.hdtchat.Bruker

class brukerAD(val context: Context, val userList:ArrayList<Bruker>):
    RecyclerView.Adapter<brukerAD.userviewholder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): userviewholder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.bruker,parent,false )
        return userviewholder(view)
    }

    override fun onBindViewHolder(holder: userviewholder, position: Int) {
        val currentuser = userList[position]
        holder.textName.text = currentuser.name
    }

    override fun getItemCount(): Int {
        return userList.size
    }



    class userviewholder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textName = itemView.findViewById<TextView>(R.id.txt_bruker)
    }
}