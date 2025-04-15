package com.example.etatdeslieux.ui.screens.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.etatdeslieux.MainActivity
import com.example.etatdeslieux.model.Room
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testRoomItemLongClick() {
        // Attendre que l'écran soit chargé
        composeTestRule.waitForIdle()

        // Trouver un RoomItem et faire un long click
        composeTestRule
            .onNodeWithText("Chambre")
            .performTouchInput { longClick() }

        // Vérifier que le dialogue d'options est affiché
        composeTestRule
            .onNodeWithText("Options de la salle")
            .assertIsDisplayed()

        // Vérifier que les boutons sont présents
        composeTestRule
            .onNodeWithText("Renommer")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Supprimer")
            .assertIsDisplayed()
    }

    @Test
    fun testRenameRoom() {
        composeTestRule.waitForIdle()

        // Long click sur la salle
        composeTestRule
            .onNodeWithText("Chambre")
            .performTouchInput { longClick() }

        // Cliquer sur Renommer
        composeTestRule
            .onNodeWithText("Renommer")
            .performClick()

        // Vérifier que le dialogue de renommage est affiché
        composeTestRule
            .onNodeWithText("Renommer la salle")
            .assertIsDisplayed()

        // Entrer un nouveau nom
        composeTestRule
            .onNodeWithTag("rename_input")
            .performTextInput("Nouvelle Chambre")

        // Cliquer sur le bouton Renommer
        composeTestRule
            .onAllNodesWithText("Renommer")
            .onLast()
            .performClick()

        // Vérifier que le nouveau nom est affiché
        composeTestRule
            .onNodeWithText("Nouvelle Chambre")
            .assertIsDisplayed()
    }

    @Test
    fun testDeleteRoom() {
        composeTestRule.waitForIdle()

        // Long click sur la salle
        composeTestRule
            .onNodeWithText("Chambre")
            .performTouchInput { longClick() }

        // Cliquer sur Supprimer
        composeTestRule
            .onNodeWithText("Supprimer")
            .performClick()

        // Vérifier que la salle n'est plus affichée
        composeTestRule
            .onNodeWithText("Chambre")
            .assertDoesNotExist()
    }
}
