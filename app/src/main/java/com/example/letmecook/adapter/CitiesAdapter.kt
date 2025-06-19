package com.example.recipely.adapter

import android.content.Context
import android.widget.ArrayAdapter

private val cities = arrayOf(
    "Kathmandu", "Kavre", "Kalaiya", "Kakarvitta", "Kalanki",
    "Paris", "Pau", "Pantin", "Parthenay", "Palaiseau",
    "New York", "New Orleans", "New Haven", "New Jersey", "Newport",
    "Melbourne", "Melton", "Melrose", "Melville", "Melba",
    "Toronto", "Torbay", "Torrington", "Torquay", "Torkelton",
    "Bangalore", "Bandra", "Banswara", "Banka", "Banshi",
    "Tokyo", "Tottori", "Toshima", "Tomioka", "Toyama",
    "Berlin", "Bergheim", "Bernau", "Berchtesgaden", "Bersenbrück",
    "Venice", "Ventimiglia", "Venezia", "Venafro", "Veneziano",
    "Rio de Janeiro", "Rio Claro", "Rio Grande", "Rio Branco", "Rio Azul"
)

class CitiesAdapter(
    context: Context,
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, cities)