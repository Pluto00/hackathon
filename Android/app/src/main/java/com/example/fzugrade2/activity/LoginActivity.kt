package com.example.fzugrade2.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.Toast
import com.example.fzugrade2.ActivityCollector
import com.example.fzugrade2.R
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val loginUrl = "http://47.102.118.1:5050/api_1_0/login"
        private const val S_PW_WRONG = 203
        private const val S_NOT_EXIST = 401
        private const val S_NO_ACCESS = 205
        private const val S_SUCCESS = 200

        private val JSON = MediaType.parse("application/json; charset=utf-8")
    }


    private lateinit var uiHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_login)

        //在MainActivity结束该活动
        ActivityCollector.INSTANCE.finishAll()
        ActivityCollector.INSTANCE.addActivity(this)

        setListener()
        initHandler()
        setInfo()
    }

    private fun setInfo(){
        val data = getSharedPreferences("data", Context.MODE_PRIVATE)
        id_et.setText(data.getString("id",""))
        pw_et.setText(data.getString("pw",""))
        remember_cb.isChecked = data.getBoolean("check",false)

    }

    private fun initHandler(){
        uiHandler = Handler{
            //解析Json
            val jsonObject = JSONObject(it.data.getString("body"))
            val status = jsonObject.getInt("status")
            //根据返回json的status参数判断登录情况
            when (status){
                S_SUCCESS -> {
                    Toast.makeText(this,"登录成功",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,MainActivity::class.java)
                    //用于获取个人信息json的令牌参数
                    val token = jsonObject.getString("token")
                    intent.putExtra("token",token)
                    val spf = getSharedPreferences("data", Context.MODE_PRIVATE).edit()
                    spf.putString("token",token)
                        .putString("id",id_et.text.toString())
                    if(remember_cb.isChecked) {
                        spf.putBoolean("check",true)
                        spf.putString("pw", pw_et.text.toString())
                    }
                    else {
                        spf.putBoolean("check",false)
                        spf.remove("pw")
                    }
                    spf.apply()

                    startActivity(intent)
                }
                S_NOT_EXIST -> Toast.makeText(this,"用户不存在",Toast.LENGTH_SHORT).show()
                S_NO_ACCESS -> Toast.makeText(this,"权限不足",Toast.LENGTH_SHORT).show()
                S_PW_WRONG -> Toast.makeText(this,"密码错误",Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun setListener(){
        login_btn.setOnClickListener {
            when(""){
                id_et.text.toString() -> {
                    Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                pw_et.text.toString() -> {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else -> {
                    checkUserByJson(id_et.text.toString(), pw_et.text.toString())
                }
            }
        }

    }

    private fun checkUserByJson(id: String, pw: String){
        val client = OkHttpClient()
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
                val msg = Message()
                val bundle = Bundle()
                bundle.putString("body",body)
                msg.data = bundle
                //向uiHandler传递登录状态（将动作加入到主线程）
                uiHandler.sendMessage(msg)
            }
        })

    }



}
