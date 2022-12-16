package com.notes.colornotes.fragments.allnotes.bottom.calender

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.notes.colornotes.R
import com.shuhart.materialcalendarview.MaterialCalendarView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CalenderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender)

        val calender = findViewById<MaterialCalendarView>(R.id.calendarView)
        val sysCalendar = Calendar.getInstance()
        calender.setSelectedDate(sysCalendar.time)
        calender.selectionColor = resources.getColor(
            R.color.color_main
        )

        getDate()

        findViewById<ImageView>(R.id.imgGoBackWeb).setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun getDate(){
        val locale = Locale("en", "UK")
        val dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)
        val date: String = dateFormat.format(Date())
        val txtCurrentDate =  findViewById<TextView>(R.id.txtCurrentDate)
        txtCurrentDate.text = date
    }

}