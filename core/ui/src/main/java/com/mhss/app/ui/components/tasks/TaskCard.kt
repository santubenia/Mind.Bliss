package com.mhss.app.ui.components.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.R
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.ui.color
import com.mhss.app.util.date.formatDateDependingOnDay
import com.mhss.app.util.date.isDueDateOverdue

@Composable
fun LazyItemScope.TaskCard(
    modifier: Modifier = Modifier,
    task: Task,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .animateItem(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(
            8.dp
        ),
    ) {
        Column(
            Modifier
                .clickable {
                    onClick()
                }
                .padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TaskCheckBox(
                    isComplete = task.isCompleted,
                    task.priority.color,
                    onComplete = { onComplete() }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (task.subTasks.isNotEmpty()) {
                    SubTasksProgressBar(
                        modifier = Modifier.padding(top = 8.dp),
                        subTasks = task.subTasks
                    )
                }
                Spacer(Modifier.width(8.dp))
                if (task.dueDate != 0L) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(13.dp),
                            painter = painterResource(R.drawable.ic_alarm),
                            contentDescription = stringResource(R.string.due_date),
                            tint = if (task.dueDate.isDueDateOverdue()) Color.Red else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.8f
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = task.dueDate.formatDateDependingOnDay(context),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (task.dueDate.isDueDateOverdue()) Color.Red else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.8f
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCheckBox(
    isComplete: Boolean,
    borderColor: Color,
    onComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .clickable {
                onComplete()
            }, contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = isComplete) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null
            )
        }
    }
}

@Composable
fun SubTasksProgressBar(modifier: Modifier = Modifier, subTasks: List<SubTask>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        val completed = remember {
            subTasks.count { it.isCompleted }
        }
        val total = subTasks.size
        val progress by remember {
            derivedStateOf {
                completed.toFloat() / total.toFloat()
            }
        }
        val circleColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        val progressColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        Canvas(
            modifier = Modifier.size(16.dp)
        ) {
            drawCircle(
                color = circleColor,
                radius = size.width / 2,
                style = Stroke(width = 8f)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                style = Stroke(width = 8f, cap = StrokeCap.Round),
                useCenter = false
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.bodyMedium,
            color = progressColor,
        )
    }
}

@Preview
@Composable
fun TaskItemPreview() {
    LazyColumn {
        item {
            TaskCard(
                task = Task(
                    title = "Task 1",
                    description = "Task 1 description",
                    dueDate = 1666999999999L,
                    priority = Priority.MEDIUM,
                    isCompleted = false
                ),
                onComplete = {},
                onClick = {}
            )
        }
    }
}