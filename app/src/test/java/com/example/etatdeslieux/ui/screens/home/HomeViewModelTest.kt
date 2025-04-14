package com.example.etatdeslieux.ui.screens.home

import com.example.etatdeslieux.data.repository.RoomGroupRepository
import com.example.etatdeslieux.data.repository.RoomRepository
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var viewModel: HomeViewModel
    private lateinit var roomRepository: RoomRepository
    private lateinit var roomGroupRepository: RoomGroupRepository
    private val testDispatcher = StandardTestDispatcher()
    private val roomsFlow = MutableStateFlow<List<Room>>(emptyList())
    private val groupsFlow = MutableStateFlow<List<RoomGroup>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        roomRepository = mockk(relaxed = true)
        roomGroupRepository = mockk(relaxed = true)

        coEvery { roomRepository.getAllRooms() } returns roomsFlow
        coEvery { roomGroupRepository.getAllRoomGroups() } returns groupsFlow

        viewModel = HomeViewModel(roomRepository, roomGroupRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized, loads rooms and groups`() = runTest {
        // Given
        val testRoom = Room(
            id = 1L,
            name = "Test Room",
            description = "Test Description",
            size = 20f,
            floor = 1,
            creator = "Test Creator",
            etatType = "ENTREE",
            etatNumber = 1
        )
        val testGroup = RoomGroup(
            id = 1L,
            name = "Test Group",
            roomIds = setOf(1L),
            isExpanded = true
        )

        // When
        roomsFlow.value = listOf(testRoom)
        groupsFlow.value = listOf(testGroup)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(listOf(testRoom), viewModel.uiState.value.rooms)
        assertEquals(listOf(testGroup), viewModel.uiState.value.roomGroups)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `when toggling group expanded, updates repository`() = runTest {
        // Given
        val testGroup = RoomGroup(
            id = 1L,
            name = "Test Group",
            roomIds = setOf(1L),
            isExpanded = false
        )
        groupsFlow.value = listOf(testGroup)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleGroupExpanded(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            roomGroupRepository.updateRoomGroup(match {
                it.id == 1L && it.isExpanded
            })
        }
    }

    @Test
    fun `when deleting room, updates repository and groups`() = runTest {
        // Given
        val testRoom = Room(
            id = 1L,
            name = "Test Room",
            description = "Test Description",
            size = 20f,
            floor = 1,
            creator = "Test Creator",
            etatType = "ENTREE",
            etatNumber = 1
        )
        val testGroup = RoomGroup(
            id = 1L,
            name = "Test Group",
            roomIds = setOf(1L),
            isExpanded = true
        )

        roomsFlow.value = listOf(testRoom)
        groupsFlow.value = listOf(testGroup)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.deleteRoom(testRoom)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            roomRepository.deleteRoom(1L)
            roomGroupRepository.updateRoomGroup(match {
                it.id == 1L && it.roomIds.isEmpty()
            })
        }
    }

    @Test
    fun `when creating room group, updates repository`() = runTest {
        // Given
        val groupName = "New Group"
        val roomIds = setOf(1L, 2L)
        coEvery { roomGroupRepository.insertRoomGroup(any()) } returns 1L

        // When
        viewModel.createRoomGroup(groupName, roomIds)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            roomGroupRepository.insertRoomGroup(match {
                it.name == groupName && it.roomIds == roomIds && it.isExpanded
            })
        }
    }
}
