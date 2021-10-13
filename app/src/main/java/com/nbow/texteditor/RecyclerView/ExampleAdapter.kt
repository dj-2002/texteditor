package com.nbow.texteditor.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nbow.texteditor.R


class ExampleAdapter(private val mExampleList: ArrayList<ExampleItem>) :
    RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder>() {
    private var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)

    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }

    class ExampleViewHolder(itemView: View, listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var fileName: TextView
        var fileSize: TextView
        var date: TextView
        var checkBox : CheckBox


        init {
            fileName = itemView.findViewById(R.id.file_name)
            fileSize = itemView.findViewById(R.id.file_size)
            date = itemView.findViewById(R.id.date)
            checkBox=itemView.findViewById(R.id.checkbox_recyclerview)


            itemView.setOnClickListener {
                if (listener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position)

                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.example_item, parent, false)
        return ExampleViewHolder(v, mListener)
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        val currentItem = mExampleList[position]
        holder.fileName.setText(currentItem.fileName)
        holder.fileSize.setText(currentItem.fileSize)
        holder.date.setText(currentItem.date)
        holder.checkBox.isChecked=currentItem.checkbox
        if(currentItem.checkbox==false)
            holder.checkBox.visibility=View.INVISIBLE
        else
            holder.checkBox.visibility=View.VISIBLE
    }

    override fun getItemCount(): Int {
        return mExampleList.size
    }
}