package com.example.opsc7311poepart2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter(private val context: Context, private val logsList: List<TimeLog>) :
    RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timesheet, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logsList[position]
        holder.bind(log)
    }

    override fun getItemCount(): Int {
        return logsList.size
    }

    inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tvTask)
        private val imageView: ImageView = itemView.findViewById(R.id.ivTaskImage)

        fun bind(log: TimeLog) {
            // Convert Unix timestamp to formatted date string
            val formattedDate = formatDate(log.selectedDate.toLong())
            dateTextView.text = formattedDate
            categoryTextView.text = log.selectedCategory

            Picasso.get().load(log.imageUrl).into(imageView)
        }

        // Function to format Unix timestamp to "dd MMMM yyyy" format
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            return sdf.format(calendar.time)
        }
    }
}
