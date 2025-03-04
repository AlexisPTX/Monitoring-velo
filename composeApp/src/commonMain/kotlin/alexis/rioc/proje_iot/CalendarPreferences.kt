package alexis.rioc.proje_iot

import com.russhwolf.settings.Settings

class CalendarPreferences(private val settings: Settings) {

    private val PAST_DAYS_KEY = "past_days"
    private val FUTURE_DAYS_KEY = "future_days"

    fun saveDays(pastDays: List<String>, futureDays: List<String>) {
        settings.putString(PAST_DAYS_KEY, pastDays.joinToString(","))
        settings.putString(FUTURE_DAYS_KEY, futureDays.joinToString(","))
    }

    fun getPastDays(): List<String> {
        val data = settings.getString(PAST_DAYS_KEY, "")
        return if (data.isNotBlank()) data.split(",") else emptyList()
    }

    fun getFutureDays(): List<String> {
        val data = settings.getString(FUTURE_DAYS_KEY, "")
        return if (data.isNotBlank()) data.split(",") else emptyList()
    }
}



