package com.example.finalproject

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : BaseActivity() {

    private lateinit var historyContainer: LinearLayout
    private lateinit var database: DatabaseReference
    private val medications = mutableListOf<Medication>()

    data class Medication(
        val id: String = "",
        val date: String = "",
        val time: String = "",
        val name: String = "",
        val dosage: String = "",
        val taken: Boolean = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContentLayout(R.layout.activity_history)

        historyContainer = findViewById(R.id.historyContainer)
        database = FirebaseDatabase.getInstance().getReference("medications")

        loadPastMedications()
    }

    private fun loadPastMedications() {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
        val now = Calendar.getInstance().time

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medications.clear()
                for (medSnap in snapshot.children) {
                    val med = medSnap.getValue(Medication::class.java)
                    if (med != null) {
                        val medDateTimeStr = "${med.date} ${med.time}"
                        try {
                            val medDateTime = format.parse(medDateTimeStr)
                            if (medDateTime != null && medDateTime.before(now)) {
                                medications.add(med.copy(id = medSnap.key ?: ""))
                            }
                        } catch (e: Exception) {
                            Log.e("ParseError", "Could not parse: $medDateTimeStr")
                        }
                    }
                }
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI() {
        historyContainer.removeAllViews()

        val pillIcons = listOf(
            R.drawable.pill1,
            R.drawable.pill2,
            R.drawable.pill3,
            R.drawable.pill4,
            R.drawable.pill5,
            R.drawable.pill6
        )

        for (med in medications) {
            val card = CardView(this).apply {
                radius = 24f
                cardElevation = 4f
                setContentPadding(24, 24, 24, 24)
                useCompatPadding = true
                setCardBackgroundColor(Color.parseColor("#E3F2FD"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 24)
                }
            }

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.START
            }

            // קביעת אייקון לפי מזהה התרופה (id)
            val iconIndex = kotlin.math.abs(med.id.hashCode()) % pillIcons.size
            val selectedIcon = pillIcons[iconIndex]

            val image = ImageView(this).apply {
                setImageResource(selectedIcon)
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    marginEnd = 16
                }
            }

            val nameText = TextView(this).apply {
                text = med.name
                setTextColor(Color.BLACK)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            }

            val dosageText = TextView(this).apply {
                text = med.dosage
                setTextColor(Color.DKGRAY)
                textSize = 14f
            }

            val nameDosageLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                addView(nameText)
                addView(dosageText)
            }

            val nameRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(image)
                addView(nameDosageLayout)
            }

            val dateTimeText = TextView(this).apply {
                text = "${med.date} ${med.time}"
                setTextColor(Color.GRAY)
                textSize = 12f
                setPadding(0, 8, 0, 0)
            }

            val statusButton = Button(this).apply {
                text = if (med.taken) "Taken" else "Not Taken"
                setBackgroundColor(if (med.taken) Color.parseColor("#A5D6A7") else Color.parseColor("#EF9A9A"))
                setTextColor(Color.BLACK)
                setOnClickListener {
                    val updated = med.copy(taken = !med.taken)
                    database.child(med.id).setValue(updated)
                }
            }

            row.addView(nameRow)
            row.addView(dateTimeText)
            row.addView(statusButton)

            card.addView(row)
            historyContainer.addView(card)
        }
    }

}
