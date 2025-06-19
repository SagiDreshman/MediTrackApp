package com.example.finalproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class EditMedicationActivity : AppCompatActivity() {

    private var selectedDate = ""
    private var selectedTime = ""
    private var medId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_medication)

        val nameEdit = findViewById<EditText>(R.id.nameEdit)
        val dosageEdit = findViewById<EditText>(R.id.dosageEdit)
        val timeButton = findViewById<Button>(R.id.timeButton)
        val dateButton = findViewById<Button>(R.id.dateButton)
        val updateButton = findViewById<Button>(R.id.updateButton)
        val deleteButton = findViewById<Button>(R.id.deleteButton)

        // קבלת נתונים מה-Intent
        medId = intent.getStringExtra("id") ?: return
        val name = intent.getStringExtra("name") ?: ""
        val dosage = intent.getStringExtra("dosage") ?: ""
        selectedTime = intent.getStringExtra("time") ?: ""
        selectedDate = intent.getStringExtra("date") ?: ""

        nameEdit.setText(name)
        dosageEdit.setText(dosage)
        timeButton.text = selectedTime
        dateButton.text = selectedDate

        timeButton.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                timeButton.text = selectedTime
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        dateButton.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = String.format("%02d/%02d/%04d", day, month + 1, year)
                dateButton.text = selectedDate
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        updateButton.setOnClickListener {
            val newName = nameEdit.text.toString().trim()
            val newDosage = dosageEdit.text.toString().trim()

            if (newName.isEmpty() || newDosage.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dbRef = FirebaseDatabase.getInstance().getReference("medications").child(medId)
            val updatedMed = mapOf(
                "name" to newName,
                "dosage" to newDosage,
                "date" to selectedDate,
                "time" to selectedTime
            )

            dbRef.updateChildren(updatedMed).addOnSuccessListener {
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            val dbRef = FirebaseDatabase.getInstance().getReference("medications").child(medId)
            dbRef.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Medication deleted", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
