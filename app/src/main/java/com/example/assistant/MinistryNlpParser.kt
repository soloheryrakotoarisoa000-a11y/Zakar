package com.example.assistant

import com.example.localization.AppLanguage
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class ParsedTaskIntent(
    val title: String,
    val categoryKey: String,
    val dateTimeMillis: Long?,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val repeatType: String,
    val missingField: MissingField = MissingField.NONE
)

enum class MissingField {
    NONE,
    DATE,
    TIME
}

object MinistryNlpParser {

    fun parse(input: String, language: AppLanguage): ParsedTaskIntent {
        val lower = input.lowercase(Locale.ROOT).trim()

        val categoryKey = detectCategory(lower)
        val repeatType = detectRecurrence(lower)
        val dateResult = extractDate(lower)
        val timeResult = extractTime(lower)

        val cleanTitle = cleanTaskTitle(input)

        val hasDate = dateResult != null
        val hasTime = timeResult != null

        val finalMillis: Long? = if (hasDate && hasTime) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = dateResult!!
                set(Calendar.HOUR_OF_DAY, timeResult!!.first)
                set(Calendar.MINUTE, timeResult.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        } else if (hasDate && !hasTime) {
            // Default 9:00 AM if date known but no time specified yet
            val cal = Calendar.getInstance().apply {
                timeInMillis = dateResult!!
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            cal.timeInMillis
        } else if (!hasDate && hasTime) {
            // Assume today (or tomorrow if time already passed today)
            val cal = Calendar.getInstance()
            val nowHour = cal.get(Calendar.HOUR_OF_DAY)
            val nowMinute = cal.get(Calendar.MINUTE)
            cal.set(Calendar.HOUR_OF_DAY, timeResult!!.first)
            cal.set(Calendar.MINUTE, timeResult.second)
            cal.set(Calendar.SECOND, 0)
            if (timeResult.first < nowHour || (timeResult.first == nowHour && timeResult.second <= nowMinute)) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            cal.timeInMillis
        } else {
            null
        }

        val missing = when {
            !hasDate && !hasTime -> MissingField.DATE
            !hasTime -> MissingField.TIME
            else -> MissingField.NONE
        }

        return ParsedTaskIntent(
            title = if (cleanTitle.isNotBlank()) cleanTitle else input,
            categoryKey = categoryKey,
            dateTimeMillis = finalMillis,
            hasDate = hasDate,
            hasTime = hasTime,
            repeatType = repeatType,
            missingField = missing
        )
    }

    private fun detectCategory(lower: String): String {
        return when {
            lower.contains("visite") || lower.contains("famangiana") || lower.contains("malade") ||
                    lower.contains("marary") || lower.contains("visit") || lower.contains("fianakaviana") -> "catPastoralVisits"

            lower.contains("prédication") || lower.contains("predication") || lower.contains("toriteny") ||
                    lower.contains("sermon") || lower.contains("preach") || lower.contains("hitory") -> "catSermons"

            lower.contains("prière") || lower.contains("priere") || lower.contains("vavaka") ||
                    lower.contains("prayer") || lower.contains("veillée") || lower.contains("mivavaka") -> "catPrayers"

            lower.contains("réunion") || lower.contains("reunion") || lower.contains("fivoriana") ||
                    lower.contains("meeting") || lower.contains("comité") || lower.contains("birao") -> "catMeetings"

            lower.contains("mariage") || lower.contains("fanambadiana") || lower.contains("wedding") -> "catWeddings"

            lower.contains("baptême") || lower.contains("bapteme") || lower.contains("batisa") ||
                    lower.contains("baptism") -> "catBaptisms"

            lower.contains("funérailles") || lower.contains("enterrement") || lower.contains("fandevenana") ||
                    lower.contains("funeral") || lower.contains("fisaonana") -> "catFunerals"

            lower.contains("évangélisation") || lower.contains("evangelisation") || lower.contains("filazantsara") ||
                    lower.contains("evangelism") || lower.contains("fitoriana") -> "catEvangelism"

            lower.contains("étude biblique") || lower.contains("fianarana baiboly") ||
                    lower.contains("bible study") || lower.contains("baiboly") -> "catBibleStudy"

            lower.contains("formation") || lower.contains("fampianarana") || lower.contains("training") -> "catTraining"

            lower.contains("administration") || lower.contains("fitantanana") || lower.contains("comptabilité") -> "catAdministration"

            else -> "catPersonal"
        }
    }

    private fun detectRecurrence(lower: String): String {
        return when {
            lower.contains("chaque jour") || lower.contains("tous les jours") || lower.contains("isan'andro") ||
                    lower.contains("isan-andro") || lower.contains("every day") || lower.contains("daily") -> "DAILY"

            lower.contains("chaque semaine") || lower.contains("toutes les semaines") || lower.contains("isan-kerinandro") ||
                    lower.contains("chaque lundi") || lower.contains("chaque mardi") || lower.contains("chaque dimanche") ||
                    lower.contains("isaky ny") || lower.contains("every week") || lower.contains("weekly") -> "WEEKLY"

            lower.contains("chaque mois") || lower.contains("tous les mois") || lower.contains("isam-bolana") ||
                    lower.contains("every month") || lower.contains("monthly") -> "MONTHLY"

            else -> "NONE"
        }
    }

    private fun extractDate(lower: String): Long? {
        val cal = Calendar.getInstance()

        // Today / Anio / Today
        if (lower.contains("aujourd'hui") || lower.contains("anio") || lower.contains("today")) {
            return cal.timeInMillis
        }

        // Tomorrow / Rahampitso / Demain
        if (lower.contains("demain") || lower.contains("rahampitso") || lower.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            return cal.timeInMillis
        }

        // In X days / Afaka X andro / Dans X jours
        val inDaysMatch = Pattern.compile("(?:dans|afaka|in)\\s+(\\d+)\\s+(?:jours|andro|days)").matcher(lower)
        if (inDaysMatch.find()) {
            val count = inDaysMatch.group(1)?.toIntOrNull() ?: 1
            cal.add(Calendar.DAY_OF_YEAR, count)
            return cal.timeInMillis
        }

        // Days of week
        val daysMap = mapOf(
            Calendar.MONDAY to listOf("lundi", "alatsinainy", "monday"),
            Calendar.TUESDAY to listOf("mardi", "talata", "tuesday"),
            Calendar.WEDNESDAY to listOf("mercredi", "alarobia", "wednesday"),
            Calendar.THURSDAY to listOf("jeudi", "alakamisy", "thursday"),
            Calendar.FRIDAY to listOf("vendredi", "zoma", "friday"),
            Calendar.SATURDAY to listOf("samedi", "sabotsy", "saturday"),
            Calendar.SUNDAY to listOf("dimanche", "alahady", "sunday")
        )

        for ((dayOfWeek, names) in daysMap) {
            for (name in names) {
                if (lower.contains(name)) {
                    val currentDay = cal.get(Calendar.DAY_OF_WEEK)
                    var diff = dayOfWeek - currentDay
                    if (diff <= 0) {
                        diff += 7 // next occurrence
                    }
                    cal.add(Calendar.DAY_OF_YEAR, diff)
                    return cal.timeInMillis
                }
            }
        }

        return null
    }

    private fun extractTime(lower: String): Pair<Int, Int>? {
        // Regex for French/MG/EN:
        // "à 8 heures", "à 8h", "amin'ny 8", "amin'ny 14h30", "at 8am", "at 2:30pm", "14h"
        val timeRegex = Pattern.compile("(?:à|a|amin'ny|amin ny|at)?\\s*(\\d{1,2})(?:h|:|ora|\\s*heures?)?\\s*(\\d{2})?\\s*(am|pm|hariva|maraina)?")
        val matcher = timeRegex.matcher(lower)

        while (matcher.find()) {
            val hourStr = matcher.group(1) ?: continue
            val minuteStr = matcher.group(2)
            val ampm = matcher.group(3)

            var hour = hourStr.toIntOrNull() ?: continue
            val minute = minuteStr?.toIntOrNull() ?: 0

            if (ampm != null) {
                if ((ampm == "pm" || ampm == "hariva") && hour < 12) {
                    hour += 12
                } else if ((ampm == "am" || ampm == "maraina") && hour == 12) {
                    hour = 0
                }
            }

            if (hour in 0..23 && minute in 0..59) {
                return Pair(hour, minute)
            }
        }

        // General fallback for "ce soir" (19:00), "ce matin" (08:00), "tolakandro" (15:00)
        if (lower.contains("ce soir") || lower.contains("hariva") || lower.contains("tonight")) {
            return Pair(19, 0)
        }
        if (lower.contains("ce matin") || lower.contains("maraina") || lower.contains("this morning")) {
            return Pair(8, 0)
        }

        return null
    }

    private fun cleanTaskTitle(raw: String): String {
        var clean = raw
        val removePrefixes = listOf(
            "rappelle-moi de", "rappelle moi de", "rappelle-moi", "rappelle moi",
            "ampahatsiarovy ahy ny", "ampahatsiarovy ahy", "ampahatsiarovy",
            "remind me to", "remind me",
            "je dois", "tsy maintsy", "i must", "i have to", "i need to",
            "chaque lundi à 6 heures,", "chaque lundi à 6h,",
            "vendredi à 14 heures,", "vendredi à 14h,",
            "demain à 8 heures de", "demain à 8h de",
            "rahampitso amin'ny 8 ora"
        )
        for (prefix in removePrefixes) {
            if (clean.lowercase().startsWith(prefix)) {
                clean = clean.substring(prefix.length).trim()
            }
        }
        return clean.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
}
