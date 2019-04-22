package com.example.fzugrade2.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.fzugrade2.R

class TermPopupAdapter(var termList: ArrayList<String>): RecyclerView.Adapter<TermPopupAdapter.ViewHolder>(){

    private lateinit var mOnItemClickListener: OnItemClickListener

    class ViewHolder(v: View): RecyclerView.ViewHolder(v){
        var tv: TextView = v.findViewById(R.id.tab1_rv)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.tv_layout,p0,false)
        val result = ViewHolder(v)
        result.itemView.setOnClickListener{
            mOnItemClickListener.onClick(p1)
        }
        return ViewHolder(v)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.tv.text = termList[p1]
    }

    override fun getItemCount(): Int {
        return termList.size
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        mOnItemClickListener = onItemClickListener
    }

    interface OnItemClickListener{
        fun onClick(i: Int)
    }


}