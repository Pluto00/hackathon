package com.example.fzugrade2.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fzugrade2.R
import com.example.fzugrade2.bean.GradeBean
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.collections.ArrayList

class MyPagerAdapter(private var pageCount: Int,private var context: Context): PagerAdapter(){


    private val blue = Color.parseColor("#373371d8")
    private val blue2 = Color.parseColor("#623371d8")
    private val red = Color.parseColor("#37cc5e50")
    private val grey = Color.parseColor("#37888888")

    lateinit var radar: RadarChart
    lateinit var pie1: PieChart
    lateinit var pie2: PieChart
    lateinit var pie3: PieChart
    lateinit var pie4: PieChart



    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout: View
        return when(position) {
            0 -> {
                layout = LayoutInflater.from(context)
                    .inflate(R.layout.tab1_ayout, container, false)
                initRecyclerView(layout)
                container.addView(layout)
                layout
            }
            1 -> {
                layout = LayoutInflater.from(context)
                    .inflate(R.layout.tab2_layout,container,false)
                initRadar(layout)
                initPie(layout)
                container.addView(layout)
                layout
            }
            else -> {
                layout = LayoutInflater.from(context)
                    .inflate(R.layout.tab3_layout,container,false)
                initLine(layout)
                container.addView(layout)
                layout
            }
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return pageCount
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return p0 == p1
    }

    //初始化折线图
    private fun initLine(v: View){
        val line = v.findViewById<GraphView>(R.id.line_chart)
        val data = LineGraphSeries<DataPoint>(arrayOf(
            DataPoint(0.0,1.0),
            DataPoint(1.0,1.5),
            DataPoint(2.0,3.0),
            DataPoint(4.0,6.0)
        ))
        line.addSeries(data)
    }

    //初始化雷达图
    private fun initRadar(v: View){
        radar = v.findViewById(R.id.tab2_radar)
        radar.apply {
            description.isEnabled = false
            xAxis.run {
                valueFormatter = object: ValueFormatter(){
                    private val xAxisTexts = arrayOf("A","B","C","D","E","F")
                    override fun getFormattedValue(value: Float): String {
                        return xAxisTexts[value.toInt() % xAxisTexts.size]
                    }
                }
                textSize = 8f
                //axisLineWidth = 5f
            }
            yAxis.run {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawLabels(false)
            }
            legend.apply {
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                yOffset = 20f
                xOffset = 20f
            }
            //数据
            val entries = arrayListOf(RadarEntry(62f), RadarEntry(71f),
                RadarEntry(80f),RadarEntry(91f), RadarEntry(44f), RadarEntry(120f)
            )
            val dataSet = RadarDataSet(entries,"个人得分").apply {
                color = blue
                fillColor = blue
                setDrawFilled(true)
            }

            val sets = arrayListOf<IRadarDataSet>(dataSet)
            val finalData = RadarData(sets).apply {
                setValueTextSize(8f)
                setDrawValues(false)
            }

            data = finalData
            data.setValueTextSize(10f)
            animateXY(500,500,Easing.EaseInBounce)
        }
    }

    //初始化饼图
    private fun initPie(v: View){
        pie1 = v.findViewById(R.id.rate1)
        pie2 = v.findViewById(R.id.rate2)
        pie3 = v.findViewById(R.id.rate3)
        pie4 = v.findViewById(R.id.rate4)

        //视图
        pie1.apply {
            centerText = "80%"
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            isRotationEnabled = false
            isHighlightPerTapEnabled = true
            animateY(1400,Easing.EaseInOutQuad)
            legend.isEnabled = false
            description.isEnabled = false
        }
        pie2.apply {
            centerText = "80%"
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            isRotationEnabled = false
            isHighlightPerTapEnabled = true
            animateY(1400,Easing.EaseInOutQuad)
            legend.isEnabled = false
            description.isEnabled = false
        }
        pie3.apply {
            centerText = "80%"
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            isRotationEnabled = false
            isHighlightPerTapEnabled = true
            animateY(1400,Easing.EaseInOutQuad)
            legend.isEnabled = false
            description.isEnabled = false
        }
        pie4.apply {
            centerText = "80%"
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            isRotationEnabled = false
            isHighlightPerTapEnabled = true
            animateY(1400,Easing.EaseInOutQuad)
            legend.isEnabled = false
            description.isEnabled = false
        }

        //数据
        val entries = arrayListOf(
            PieEntry(0.8f),
            PieEntry(0.2f)
        )

        val dataSet = PieDataSet(entries,"ALL")
        dataSet.setColors(blue2,grey)
        dataSet.selectionShift = 3f
        dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        pie1.data = data
        pie2.data = data
        pie3.data = data
        pie4.data = data

    }

    //初始化RecyclerView
    private fun initRecyclerView(v: View){
        val rv = v.findViewById<RecyclerView>(R.id.tab1_rv)
        val list = ArrayList<GradeBean>()
        val layoutManager = LinearLayoutManager(v.context)
        list.apply {
            add(GradeBean("毛泽东思想和中国特色社会主义理论体系概论（上）",0,10,85.22,85.5))
            add(GradeBean("Java程序设计",1,20,79.89,80.0))
            add(GradeBean("计算机硬件基础",2,30,80.56,70.0))
            add(GradeBean("大学物理（A）",3,50,70.75,59.0))
        }
        rv?.layoutManager = layoutManager
        rv?.adapter = Tab1RVAdapter(list)
    }

}