package com.example.finalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : BaseActivity() {

    private lateinit var planContainer: LinearLayout
    private lateinit var database: DatabaseReference
    private val medicationList = mutableListOf<Medication>()

    private val refreshHandler = Handler()
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadTodayMedications()
            refreshHandler.postDelayed(this, 10000)
        }
    }

    data class Medication(
        val id: String = "",
        val date: String = "",
        val time: String = "",
        val name: String = "",
        val dosage: String = "",
        val taken: Boolean = false
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContentLayout(R.layout.activity_home)

        val dateText: TextView = findViewById(R.id.dateText)
        val displayDate = SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH).format(Date())
        dateText.text = displayDate

        planContainer = findViewById(R.id.planContainer)
        database = FirebaseDatabase.getInstance().getReference("medications")

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    true
                }
                R.id.menu_add -> {
                    startActivityForResult(Intent(this, AddMedicationActivity::class.java), 100)
                    true
                }
                R.id.menu_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.menu_medications -> {
                    startActivity(Intent(this, AllMedicationsActivity::class.java))
                    true
                }
                R.id.menu_my_documents -> {
                    startActivity(Intent(this, MyDocumentsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        loadTodayMedications()
        refreshHandler.postDelayed(refreshRunnable, 10000)
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun loadTodayMedications() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date())
        val now = Calendar.getInstance().time
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicationList.clear()
                for (medSnap in snapshot.children) {
                    val med = medSnap.getValue(Medication::class.java)
                    if (med != null && med.date == today) {
                        val medDateTimeStr = "${med.date} ${med.time}"
                        try {
                            val medDateTime = format.parse(medDateTimeStr)
                            if (medDateTime != null && medDateTime.after(now)) {
                                medicationList.add(med.copy(id = medSnap.key ?: ""))
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
                updateMedicationDisplay()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            try {
                val name = data?.getStringExtra("name") ?: throw Exception("Missing name")
                val dosage = data.getStringExtra("dosage") ?: throw Exception("Missing dosage")
                val date = data.getStringExtra("date") ?: throw Exception("Missing date")
                val time = data.getStringExtra("time") ?: throw Exception("Missing time")

                val medId = database.push().key ?: throw Exception("Missing database key")
                val med = Medication(id = medId, date = date, time = time, name = name, dosage = dosage)
                database.child(medId).setValue(med)
            } catch (e: Exception) {
                Toast.makeText(this, "Error saving medication: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun updateMedicationDisplay() {
        planContainer.removeAllViews()

        val grouped = medicationList.groupBy { it.time }

        // רשימת התמונות מתיקיית res/drawable
        val pillIcons = listOf(
            R.drawable.pill1,
            R.drawable.pill2,
            R.drawable.pill3,
            R.drawable.pill4,
            R.drawable.pill5,
            R.drawable.pill6
        )

        for ((time, medsAtTime) in grouped) {
            val timeText = TextView(this).apply {
                text = time
                setTextColor(Color.BLACK)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 24, 0, 12)
            }
            planContainer.addView(timeText)

            for (med in medsAtTime) {
                val card = CardView(this).apply {
                    radius = 24f
                    cardElevation = 0f
                    setCardBackgroundColor(Color.parseColor("#F1F6FD"))
                    useCompatPadding = true
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 12
                    }
                    setOnClickListener {
                        val intent = Intent(this@HomeActivity, EditMedicationActivity::class.java).apply {
                            putExtra("id", med.id)
                            putExtra("name", med.name)
                            putExtra("dosage", med.dosage)
                            putExtra("time", med.time)
                            putExtra("date", med.date)
                        }
                        startActivityForResult(intent, 200)
                    }
                }

                // בחר תמונה רנדומלית מהמאגר
                val randomIcon = pillIcons.random()
                val image = ImageView(this).apply {
                    setImageResource(randomIcon)
                    layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                        marginEnd = 32
                    }
                }

                val medName = TextView(this).apply {
                    text = med.name
                    textSize = 16f
                    setTextColor(Color.BLACK)
                    setTypeface(null, Typeface.BOLD)
                }

                val medDosage = TextView(this).apply {
                    text = med.dosage
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                    setPadding(0, 4, 0, 0)
                }

                val textLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(medName)
                    addView(medDosage)
                }

                val contentLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(32, 24, 32, 24)
                    addView(image)
                    addView(textLayout)
                }

                card.addView(contentLayout)
                planContainer.addView(card)
            }
        }
    }

}
