package com.example.recipely.adapter

import android.content.Context
import android.widget.ArrayAdapter

val countries = arrayOf(
    "Nepal",
    "France",
    "USA",
    "Australia",
    "Canada",
    "India",
    "Japan",
    "Germany",
    "Italy",
    "Brazil"
)

class CountriesAdapter(
    context: Context,
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, countries)