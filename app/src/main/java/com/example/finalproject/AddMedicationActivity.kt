package com.example.finalproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.*
import java.util.*

class AddMedicationActivity : BaseActivity() {

    private lateinit var nameEdit: EditText
    private lateinit var dosageEdit: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var addButton: Button

    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContentLayout(R.layout.activity_add_medication)

        nameEdit = findViewById(R.id.nameEdit)
        dosageEdit = findViewById(R.id.dosageEdit)
        dateButton = findViewById(R.id.dateButton)
        timeButton = findViewById(R.id.timeButton)
        addButton = findViewById(R.id.addButton)

        dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                selectedDate = String.format("%02d/%02d/%04d", d, m + 1, y)
                dateButton.text = selectedDate
            }, year, month, day).show()
        }

        timeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, h, m ->
                selectedTime = String.format("%02d:%02d", h, m)
                timeButton.text = selectedTime
            }, hour, minute, true).show()
        }

        addButton.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val dosage = dosageEdit.text.toString().trim()

            if (name.isEmpty() || dosage.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ➕ מוסיפים ליומן ברגע שהתרופה מתווספת
            addEventToCalendar(name, dosage, selectedDate, selectedTime)

            // החזרת תוצאה לפעילות הקוראת (HomeActivity)
            val intent = Intent().apply {
                putExtra("name", name)
                putExtra("dosage", dosage)
                putExtra("date", selectedDate)
                putExtra("time", selectedTime)
            }

            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun addEventToCalendar(name: String, dosage: String, date: String, time: String) {
        val cal = Calendar.getInstance()
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
        val datetime = sdf.parse("$date $time") ?: return
        cal.time = datetime

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Take $name")
            putExtra(CalendarContract.Events.DESCRIPTION, "Dosage: $dosage")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 15 * 60 * 1000)
            putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            putExtra(CalendarContract.Reminders.MINUTES, 0)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No calendar app found", Toast.LENGTH_SHORT).show()
        }
    }
}
