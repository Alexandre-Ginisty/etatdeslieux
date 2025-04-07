package com.example.etatdeslieux

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SalleActivityInstrumentedTest {

    private lateinit var intent: Intent

    @Before
    fun setup() {
        intent = Intent(ApplicationProvider.getApplicationContext(), SalleActivity::class.java).apply {
            putExtra("PIECE_NAME", "Test Room")
            putExtra("PIECE_DESCRIPTION", "Test Description")
            putExtra("PIECE_SIZE", 25.5f)
            putExtra("PIECE_FLOOR", 2)
            putExtra("PIECE_CREATOR", "Test Creator")
            putExtra("PIECE_ETAT_TYPE", "Entrée")
            putExtra("PIECE_ETAT_NUMBER", 123)
        }
    }

    @Test
    fun testDisplayRoomDetails() {
        ActivityScenario.launch<SalleActivity>(intent)

        onView(withId(R.id.salleTitle))
            .check(matches(withText("Test Room")))
        
        onView(withId(R.id.salleDescription))
            .check(matches(withText("Test Description")))
        
        onView(withId(R.id.salleSize))
            .check(matches(withText("Taille : 25.5 m²")))
        
        onView(withId(R.id.salleFloor))
            .check(matches(withText("Étage : 2")))
        
        onView(withId(R.id.salleCreator))
            .check(matches(withText("Créateur : Test Creator")))
    }

    @Test
    fun testTakePhotoButton() {
        ActivityScenario.launch<SalleActivity>(intent)

        onView(withId(R.id.takePhotoButton))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun testDownloadPdfButton() {
        ActivityScenario.launch<SalleActivity>(intent)

        onView(withId(R.id.downloadPdfButton))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun testChecklistDisplay() {
        ActivityScenario.launch<SalleActivity>(intent)

        onView(withId(R.id.checklistRecyclerView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testPhotoContainerDisplay() {
        ActivityScenario.launch<SalleActivity>(intent)

        onView(withId(R.id.photoContainer))
            .check(matches(isDisplayed()))
    }
}
