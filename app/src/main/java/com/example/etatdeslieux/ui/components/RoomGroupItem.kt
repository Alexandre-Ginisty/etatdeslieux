package com.example.etatdeslieux.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup

@Composable
fun RoomGroupItem(
    group: RoomGroup,
    rooms: List<Room>,
    onGroupClick: () -> Unit,
    onRoomClick: (Long) -> Unit,
    onRoomLongClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(group.isExpanded) }
    
    Column(modifier = modifier) {
        // Group header
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onAddClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter un état des lieux",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { 
                            isExpanded = !isExpanded
                            onGroupClick()
                        }
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "Réduire" else "Développer"
                )
            }
        }

        // Group content
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                rooms.filter { it.id in group.roomIds }.forEach { room ->
                    RoomItem(
                        room = room,
                        onClick = { onRoomClick(room.id) },
                        onLongClick = { onRoomLongClick(room.id) }
                    )
                }
            }
        }
    }
}
