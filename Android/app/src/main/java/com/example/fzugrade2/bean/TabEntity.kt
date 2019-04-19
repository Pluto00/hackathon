package com.example.fzugrade2.bean

import android.support.v7.widget.DialogTitle
import com.flyco.tablayout.listener.CustomTabEntity

class TabEntity(var title: String,
    var selectedIcon: Int, var unSelectedIcon: Int): CustomTabEntity{

    override fun getTabSelectedIcon(): Int {
        return selectedIcon
    }

    override fun getTabTitle(): String {
        return title
    }

    override fun getTabUnselectedIcon(): Int {
        return unSelectedIcon
    }
}