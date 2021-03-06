package com.example.fzugrade2.adapter

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.fzugrade2.R
import com.example.fzugrade2.bean.CourseBean

class Tab1RVAdapter(private var mGradeList: ArrayList<CourseBean>? = null): RecyclerView.Adapter<Tab1RVAdapter.ViewHolder>(){

    fun setList(list: ArrayList<CourseBean>): Tab1RVAdapter{
        mGradeList = list
        return this
    }

    class ViewHolder(v: View): RecyclerView.ViewHolder(v){
        var circle: View = v.findViewById(R.id.circle_v)
        var course: TextView = v.findViewById(R.id.course_tv)
        var statistics: TextView = v.findViewById(R.id.statistics_tv)
        var grade: TextView = v.findViewById(R.id.grade_tv)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.tab1_rv_item,p0,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val gradeBean = mGradeList?.get(p1)
        //圆点颜色
        val d = ShapeDrawable(OvalShape())
        d.paint.color = gradeBean!!.segment
        p0.circle.background = d

        //文本
        p0.apply {
            course.text = gradeBean.name
            statistics.text = gradeBean.statistics
            grade.text = gradeBean.grade.toString()
        }

    }

    override fun getItemCount(): Int = mGradeList!!.size

}