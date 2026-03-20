package com.example.anatronics

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var neutralButton: Button
    private lateinit var smileyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        neutralButton = findViewById(R.id.neutralButton)
        smileyButton = findViewById(R.id.smileyButton)

        neutralButton.setOnClickListener {
            val intent = Intent(this, NeutralFrontalCameraActivity::class.java)
            startActivity(intent)
        }

        // Uncomment once you have smiley camera ready
        // smileyButton.setOnClickListener {
        //     val intent = Intent(this, SmileyFrontalCameraActivity::class.java)
        //     startActivity(intent)
        // }
    }
}