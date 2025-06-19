package com.example.letmecook.adapter

import android.content.Context
import android.widget.ArrayAdapter

private val cities = arrayOf(
    "Denpasar", "Jakarta", "Jimbaran", "Pune", "Pattaya",
    "Nanjing", "New Delhi", "Nagoya", "Nha Trang", "Nakhon Ratchasima",
    "Manila", "Mumbai", "Mecca", "Medan", "Mandalay",
    "Taipei", "Tehran", "Tashkent", "Tianjin", "Thimphu",
    "Bangalore", "Bandra", "Banswara", "Banka", "Banshi",
    "Tokyo", "Tottori", "Toshima", "Tomioka", "Toyama",
    "Beijing", "Bangkok", "Baghdad", "Busan", "Baku",
    "Vientiane", "Varanasi", "Vladivostok", "Visakhapatnam", "Vellore",
    "Riyadh", "Rangoon", "Rawalpindi", "Rishikesh", "Ras Al Khaimah",
    "Shanghai", "Guangzhou", "Shenzhen", "Chongqing", "Chengdu",
    "Wuhan", "Hangzhou", "Xi'an", "Qingdao", "Suzhou",
    "Shenyang", "Harbin", "Dalian", "Zhengzhou", "Jinan",
    "Changsha", "Kunming", "Fuzhou", "Hefei", "Shijiazhuang",
    "Ürümqi", "Lanzhou", "Changchun", "Nanning", "Guiyang",
    "Xiamen", "Wenzhou", "Ningbo", "Taiyuan", "Tangshan",
    "Hohhot", "Nanchang", "Wuxi", "Dongguan", "Foshan",
    "Zibo", "Handan", "Baotou", "Luoyang", "Xuzhou",
    "Lhasa", "Macau", "Hong Kong",
    "Yokohama", "Osaka", "Sapporo", "Fukuoka", "Kobe",
    "Kyoto", "Kawasaki", "Saitama", "Hiroshima", "Sendai",
    "Kitakyushu", "Chiba", "Sakai", "Niigata", "Hamamatsu",
    "Kumamoto", "Sagamihara", "Okayama", "Shizuoka", "Kagoshima",
    "Kanazawa", "Nara", "Gifu", "Nagasaki", "Oita",
    "Seoul", "Incheon", "Daegu", "Daejeon", "Gwangju",
    "Suwon", "Ulsan", "Goyang", "Changwon", "Seongnam",
    "Cheongju", "Jeonju", "Pohang", "Gimhae", "Ansan",
    "New Taipei City", "Kaohsiung", "Taichung", "Tainan", "Taoyuan",
    "Hsinchu", "Keelung", "Chiayi", "Changhua",
)

class CitiesAdapter(
    context: Context,
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, cities)