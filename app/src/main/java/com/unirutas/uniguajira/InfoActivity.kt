package com.unirutas.uniguajira

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
    }

    fun facebook(view: View){
        val openUrl = Intent(Intent.ACTION_VIEW)
        openUrl.data = Uri.parse("https://www.facebook.com/wilmoscote")
        startActivity(openUrl)
    }

    fun instagram(view: View){
        val openUrl = Intent(Intent.ACTION_VIEW)
        openUrl.data = Uri.parse("https://www.instagram.com/wilmoscote/")
        startActivity(openUrl)
    }

    fun facebookJuanma(view: View){
        val openUrl = Intent(Intent.ACTION_VIEW)
        openUrl.data = Uri.parse("https://www.facebook.com/JuanMaQ7")
        startActivity(openUrl)
    }

    fun instagramJuanma(view: View){
        val openUrl = Intent(Intent.ACTION_VIEW)
        openUrl.data = Uri.parse("https://www.instagram.com/manuel.q7/")
        startActivity(openUrl)
    }
}
