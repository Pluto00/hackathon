package com.example.fzugrade2.bean

import android.graphics.Color
import android.support.v4.graphics.PathSegment

class GradeBean(var course: String ,segment: Int, rank: Int, average: Double, grade: Double){
    companion object {
        //颜色值：优秀，良好，及格，不及格
        val gradeList = listOf(
            Color.parseColor("#80cc66"),
            Color.parseColor("#b5cc66"),
            Color.parseColor("#cc9466"),
            Color.parseColor("#cc6666"))
    }

    var segment = 0
    var statistics: String
    var grade: String

    init {
        //对分数段另做初始化
        this.segment = gradeList[segment]
        statistics = "单科排名：$rank    平均分：$average"
        this.grade = grade.toString()
    }
}