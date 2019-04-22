package com.example.fzugrade2.manager

import com.example.fzugrade2.bean.CourseBean
import com.example.fzugrade2.bean.InfoBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class JsonManager(){

    companion object {
        private val JSON = MediaType.parse("application/json; charset=utf-8")
        private const val courseUrl = "http://47.102.118.1:5050/api_1_0/get_mark"
        private const val infoUrl = "http://47.102.118.1:5050/api_1_0/get_info"
    }

    private var mCourseListener: OnGetCourseListener? = null
    private var mInfoListener: OnGetInfoListener? = null

    fun getCourse(term: Int, token: String?){
        val json = "{\"term\":$term,\"token\":\"$token\"}"
        val requestBody = RequestBody.create(JSON,json)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(courseUrl)
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                val beanList: ArrayList<CourseBean> = Gson().fromJson(body)
                mCourseListener?.onGetCourse(beanList)
            }
        })
    }

    fun getInfo(term: Int, token: String?){
        val json = "{\"term\":$term,\"token\":\"$token\"}"
        val requestBody = RequestBody.create(JSON,json)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(infoUrl)
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val bean = Gson().fromJson(response.body()?.string(),InfoBean::class.java)
                mInfoListener?.onGetInfo(bean)
            }
        })
    }

    fun setOnGetCourseListener(listener: OnGetCourseListener){
        mCourseListener = listener
    }

    fun setOnGetInfoListener(listener: OnGetInfoListener){
        mInfoListener = listener
    }


    interface OnGetCourseListener{
        fun onGetCourse(list: ArrayList<CourseBean>)
    }

    interface OnGetInfoListener{
        fun onGetInfo(info: InfoBean)
    }

    private inline fun <reified T> Gson.fromJson(json: String?) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
}