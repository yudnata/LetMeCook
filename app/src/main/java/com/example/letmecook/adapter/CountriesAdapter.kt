package com.example.letmecook.adapter

import android.content.Context
import android.widget.ArrayAdapter

val countries = arrayOf(
    "Indonesia",
    "Malaysia",
    "China",
    "Thailand",
    "Australia",
    "Philippines",
    "India",
    "Japan",
    "South Korea",
    "Vietnam"
)

class CountriesAdapter(
    context: Context,
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, countries)