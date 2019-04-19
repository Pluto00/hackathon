package com.example.fzugrade2.activity

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.transition.Fade
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.fzugrade2.ActivityCollector
import com.example.fzugrade2.R
import com.example.fzugrade2.adapter.MyPagerAdapter
import com.example.fzugrade2.bean.TabEntity
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SwitchDrawerItem


import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val pageCount = 3  //pager数量
        private val mTabList = ArrayList<CustomTabEntity>()
        private val themeColors = listOf(
            Color.parseColor("#3371d8"),
            Color.parseColor("#cc5e50"),
            Color.parseColor("#058700")
        )
        private val infoUrl = "http://47.102.118.1:5050/api_1_0/get_info"
    }


    private lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
    private var colorEvaluator = ArgbEvaluator()
    private var animState = true
    private lateinit var pagerAdapter: MyPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //取消状态栏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        //销毁LoginActivity
        ActivityCollector.INSTANCE.finishAll()
        initToolbar()
        initWidgets()
        initTab()
        initDrawer()
    }

    override fun onBackPressed() {
        if(drawer.isDrawerOpen)
            drawer.closeDrawer()
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> {
                drawer.openDrawer()
            }
        }
        return true
    }

    //初始化抽屉
    private fun initDrawer(){
        header = AccountHeaderBuilder()
            .withActivity(this)
            .withCompactStyle(true)
            .addProfiles(
                ProfileDrawerItem().withName("陈吃吃").withEmail("221801122").withIcon(R.drawable.avatar)
            )
            .withSelectionListEnabled(false)
            .withHeaderBackground(R.color.material_drawer_dark_background)
            .withHeightDp(90)
            .build()

        drawer = DrawerBuilder()
            .withActivity(this)
            .withAccountHeader(header)
            .addDrawerItems(
                SwitchDrawerItem().withName("班级模式").withSelectable(false)
                    .withIcon(R.drawable.classmode)
                    .withSelectedColorRes(R.color.material_drawer_dark_background)
                    .withSelectedTextColorRes(R.color.colorTabUnselected)
                    .withTextColorRes(R.color.colorTabUnselected),
                SwitchDrawerItem().withName("接收成绩通知").withSelectable(false)
                    .withIcon(R.drawable.inform)
                    .withSelectedColorRes(R.color.material_drawer_dark_background)
                    .withSelectedTextColorRes(R.color.colorTabUnselected)
                    .withTextColorRes(R.color.colorTabUnselected),
                SwitchDrawerItem().withName("颜色变换").withSelectable(false)
                    .withIcon(R.drawable.color)
                    .withSelectedColorRes(R.color.material_drawer_dark_background)
                    .withSelectedTextColorRes(R.color.colorTabUnselected)
                    .withTextColorRes(R.color.colorTabUnselected)
                    .withChecked(true)
                    .withOnCheckedChangeListener{_,_,_->
                        animState = !animState
                    },
                PrimaryDrawerItem().withName("意见反馈").withSelectable(false)
                    .withIcon(R.drawable.feedback)
                    .withSelectedColorRes(R.color.material_drawer_dark_background)
                    .withSelectedTextColorRes(R.color.colorTabUnselected)
                    .withTextColorRes(R.color.colorTabUnselected),
                PrimaryDrawerItem().withName("检查新版本").withSelectable(false)
                    .withIcon(R.drawable.upgrade)
                    .withSelectedColorRes(R.color.material_drawer_dark_background)
                    .withSelectedTextColorRes(R.color.colorTabUnselected)
                    .withTextColorRes(R.color.colorTabUnselected),
                PrimaryDrawerItem().withName("关于").withSelectable(false)
                    .withIcon(R.drawable.about)
                    .withSelectedColorRes(R.color.material_drawer_dark_background)
                    .withSelectedTextColorRes(R.color.colorTabUnselected)
                    .withTextColorRes(R.color.colorTabUnselected)

            )
            .addStickyDrawerItems(
                SecondaryDrawerItem().withName("退出当前账号").withSetSelected(true)
                    .withSelectedColorRes(R.color.material_drawer_dark_background)
                    .withOnDrawerItemClickListener { _, _, _ ->
                        //删除token，开启登录界面，将结束该活动
                        getSharedPreferences("data", Context.MODE_PRIVATE).edit().remove("token").apply()
                        ActivityCollector.INSTANCE.addActivity(this)
                        val intent = Intent(this,LoginActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
                        return@withOnDrawerItemClickListener true
                    }
            )
            .withSliderBackgroundColorRes(R.color.material_drawer_dark_background)
            .build()
    }

    //初始化Toolbar
    private fun initToolbar(){
        setSupportActionBar(toolbar)
        val actionbar =  supportActionBar
        val drawable = ContextCompat.getDrawable(this,R.drawable.menu)
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(drawable)
    }

    //初始化各种控件
    private fun initWidgets(){
        //spinner触发器，箭头图标的动画
        val animator = ObjectAnimator.ofFloat(arrow_image,"rotation",0f,180f)
        val animator2 = ObjectAnimator.ofFloat(arrow_image,"rotation",180f,0f)
        animator.duration = 200
        animator2.duration = 200
        var orientation = true
        arrow_image.setOnClickListener{
            if(orientation)
                animator.start()
            else
                animator2.start()
            orientation = !orientation
        }

        pagerAdapter = MyPagerAdapter(pageCount,this)
        view_pager.adapter = pagerAdapter

        view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                if (animState)
                    toolbar_bg.setBackgroundColor(colorEvaluator.evaluate(p1, themeColors[p0], themeColors[(p0+1)% pageCount]) as Int)
            }

            override fun onPageSelected(p0: Int) {
                //改变当前tab
                tab.currentTab = p0
                if(p0 == 1){
                    pagerAdapter.pie1.animateXY(1000,1000)
                    pagerAdapter.pie2.animateXY(1000,1000)
                    pagerAdapter.pie3.animateXY(1000,1000)
                    pagerAdapter.pie4.animateXY(1000,1000)
                    pagerAdapter.radar.animateXY(1000,1000)
                }

            }
        })
    }

    private fun initTab(){
        mTabList.apply {
            add(TabEntity(resources.getString(R.string.tab1),R.drawable.tab1,R.drawable.tab1_unselected))
            add(TabEntity(resources.getString(R.string.tab2),R.drawable.tab2,R.drawable.tab2_unselected))
            add(TabEntity(resources.getString(R.string.tab3),R.drawable.tab3,R.drawable.tab3_unselected))
        }

        tab.setTabData(mTabList)

        tab.setOnTabSelectListener(object: OnTabSelectListener{
            override fun onTabReselect(position: Int) {

            }
            override fun onTabSelect(position: Int) {
                //TODO: indicator无动画bug
                view_pager.currentItem = position
            }
        })
    }

    override fun onDestroy() {
        tab.setTabData(null)
        super.onDestroy()
    }
}
