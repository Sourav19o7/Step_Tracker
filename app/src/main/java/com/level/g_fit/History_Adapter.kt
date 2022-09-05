package com.level.g_fit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class History_Adapter: RecyclerView.Adapter<historyViewHolder>() {
    private val items: ArrayList<String> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): historyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return historyViewHolder(view)
    }

    override fun onBindViewHolder(holder: historyViewHolder, position: Int) {
        val currentItem = items[position]
        holder.titleView.text = currentItem
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateSteps(updatedSteps: ArrayList<String>)
    {
        items.clear()
        items.addAll(updatedSteps)

        notifyDataSetChanged()
    }
}

class historyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val titleView: TextView = itemView.findViewById(R.id.steps)
}