package com.example.fzugrade2.activity

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import com.example.fzugrade2.ActivityCollector
import com.example.fzugrade2.R
import com.example.fzugrade2.adapter.MyPagerAdapter
import com.example.fzugrade2.adapter.Tab1RVAdapter
import com.example.fzugrade2.bean.CourseBean
import com.example.fzugrade2.bean.InfoBean
import com.example.fzugrade2.bean.TabEntity
import com.example.fzugrade2.manager.JsonManager
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
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
            Color.parseColor("#549f8b"),
            Color.parseColor("#e58a55"),
            Color.parseColor("#4f7c93")
        )
        private const val monitorId = "031799101"
    }

    private var uiHandler: Handler? = null

    private lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
    private var colorEvaluator = ArgbEvaluator()
    private var animState = true
    private var token: String? = null
    private lateinit var pagerAdapter: MyPagerAdapter
    private var jsonManager = JsonManager()
    private var courseList = ArrayList<CourseBean>()
    private lateinit var infoBean: InfoBean

    private lateinit var termPopup: PopupWindow
    private lateinit var termLayout: View

    private var firstShow1 = true
    private var term = 4
    private var isFirst = true
    private var isTow = true
    var isMonitor = false
    var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //销毁LoginActivity
        ActivityCollector.INSTANCE.finishAll()
        id = getSharedPreferences("data", Context.MODE_PRIVATE).getString("id","")
        isMonitor = id == monitorId
        token = intent.getStringExtra("token")

        uiHandler = Handler()

        initToolbar()
        initWidgets()
        initTab()
        setToolbarHeight()
        //数据
        initData()
        //initListForTest()
        initManager()
        initPopupWindow()
    }

    private fun initData(){
        jsonManager.getInfo(4,token)
    }

    private fun initManager(){
        jsonManager.setOnGetCourseListener(object: JsonManager.OnGetCourseListener{
            override fun onGetCourse(list: ArrayList<CourseBean>) {
                //得到课程分数
                courseList.clear()
                courseList.addAll(list)
                if (isFirst) {
                    uiHandler?.post {
                        initViewPager()
                        initDrawer(isMonitor)
                    }
                    isFirst = false
                }else{
                    pagerAdapter.setData(courseList,infoBean)
                    if(!isTow)
                        pagerAdapter.setData2()
                }
                refreshList()
            }
        })
        jsonManager.setOnGetInfoListener(object: JsonManager.OnGetInfoListener{
            override fun onGetInfo(info: InfoBean) {
                //得到信息
                infoBean = info
                if (isFirst) {
                    uiHandler?.post {
                        tv_title.text = infoBean.name
                    }
                }
                jsonManager.getCourse(term,token)
            }
        })
    }

    private fun initPopupWindow(){
        @SuppressLint("InflateParams")
        termLayout = LayoutInflater.from(this).inflate(R.layout.tv_layout,null,false)
        termPopup = PopupWindow(termLayout,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,true)
        termPopup.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_bg))
        val animator2 = ObjectAnimator.ofFloat(arrow_image,"rotation",180f,0f)
        animator2.duration = 300
        termPopup.setOnDismissListener {
            animator2.start()
        }

        termLayout.findViewById<TextView>(R.id.term1_tv)
            .setOnClickListener {
                term_tv.text = "大一上学期"
                term = 1
                jsonManager.getInfo(1,token)
                termPopup.dismiss()
            }

        termLayout.findViewById<TextView>(R.id.term2_tv)
            .setOnClickListener {
                term_tv.text = "大一下学期"
                term = 2
                jsonManager.getInfo(2,token)
                termPopup.dismiss()
            }

        termLayout.findViewById<TextView>(R.id.term3_tv)
            .setOnClickListener {
                term_tv.text = "大二上学期"
                term = 3
                jsonManager.getInfo(3,token)
                termPopup.dismiss()
            }

        termLayout.findViewById<TextView>(R.id.term4_tv)
            .setOnClickListener {
                term_tv.text = "大二下学期"
                term = 4
                jsonManager.getInfo(4,token)
                termPopup.dismiss()
            }
    }

    private fun setToolbarHeight(){
        toolbar.viewTreeObserver.addOnGlobalLayoutListener (object :ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                toolbar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                toolbar.layoutParams.height = toolbar.measuredHeight + getStatusBarHeight()
            }
        })

    }

    private fun getStatusBarHeight(): Int{
        return resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height","dimen","android")
        )

    }

    override fun onBackPressed() {
        if(drawer.isDrawerOpen)
            drawer.closeDrawer()
        else
            super.onBackPressed()
    }

    //初始化抽屉
    private fun initDrawer(isMonitor: Boolean){
        header = AccountHeaderBuilder()
            .withActivity(this)
            .withCompactStyle(true)
            .addProfiles(
                ProfileDrawerItem().withName(infoBean.name).withEmail(id).withIcon(R.drawable.avatar)
            )
            .withSelectionListEnabled(false)
            .withHeaderBackground(R.color.material_drawer_dark_background)
            .withHeightDp(90)
            .build()

        drawer = DrawerBuilder().run {
            withActivity(this@MainActivity)
            withAccountHeader(header)
            if(isMonitor){
                addDrawerItems(SwitchDrawerItem()
                    .withName("班级模式")
                    .withSelectable(false)
                    .withIcon(R.drawable.classmode)
                    .withSelectedColorRes(R.color.material_drawer_dark_background)
                    .withSelectedTextColorRes(R.color.colorTabUnselected)
                    .withTextColorRes(R.color.colorTabUnselected))
                    .withOnDrawerItemClickListener { _, _, _ ->

                        true
                    }
            }
            addDrawerItems(
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
                    .withOnDrawerItemClickListener{_,_,_->
                        val intent = Intent(this@MainActivity,AboutActivity::class.java)
                        startActivity(intent)

                        return@withOnDrawerItemClickListener true
                    }

            )
            addStickyDrawerItems(
                SecondaryDrawerItem().withName("退出当前账号").withSetSelected(true)
                    .withSelectedColorRes(R.color.material_drawer_dark_background)
                    .withOnDrawerItemClickListener { _, _, _ ->
                        //删除token，开启登录界面，将结束该活动
                        getSharedPreferences("data", Context.MODE_PRIVATE).edit().remove("token").apply()
                        ActivityCollector.INSTANCE.addActivity(this@MainActivity)
                        val intent = Intent(this@MainActivity,LoginActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
                        return@withOnDrawerItemClickListener true
                    }
            )
            withSliderBackgroundColorRes(R.color.material_drawer_dark_background)
            build()
        }

    }

    //初始化Toolbar
    private fun initToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        menu.setOnClickListener {
            drawer.openDrawer()
        }
    }

    private fun initViewPager(){
        pagerAdapter = MyPagerAdapter(pageCount,this, courseList, infoBean)
        view_pager.adapter = pagerAdapter
        view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                if (animState)
                    toolbar_bg.setBackgroundColor(
                        colorEvaluator.evaluate(p1, themeColors[p0],
                            themeColors[(p0+1)% pageCount]) as Int
                    )
            }

            override fun onPageSelected(p0: Int) {
                //改变当前tab
                tab.currentTab = p0
                if(firstShow1 && p0 == 1){
                    pagerAdapter.animateChart()
                    firstShow1 = false
                }
                if(isTow && p0 == 2){
                    pagerAdapter.setData2()
                    isTow = false
                }
            }
        })
    }

    //初始化各种控件
    private fun initWidgets(){
        //spinner触发器，箭头图标的动画
        val animator = ObjectAnimator.ofFloat(arrow_image,"rotation",0f,180f)
        animator.duration = 200
        term_layout.setOnClickListener {
            termPopup.showAsDropDown(it,0,0,Gravity.CENTER)
            animator.start()
        }


    }

    private fun initTab(){
        mTabList.clear()
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

    private fun refreshList(){
        uiHandler?.post {
            ((view_pager.adapter as MyPagerAdapter).rv.adapter as Tab1RVAdapter)
                .setList(courseList)
                .notifyDataSetChanged()
        }
    }

}
