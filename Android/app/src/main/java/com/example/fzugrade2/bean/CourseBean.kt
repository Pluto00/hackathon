package com.example.fzugrade2.bean

import android.graphics.Color
import com.google.gson.annotations.SerializedName

class CourseBean(
    var name: String,
    var grade: Float,
    segment: Int,
    val rank: Int,
    val average: Float){
    companion object {
        //颜色值：优秀，良好，及格，不及格
        val gradeList = listOf(
            Color.parseColor("#80cc66"),
            Color.parseColor("#b5cc66"),
            Color.parseColor("#cc9466"),
            Color.parseColor("#cc6666"))
    }


    val segment = segment
        get() = gradeList[field]
    @SerializedName("rank_rate")
    val rankRate = 0.0
    val statistics: String
        get() = "名次:$rank    平均分:$average    超过了:$rankRate%"

}