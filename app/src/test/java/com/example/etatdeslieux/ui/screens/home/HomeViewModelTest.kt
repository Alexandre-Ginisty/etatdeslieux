package com.example.etatdeslieux.ui.screens.home

import com.example.etatdeslieux.data.dao.RoomDao
import com.example.etatdeslieux.model.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var viewModel: HomeViewModel
    private lateinit var roomDao: RoomDao
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        roomDao = mock()
        viewModel = HomeViewModel(roomDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting room updates selectedRoom state`() = runTest {
        // Given
        val room = Room(id = 1, name = "Test Room")

        // When
        viewModel.selectRoom(room)

        // Then
        assertEquals(room, viewModel.selectedRoom.value)
    }

    @Test
    fun `clearing selected room sets selectedRoom to null`() = runTest {
        // Given
        val room = Room(id = 1, name = "Test Room")
        viewModel.selectRoom(room)

        // When
        viewModel.clearSelectedRoom()

        // Then
        assertNull(viewModel.selectedRoom.value)
    }

    @Test
    fun `deleting room calls roomDao delete`() = runTest {
        // Given
        val room = Room(id = 1, name = "Test Room")
        whenever(roomDao.deleteRoom(room)).thenReturn(Unit)

        // When
        viewModel.deleteRoom(room)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(roomDao).deleteRoom(room)
    }

    @Test
    fun `updating room calls roomDao update`() = runTest {
        // Given
        val room = Room(id = 1, name = "Test Room")
        whenever(roomDao.updateRoom(room)).thenReturn(Unit)

        // When
        viewModel.updateRoom(room)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(roomDao).updateRoom(room)
    }
}
