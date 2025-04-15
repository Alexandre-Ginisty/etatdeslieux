package com.example.etatdeslieux

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Before
    fun setup() {
        ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun testCreateNewPiece() {
        // Remplir le formulaire
        onView(withId(R.id.editTextPieceName))
            .perform(typeText("Chambre Test"), closeSoftKeyboard())
        
        onView(withId(R.id.editTextPieceDescription))
            .perform(typeText("Description Test"), closeSoftKeyboard())
        
        onView(withId(R.id.editTextPieceSize))
            .perform(typeText("20.5"), closeSoftKeyboard())
        
        onView(withId(R.id.editTextPieceFloor))
            .perform(typeText("1"), closeSoftKeyboard())
        
        onView(withId(R.id.editTextPieceCreator))
            .perform(typeText("Test User"), closeSoftKeyboard())

        // Cliquer sur le bouton de création
        onView(withId(R.id.creerPieceButton))
            .perform(click())

        // Vérifier que la pièce apparaît dans la liste
        onView(withText("Chambre Test"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testLongClickPiece() {
        // Créer d'abord une pièce
        testCreateNewPiece()

        // Faire un long clic sur la pièce
        onView(withText("Chambre Test"))
            .perform(longClick())

        // Vérifier que les boutons d'action sont visibles
        onView(withId(R.id.actionButtonsLayout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testDeletePiece() {
        // Créer d'abord une pièce
        testCreateNewPiece()

        // Faire un long clic sur la pièce
        onView(withText("Chambre Test"))
            .perform(longClick())

        // Cliquer sur le bouton de suppression
        onView(withId(R.id.deleteButton))
            .perform(click())

        // Vérifier que la pièce n'est plus visible
        onView(withText("Chambre Test"))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun testSpinnerSelection() {
        onView(withId(R.id.spinnerEtatDesLieux))
            .perform(click())
        
        onView(withText("Entrée"))
            .perform(click())
        
        onView(withId(R.id.spinnerEtatDesLieux))
            .check(matches(withSpinnerText("Entrée")))
    }
}
