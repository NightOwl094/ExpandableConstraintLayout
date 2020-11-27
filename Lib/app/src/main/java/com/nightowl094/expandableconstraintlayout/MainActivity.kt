package com.nightowl094.expandableconstraintlayout

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.nightowl094.lib.OnLayoutStateChangeListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testc1.onLayoutStateChangeListener = object : OnLayoutStateChangeListener {
            override fun onAnimationStart() {
                Log.d("ttt", "onAnimationStart: #1")
            }

            override fun onAnimation() {
                Log.d("ttt", "onAnimation: #2")
            }

            override fun onAnimationEnd() {
                Log.d("ttt", "onAnimationEnd: #3")
                Log.d("ttt", "onAnimationEnd: #4 ${testc1.isExpanded}")
            }
        }

        testc1.setOnClickListener {
            testc1.toggleLayout()
        }

        openBtn.setOnClickListener {
            testc1.expandLayout()
        }

        closeBtn.setOnClickListener {
            testc1.foldLayout()
        }

        expand800Btn.setOnClickListener {
            testc1.expandLayout(800)
        }

        fold400Btn.setOnClickListener {
            testc1.foldLayout(400)
        }

        toggleBtn.setOnClickListener {
            testc1.toggleLayout()
        }

        foldTargetTextView2.setOnClickListener {
            testc1.foldLayoutById(R.id.testt2)
        }

        invalidate.setOnClickListener {
            testc1.invalidateLayout() {
                Log.d("ttt", "onCreate: is invalidated")
            }
        }

    }
}