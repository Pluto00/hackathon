package com.example.fzugrade2.activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.example.fzugrade2.ActivityCollector
import com.example.fzugrade2.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


//展示wizard，从sharePreference获取上次登录的token
//如获取不到token，则进入登录界面
//获取token则进入主界面
class WelcomeActivity : AppCompatActivity() {

    private lateinit var uiHandler: Handler

    companion object {
        private val JSON = MediaType.parse("application/json; charset=utf-8")
        private const val loginUrl = "http://47.102.118.1:5050/api_1_0/login"
    }

    private var wizardState = true  //wizard显示不允许返回退出

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        //在LoginActivity结束该活动
        ActivityCollector.INSTANCE.addActivity(this)

        val data = getToken()

        //post延迟动作，关闭wizard
        uiHandler = Handler{
            val intent = Intent(this@WelcomeActivity,MainActivity::class.java)
            intent.putExtra("token",it.data.getString("token"))
            startActivity(intent)
            //淡出淡入进入活动
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            true
        }
        uiHandler.postDelayed({
            //根据token是否存在判断进入的界面
            if (data.isNullOrEmpty()){
                val intent = Intent(this@WelcomeActivity,LoginActivity::class.java)
                startActivity(intent)
            }
            else{
                enterWithToken()
            }
        }
        ,800)
    }

    private fun enterWithToken(){
        val client = OkHttpClient()
        val spf = getSharedPreferences("data", Context.MODE_PRIVATE)
        val id = spf.getString("id","")
        val pw = spf.getString("pw","")
        val json = "{\"student_number\":\"$id\",\"password\":\"$pw\"}"
        val requestBody = RequestBody.create(JSON,json)
        val request = Request.Builder()
            .url(loginUrl)
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                val jsonObject = JSONObject(body)
                val msg = Message()
                val bundle = Bundle()
                bundle.putString("token",jsonObject.getString("token"))
                msg.data = bundle
                uiHandler.sendMessage(msg)
            }
        })
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
