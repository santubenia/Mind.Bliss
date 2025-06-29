package com.mhss.app.widget.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mhss.app.domain.model.Priority
import com.mhss.app.ui.R
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.color
import com.mhss.app.util.date.formatDateDependingOnDay
import com.mhss.app.util.date.isDueDateOverdue
import com.mhss.app.widget.smallBackgroundBasedOnVersion

@Composable
fun TaskWidgetItem(
    task: Task
) {
    val context = LocalContext.current
    Box(
        GlanceModifier.padding(bottom = 3.dp)
    ) {
        Column(
            GlanceModifier
                .smallBackgroundBasedOnVersion()
                .padding(10.dp)
                .clickable(
                    actionRunCallback<TaskWidgetItemClickAction>(
                        parameters = actionParametersOf(
                            taskId to task.id
                        )
                    )
                )
        ) {
            Row(
                GlanceModifier
                    .fillMaxWidth()
                    .clickable(
                        actionRunCallback<TaskWidgetItemClickAction>(
                            parameters = actionParametersOf(
                                taskId to task.id
                            )
                        )
                    ), verticalAlignment = Alignment.CenterVertically
            ) {
                TaskWidgetCheckBox(
                    isComplete = task.isCompleted,
                    task.priority.color,
                    onComplete = actionRunCallback<CompleteTaskAction>(
                        parameters = actionParametersOf(
                            taskId to task.id,
                            completed to !task.isCompleted
                        )
                    )
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    task.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 2,
                    modifier = GlanceModifier.clickable(
                        actionRunCallback<CompleteTaskAction>(
                            parameters = actionParametersOf(
                                taskId to task.id,
                                completed to !task.isCompleted
                            )
                        )
                    )
                )

            }
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (task.subTasks.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = GlanceModifier.padding(top = 4.dp)
                    ) {
                        val completed = remember {
                            task.subTasks.count { it.isCompleted }
                        }
                        val total = task.subTasks.size
                        Image(
                            provider = ImageProvider(R.drawable.ic_bullet_list),
                            modifier = GlanceModifier
                                .size(12.dp),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer)
                        )
                        Spacer(GlanceModifier.width(3.dp))
                        Text(
                            text = "$completed/$total",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSecondaryContainer,
                                fontSize = 12.sp,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        )
                    }
                    Spacer(GlanceModifier.width(4.dp))
                }
                if (task.dueDate != 0L) {
                    Row(
                        modifier = GlanceModifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            modifier = GlanceModifier.size(12.dp),
                            provider = ImageProvider(R.drawable.ic_alarm),
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(
                                if (task.dueDate.isDueDateOverdue()) ColorProvider(Color.Red) else GlanceTheme.colors.onSecondaryContainer
                            )
                        )
                        Spacer(GlanceModifier.width(3.dp))
                        Text(
                            text = task.dueDate.formatDateDependingOnDay(context),
                            style = TextStyle(
                                color = if (task.dueDate.isDueDateOverdue())
                                    ColorProvider(Color.Red)
                                else GlanceTheme.colors.onSecondaryContainer,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskWidgetCheckBox(
    isComplete: Boolean,
    borderColor: Color,
    onComplete: Action
) {
    Box(
        modifier = GlanceModifier
            .size(25.dp)
            .background(
                ImageProvider(
                    when (borderColor) {
                        Priority.LOW.color -> R.drawable.task_check_box_background_green
                        Priority.MEDIUM.color -> R.drawable.task_check_box_background_orange
                        else -> R.drawable.task_check_box_background_red
                    }
                )
            )
            .clickable(
                onClick = onComplete
            ).padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isComplete) {
            Image(
                modifier = GlanceModifier.size(14.dp),
                provider = ImageProvider(R.drawable.ic_check),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer)
            )
        }
    }
}