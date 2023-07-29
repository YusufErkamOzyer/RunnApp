package com.yusuf.kotlinruntimeapp.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yusuf.kotlinruntimeapp.CronometerActivtiy
import com.yusuf.kotlinruntimeapp.TimerActivity
import com.yusuf.kotlinruntimeapp.database.Records
import com.yusuf.kotlinruntimeapp.databinding.RecyclerRowBinding

class RecordsAdapter(val recordsList: List<Records>):RecyclerView.Adapter<RecordsAdapter.RecordsHolder>() {
    class RecordsHolder(val recyclerRowBinding: RecyclerRowBinding):RecyclerView.ViewHolder(recyclerRowBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordsHolder {

        val recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RecordsHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(holder: RecordsHolder, position: Int) {

        if (recordsList.get(position).currentTime == "1") {
            holder.recyclerRowBinding.recyclerViewTextView.text =
                recordsList.get(position).currentDate + "               " + "Chronometer"
            //intent yapılacak
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, CronometerActivtiy::class.java)
                intent.putExtra("chronometerRecords",recordsList[position])
                intent.putExtra("chronometerInfo","old")
                holder.itemView.context.startActivity(intent)
            }
        } else if (recordsList.get(position).currentTime == "2") {
            holder.recyclerRowBinding.recyclerViewTextView.text =
                recordsList.get(position).currentDate + "               " + "Timer"
            //intent yapılacak
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, TimerActivity::class.java)
                intent.putExtra("timerRecords",recordsList[position])
                intent.putExtra("timerInfo","old")
                holder.itemView.context.startActivity(intent)
            }

        }
    }

    override fun getItemCount(): Int {

        return recordsList.size
    }
}