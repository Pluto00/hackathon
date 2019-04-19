package com.example.fzugrade2

import android.app.Activity

enum class ActivityCollector{
    //单例
    INSTANCE;
    var activitis = ArrayList<Activity>()

    fun addActivity(activity: Activity){
        activitis.add(activity)
    }

    fun removeActivity(activity: Activity){
        activitis.remove(activity)
    }

    fun finishAll(){
        activitis.forEach {
            it.finish()
        }
        activitis.clear()
    }
}