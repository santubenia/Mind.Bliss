package com.mhss.app.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.Note
import com.mhss.app.ui.R
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.color
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.util.date.formatDateDependingOnDay
import com.mhss.app.util.date.isDueDateOverdue


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAttachmentsSection(
    modifier: Modifier = Modifier,
    attachments: List<AiMessageAttachment>,
    onRemove: (Int) -> Unit = {},
    editable: Boolean = false,
) {
    if (editable) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 5.dp, end = 5.dp)
                .horizontalScroll(rememberScrollState()),
        ) {
            attachments.forEachIndexed { i, it ->
                when (it) {
                    is AiMessageAttachment.Note -> NoteAttachmentCard(
                        note = it.note,
                        showRemoveButton = true
                    ) {
                        onRemove(i)
                    }

                    is AiMessageAttachment.Task -> TaskAttachmentCard(
                        task = it.task,
                        showRemoveButton = true
                    ) {
                        onRemove(i)
                    }

                    is AiMessageAttachment.CalenderEvents -> CalendarEventsAttachmentCard(
                        showRemoveButton = true,
                    ) {
                        onRemove(i)
                    }
                }
            }
        }
    } else {
        var maxLines by remember { mutableIntStateOf(2) }
        ContextualFlowRow(
            modifier = modifier
                .animateContentSize()
                .padding(4.dp),
            itemCount = attachments.size,
            maxLines = maxLines,
            overflow = ContextualFlowRowOverflow.expandOrCollapseIndicator(
                expandIndicator = {
                    Button(
                        onClick = { maxLines++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            "+${this@expandOrCollapseIndicator.totalItemCount - this@expandOrCollapseIndicator.shownItemCount}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
                collapseIndicator = {}
            )
        ) { i ->
            when (val attachment = attachments[i]) {
                is AiMessageAttachment.Note -> NoteAttachmentCard(attachment.note)
                is AiMessageAttachment.Task -> TaskAttachmentCard(attachment.task)
                is AiMessageAttachment.CalenderEvents -> CalendarEventsAttachmentCard()
            }
        }
    }

}

@Composable
fun NoteAttachmentCard(
    note: Note,
    modifier: Modifier = Modifier,
    showRemoveButton: Boolean = false,
    onRemoveClick: () -> Unit = {},
) {
    Box(
        modifier
            .widthIn(max = 200.dp)
            .padding(top = 6.dp, end = 6.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(8.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
        }
        if (showRemoveButton) {
            RemoveButton(
                Modifier.align(Alignment.TopEnd),
                onClick = onRemoveClick
            )
        }
    }
}

@Composable
internal fun TaskAttachmentCard(
    task: Task,
    modifier: Modifier = Modifier,
    showRemoveButton: Boolean = false,
    onRemoveClick: () -> Unit = {},
) {
    val context = LocalContext.current
    Box(
        modifier
            .widthIn(max = 200.dp)
            .padding(top = 6.dp, end = 6.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                Modifier
                    .padding(8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(1.dp, task.priority.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .align(Alignment.Center),
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                if (task.subTasks.isNotEmpty() || task.dueDate != 0L) Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (task.subTasks.isNotEmpty()) {
                        val completed = remember {
                            task.subTasks.count { it.isCompleted }
                        }
                        val total = task.subTasks.size
                        Text(
                            text = "$completed/$total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    if (task.dueDate != 0L) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(8.dp),
                                painter = painterResource(R.drawable.ic_alarm),
                                contentDescription = stringResource(R.string.due_date),
                                tint = if (task.dueDate.isDueDateOverdue()) Color.Red else MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.8f
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = task.dueDate.formatDateDependingOnDay(context),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (task.dueDate.isDueDateOverdue()) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.7f
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        if (showRemoveButton) {
            RemoveButton(
                Modifier.align(Alignment.TopEnd),
                onClick = onRemoveClick
            )
        }
    }
}

@Composable
fun CalendarEventsAttachmentCard(
    modifier: Modifier = Modifier,
    showRemoveButton: Boolean = false,
    onRemoveClick: () -> Unit = {},
) {
    Box(
        modifier.padding(top = 6.dp, end = 6.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = stringResource(R.string.calendar),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.calendar_events_next_7_days),
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
        }
        if (showRemoveButton) {
            RemoveButton(
                Modifier.align(Alignment.TopEnd),
                onClick = onRemoveClick
            )
        }
    }
}

@Composable
fun RemoveButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Icon(
        imageVector = Icons.Default.Clear,
        contentDescription = stringResource(R.string.delete_note),
        modifier = modifier
            .offset(x = 4.dp, y = (-4).dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border((0.5f).dp, Color.LightGray, CircleShape)
            .padding(2.dp)
            .size(10.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


@Preview
@Composable
private fun NoteAttachmentPreview() {
    MyBrainTheme {
        NoteAttachmentCard(
            note = Note(
                id = 1,
                title = "Test note Note Title".repeat(3),
                content = "Note Content",
            ),
            showRemoveButton = true
        )
    }
}

@Preview
@Composable
private fun TaskAttachmentPreview() {
    MyBrainTheme {
        TaskAttachmentCard(
            task = Task(
                id = 1,
                title = "Test task Task Title".repeat(3),
                description = "Task Description",
                isCompleted = false,
                dueDate = 12345,
                subTasks = listOf(
                    SubTask()
                )
            ),
            showRemoveButton = true
        )
    }
}

@Preview
@Composable
private fun CalendarEventsCardPreview() {
    MyBrainTheme {
        CalendarEventsAttachmentCard(showRemoveButton = true)
    }
}