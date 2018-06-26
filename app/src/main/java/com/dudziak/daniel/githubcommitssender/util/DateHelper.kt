package com.dudziak.daniel.githubcommitssender.util

import java.text.SimpleDateFormat
import java.util.*

class DateHelper(){
    companion object {
        fun stringToDate(date: String): Date {
            val tz = TimeZone.getTimeZone("UTC")
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            df.timeZone = tz
            return df.parse(date)
        }

        fun dateToString(date: Date): String {
            val tz = TimeZone.getTimeZone("UTC")
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            df.timeZone = tz
            return df.format(date)
        }
    }
}