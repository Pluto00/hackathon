package com.example.fzugrade2.activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.fzugrade2.ActivityCollector
import com.example.fzugrade2.R


//展示wizard，从sharePreference获取上次登录的token
//如获取不到token，则进入登录界面
//获取token则进入主界面
class WelcomeActivity : AppCompatActivity() {

    private lateinit var uiHandler: Handler

    private var wizardState = true  //wizard显示不允许返回退出

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        //在LoginActivity结束该活动
        ActivityCollector.INSTANCE.addActivity(this)

        val data = getToken()

        //post延迟动作，关闭wizard
        uiHandler = Handler()
        uiHandler.postDelayed({
            if (data.isNullOrEmpty()){
                val intent = Intent(this@WelcomeActivity,LoginActivity::class.java)
                startActivity(intent)

            }
            else{
                val intent = Intent(this@WelcomeActivity,MainActivity::class.java)
                intent.putExtra("token",data)
                startActivity(intent)
            }
            //淡出淡入进入活动
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
        ,1000)
    }



    private fun getToken(): String?{
        val editor = getSharedPreferences("data", Context.MODE_PRIVATE)
        return editor.getString("token","")
    }

    override fun onBackPressed() {
        if (!wizardState)
            super.onBackPressed()
    }
}
