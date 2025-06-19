package com.example.finalproject

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.cardview.widget.CardView
import com.google.firebase.database.*

class AllMedicationsActivity : BaseActivity() {

    private lateinit var container: LinearLayout
    private lateinit var database: DatabaseReference

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
        setBaseContentLayout(R.layout.activity_all_meds)

        container = findViewById(R.id.allMedsContainer)
        database = FirebaseDatabase.getInstance().getReference("medications")

        loadAllMedicationNames()
    }

    private fun loadAllMedicationNames() {
        val pillIcons = listOf(
            R.drawable.pill1,
            R.drawable.pill2,
            R.drawable.pill3,
            R.drawable.pill4,
            R.drawable.pill5,
            R.drawable.pill6
        )

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()
                for (medSnap in snapshot.children) {
                    val med = medSnap.getValue(Medication::class.java)
                    if (med != null) {
                        val card = CardView(this@AllMedicationsActivity).apply {
                            radius = 24f
                            cardElevation = 6f
                            useCompatPadding = true
                            setCardBackgroundColor(Color.parseColor("#E8F0FE"))
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, 24)
                            }
                        }

                        val row = LinearLayout(this@AllMedicationsActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL or Gravity.START
                            layoutDirection = LinearLayout.LAYOUT_DIRECTION_LTR
                            setPadding(24, 24, 24, 24)
                        }

                        // קביעת אייקון לפי id
                        val iconIndex = kotlin.math.abs(med.id.hashCode()) % pillIcons.size
                        val selectedIcon = pillIcons[iconIndex]

                        val icon = ImageView(this@AllMedicationsActivity).apply {
                            setImageResource(selectedIcon)
                            layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                                marginEnd = 24
                            }
                        }

                        val nameText = TextView(this@AllMedicationsActivity).apply {
                            text = med.name
                            setTextColor(Color.BLACK)
                            setTextSize(18f)
                            setTypeface(null, Typeface.BOLD)
                            textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START
                        }

                        row.addView(icon)
                        row.addView(nameText)
                        card.addView(row)
                        container.addView(card)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AllMedicationsActivity, "Failed to load medications", Toast.LENGTH_SHORT).show()
            }
        })
        }}

