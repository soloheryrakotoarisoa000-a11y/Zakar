package com.example

import com.example.assistant.MinistryNlpParser
import com.example.assistant.MissingField
import com.example.localization.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testNlpParser_frenchCommand() {
        val input = "Rappelle-moi demain à 8 heures de rendre visite à une famille malade."
        val result = MinistryNlpParser.parse(input, AppLanguage.FR)

        assertTrue("Category should be pastoral visits", result.categoryKey == "catPastoralVisits")
        assertTrue("Should detect explicit date", result.hasDate)
        assertTrue("Should detect explicit time", result.hasTime)
        assertNotNull("DateTimeMillis should be computed", result.dateTimeMillis)
    }

    @Test
    fun testNlpParser_malagasyCommand() {
        val input = "Ampahatsiarovy ahy rahampitso amin'ny 8 ora hamangy fianakaviana marary."
        val result = MinistryNlpParser.parse(input, AppLanguage.MG)

        assertTrue("Category should be pastoral visits", result.categoryKey == "catPastoralVisits")
        assertTrue("Should detect explicit date", result.hasDate)
        assertTrue("Should detect explicit time", result.hasTime)
    }

    @Test
    fun testNlpParser_recurrence() {
        val input = "Chaque lundi à 6 heures, rappelle-moi de préparer le programme de la semaine."
        val result = MinistryNlpParser.parse(input, AppLanguage.FR)

        assertEquals("WEEKLY", result.repeatType)
        assertTrue(result.hasTime)
    }

    @Test
    fun testNlpParser_missingTimePrompt() {
        val input = "Je dois préparer ma prédication pour dimanche prochain."
        val result = MinistryNlpParser.parse(input, AppLanguage.FR)

        assertTrue("Category should be sermon", result.categoryKey == "catSermons")
        assertTrue("Date should be detected", result.hasDate)
        assertEquals("Should indicate missing time", MissingField.TIME, result.missingField)
    }
}
