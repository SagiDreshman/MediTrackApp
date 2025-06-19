package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    fun setBaseContentLayout(layoutResId: Int) {
        super.setContentView(R.layout.activity_base_with_nav)

        // מוסיף את ה-layout הספציפי של ה-Activity הנוכחי
        val container = findViewById<FrameLayout>(R.id.base_content)
        layoutInflater.inflate(layoutResId, container, true)

        setupBottomNavigation(this)
    }

    private fun setupBottomNavigation(currentActivity: AppCompatActivity) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    if (currentActivity !is HomeActivity) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.menu_add -> {
                    if (currentActivity !is AddMedicationActivity) {
                        startActivity(Intent(this, AddMedicationActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.menu_history -> {
                    if (currentActivity !is HistoryActivity) {
                        startActivity(Intent(this, HistoryActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.menu_medications -> {
                    if (currentActivity !is AllMedicationsActivity) {
                        startActivity(Intent(this, AllMedicationsActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.menu_my_documents -> {
                    if (currentActivity !is MyDocumentsActivity) {
                        startActivity(Intent(this, MyDocumentsActivity::class.java))
                        finish()
                    }
                    true
                }
                else -> false
            }
        }

        // סימון הפריט הפעיל
        val selectedItem = when (currentActivity) {
            is HomeActivity -> R.id.menu_home
            is AddMedicationActivity -> R.id.menu_add
            is HistoryActivity -> R.id.menu_history
            is AllMedicationsActivity -> R.id.menu_medications
            is MyDocumentsActivity -> R.id.menu_my_documents
            else -> R.id.menu_home
        }
        bottomNav.selectedItemId = selectedItem
    }
}
