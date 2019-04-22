package com.example.fzugrade2.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.fzugrade2.R
import com.example.fzugrade2.RadarMarkerView
import com.example.fzugrade2.bean.CourseBean
import com.example.fzugrade2.bean.InfoBean
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.collections.ArrayList

class MyPagerAdapter(
    private var pageCount: Int,
    private var context: Context,
    private var list: ArrayList<CourseBean>,
    private var info: InfoBean): PagerAdapter()
{

    private val blue = Color.parseColor("#37e58a55")
    private val blue2 = Color.parseColor("#62e58a55")
    private val red = Color.parseColor("#37549f8b")
    private val grey = Color.parseColor("#37888888")

    private lateinit var radar: RadarChart
    private var pieList = ArrayList<PieChart>()
    private lateinit var data: LineGraphSeries<DataPoint>
    private lateinit var line: GraphView
    lateinit var rv: RecyclerView
    private lateinit var layout2: View
    private lateinit var layout3: View
    private lateinit var mv: RadarMarkerView
    private lateinit var textView: TextView

    private lateinit var tl1: TextView
    private lateinit var tr1: TextView
    private lateinit var tl2: TextView
    private lateinit var tr2: TextView


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
                layout2 = LayoutInflater.from(context)
                    .inflate(R.layout.tab2_layout,container,false)
                radar = layout2.findViewById(R.id.tab2_radar)
                textView = layout2.findViewById(R.id.all_tv)
                initRadar()
                initPie()
                layout2.findViewById<ImageView>(R.id.statement).setOnClickListener {
                    Toast.makeText(layout2.context,"默认学分为1，综合评分为总平均分",Toast.LENGTH_SHORT).show()
                }
                container.addView(layout2)
                layout2
            }
            else -> {
                layout3 = LayoutInflater.from(context)
                    .inflate(R.layout.tab3_layout,container,false)
                initLine(layout3)
                container.addView(layout3)
                layout3
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
        line = v.findViewById(R.id.line_chart)
        line.titleColor = blue2
        line.setOnClickListener {
            Toast.makeText(context,"最后一个数据为下一次的预测排名",Toast.LENGTH_SHORT).show()
        }
        setInfoChart2Data()
    }

    //初始化雷达图
    private fun initRadar(){
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
            post{
                radar.animateXY(500,500,Easing.EaseInBounce)
            }
            webLineWidthInner = 0.7f
        }
        //数据
        setCourseChartData()
    }

    private fun setCourseChartData(){
        mv = RadarMarkerView(layout2.context,R.layout.radar_marker)
        mv.chartView =
        radar.apply {
            //clear()
            val entries = ArrayList<RadarEntry>()
            val entries2 = ArrayList<RadarEntry>()
            list.forEach {
                entries.add(RadarEntry(it.grade,it.name))
                entries2.add(RadarEntry(it.average,it.name))
            }

            val dataSet = RadarDataSet(entries, "个人得分").apply {
                color = blue
                fillColor = blue
                setDrawFilled(true)
            }

            val dataSet2 = RadarDataSet(entries2, "班级平均分").apply {
                color = red
                fillColor = red
                setDrawFilled(true)
            }

            val finalData = RadarData(dataSet,dataSet2).apply {
                setValueTextSize(8f)
                setDrawValues(false)
            }

            data = finalData
            data.setValueTextSize(10f)
            isClickable = true
            marker = mv
        }
    }

    private fun setInfoChart1Data(){
        //饼图数据
        pieList[0].centerText = "${info.rate1}%"
        pieList[1].centerText = "${info.rate2}%"
        pieList[2].centerText = "${info.rate3}%"
        pieList[3].centerText = "${info.rate4}%"
        val setArray = arrayListOf(
            PieDataSet(arrayListOf(PieEntry(info.rate1.toFloat()), PieEntry(100-info.rate1.toFloat())),"ALL"),
            PieDataSet(arrayListOf(PieEntry(info.rate2.toFloat()), PieEntry(100-info.rate2.toFloat())),"ALL"),
            PieDataSet(arrayListOf(PieEntry(info.rate3.toFloat()), PieEntry(100-info.rate3.toFloat())),"ALL"),
            PieDataSet(arrayListOf(PieEntry(info.rate4.toFloat()), PieEntry(100-info.rate4.toFloat())),"ALL"))
        var i = 0
        pieList.forEach{
            setArray[i].apply {
                setColors(blue2,grey)
                selectionShift = 3f
                yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
                setDrawValues(false)
                val data = PieData(this)
                data.setValueFormatter(PercentFormatter())
                it.data = data
            }
            it.post {
                it.animateXY(600,600)
            }

            i++
        }
    }

    //折线图数据
    private fun setInfoChart2Data(){
        tl1 = layout3.findViewById(R.id.tv_left1)
        tr1 = layout3.findViewById(R.id.tv_right1)
        tl2 = layout3.findViewById(R.id.tv_left2)
        tr2 = layout3.findViewById(R.id.tv_right2)
        //折线图数据
        val spf = context.getSharedPreferences("data",Context.MODE_PRIVATE)
        val list = ArrayList<Double>()
        var average = 0
        for (i in 0..3) {
            list.add(spf.getInt("rank${i+1}", -1).toDouble())
            average += list[i].toInt()
        }
        average /= 4
        data = LineGraphSeries(arrayOf(
            DataPoint(1.0,list[0]),
            DataPoint(2.0,list[1]),
            DataPoint(3.0,list[2]),
            DataPoint(4.0,list[3]),
            DataPoint(5.0,list.average())
        ))
        data.color = Color.parseColor("#4f7c93")
        data.isDrawDataPoints = true
        data.dataPointsRadius = 10f
        data.setAnimated(true)
        line.removeAllSeries()
        line.addSeries(data)

        tl1.post{
            tl1.text = info.rank.toString()
            tr1.text = Math.abs(info.compare).toString()
            val drawable: Drawable?
            if (info.compare > 0) {
                drawable = ContextCompat.getDrawable(context, R.drawable.up)
                drawable?.setBounds(0, 0, 50, 50)
            }
            else if(info.compare < 0) {
                drawable = ContextCompat.getDrawable(context, R.drawable.down)
                drawable?.setBounds(0, 0, 50, 50)
            }
            else {
                drawable = ContextCompat.getDrawable(context, R.drawable.flat)
                drawable?.setBounds(0,0,50,10)
            }
            tr1.setCompoundDrawables(null,null,drawable,null)
        }
    }

    private fun changeStatics(){
        tl1 = layout3.findViewById(R.id.tv_left1)
        tr1 = layout3.findViewById(R.id.tv_right1)
        tl2 = layout3.findViewById(R.id.tv_left2)
        tr2 = layout3.findViewById(R.id.tv_right2)
        tl1.post{
            tl1.text = info.rank.toString()
            tr1.text = Math.abs(info.compare).toString()
            val drawable: Drawable?
                if (info.compare > 0) {
                    drawable = ContextCompat.getDrawable(context, R.drawable.up)
                    drawable?.setBounds(0, 0, 50, 50)
                }
                else if(info.compare < 0) {
                    drawable = ContextCompat.getDrawable(context, R.drawable.down)
                    drawable?.setBounds(0, 0, 50, 50)
                }
                else {
                    drawable = ContextCompat.getDrawable(context, R.drawable.flat)
                    drawable?.setBounds(0,0,50,10)
                }
            tr1.setCompoundDrawables(null,null,drawable,null)

        }
    }

    //初始化饼图
    private fun initPie(){
        pieList.clear()
        pieList.apply {
            add(layout2.findViewById(R.id.rate1))
            add(layout2.findViewById(R.id.rate2))
            add(layout2.findViewById(R.id.rate3))
            add(layout2.findViewById(R.id.rate4))
        }
        //视图
        pieList.forEach {
            it.apply {
                isDrawHoleEnabled = true
                setHoleColor(Color.WHITE)
                isRotationEnabled = false
                isHighlightPerTapEnabled = true
                legend.isEnabled = false
                description.isEnabled = false
            }
        }
        //数据
        setInfoChart1Data()
        //综合评分
        textView.post {
            textView.text = info.average.toString()
        }

    }

    //初始化RecyclerView
    private fun initRecyclerView(v: View){
        rv = v.findViewById(R.id.tab1_rv)
        val layoutManager = LinearLayoutManager(v.context)
        rv.layoutManager = layoutManager
        rv.adapter = Tab1RVAdapter(list)
    }

    fun animateChart(){
        pieList.forEach {
            it.animateXY(600,600)
        }
        radar.animateXY(600,600)
    }

    fun setData(list: ArrayList<CourseBean>, info: InfoBean){
        this.list = list
        this.info = info
        initRadar()
        initPie()

    }

    fun setData2(){
        changeStatics()
    }
}