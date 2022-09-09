package com.level.g_fit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class History_Adapter : RecyclerView.Adapter<HistoryViewHolder>() {
    private val items: ArrayList<String> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = items[position]
        holder.steps.text = currentItem
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateSteps(updatedSteps: ArrayList<String>) {
        items.clear()
        items.addAll(updatedSteps)

        notifyDataSetChanged()
    }
}

class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val steps: TextView = itemView.findViewById(R.id.steps)
    val day: TextView = itemView.findViewById(R.id.day)
}