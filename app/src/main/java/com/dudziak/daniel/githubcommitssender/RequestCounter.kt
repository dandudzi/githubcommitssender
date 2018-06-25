package com.dudziak.daniel.githubcommitssender

import android.content.SharedPreferences
import android.support.v7.app.ActionBar

class RequestCounter(private val sharedPref: SharedPreferences) {
    private var counter: Int = 0
    private var timestamp: Long = 0

    var requestsPerPeriodTime = 60
        set(value) {
            if (value > 0)
                field = value
        }

    companion object {
        private const val COUNTER = "COUNTER_VALUE"
        private const val TIMESTAMP = "TIMESTAMP"
        private const val TIME_PERIOD = 3600
    }

    init {
        counter = sharedPref.getInt(COUNTER, counter)
        timestamp = sharedPref.getLong(TIMESTAMP, timestamp)
        if (timestamp == 0L)
            timestamp = getTimestamp()
    }

    private fun getTimestamp() = System.currentTimeMillis() / 1000L

    fun persist() {
        with(sharedPref.edit()) {
            putLong(TIMESTAMP, timestamp)
            putInt(COUNTER, counter)
            commit()
        }
    }

    override fun toString(): String {
        val tmp = if (requestsPerPeriodTime - counter > 0) requestsPerPeriodTime - counter else 0
        return "Only left $tmp requests"
    }

    fun addOneRequest() {
        checkTimePeriod()
        counter++
    }

    private fun checkTimePeriod() {
        if (getTimestamp() - timestamp >= TIME_PERIOD){
            timestamp = getTimestamp()
            counter = 0
        }
    }

    class ActionBarTitleRefresher(
        private val supportActionBar: ActionBar?,
        private val requestCounter: RequestCounter?
    ) {
        fun refreshActionBarTitle() {
            requestCounter!!.checkTimePeriod()
            supportActionBar!!.title = requestCounter.toString()
        }
    }
}