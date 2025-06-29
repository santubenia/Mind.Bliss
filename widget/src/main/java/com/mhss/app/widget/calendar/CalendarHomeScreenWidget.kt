package com.mhss.app.widget.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.material3.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.mhss.app.ui.R
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.util.date.now
import com.mhss.app.widget.WidgetTheme
import com.mhss.app.widget.largeBackgroundBasedOnVersion
import com.mhss.app.widget.largeInnerBackgroundBasedOnVersion
import com.mhss.app.widget.widgetDarkColorScheme
import kotlin.time.Duration.Companion.hours

@Composable
fun CalendarHomeScreenWidget(
    events: Map<String, List<CalendarEvent>>,
    hasPermission: Boolean
) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .largeBackgroundBasedOnVersion()
    ) {
        Column(
            modifier = GlanceModifier
                .padding(8.dp)
        ) {
            Row(
                GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    context.getString(R.string.calendar),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    modifier = GlanceModifier
                        .padding(horizontal = 8.dp)
                        .clickable(onClick = actionRunCallback<NavigateToCalendarAction>())
                )
                Row(
                    modifier = GlanceModifier
                        .clickable(onClick = actionRunCallback<NavigateToCalendarAction>())
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Image(
                        modifier = GlanceModifier
                            .size(22.dp)
                            .clickable(actionRunCallback<RefreshCalendarAction>()),
                        provider = ImageProvider(R.drawable.ic_refresh),
                        contentDescription = "refresh",
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer)
                    )
                    Spacer(GlanceModifier.width(12.dp))
                    Image(
                        modifier = GlanceModifier
                            .size(22.dp)
                            .clickable(actionRunCallback<AddEventAction>())
                        ,
                        provider = ImageProvider(R.drawable.ic_add),
                        contentDescription = "add event",
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer)
                    )
                }
            }
            Spacer(GlanceModifier.height(8.dp))
            if (hasPermission) {
                LazyColumn(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .largeInnerBackgroundBasedOnVersion()
                        .padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    item { Spacer(GlanceModifier.height(4.dp)) }
                    if (events.isEmpty()) {
                        item {
                            Text(
                                text = context.getString(R.string.no_events),
                                modifier = GlanceModifier.fillMaxWidth().padding(16.dp),
                                style = TextStyle(
                                    color = GlanceTheme.colors.secondary,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                ),
                            )
                        }
                    }
                    item { Spacer(GlanceModifier.height(6.dp)) }
                    events.forEach { (day, dayEvents) ->
                        item {
                            Text(
                                text = day,
                                style = TextStyle(
                                    color = GlanceTheme.colors.secondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                modifier = GlanceModifier.padding(bottom = 2.dp)
                            )
                        }
                        items(dayEvents) { event ->
                            CalendarEventWidgetItem(event = event)
                        }
                    }
                }
            } else {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = context.getString(R.string.no_read_calendar_permission_message),
                        modifier = GlanceModifier.padding(16.dp),
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            color = GlanceTheme.colors.secondary,
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Button(
                        text = context.getString(R.string.go_to_settings),
                        onClick = actionRunCallback<GoToSettingsAction>()
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = context.getString(R.string.calendar_widget_refresh_message),
                        modifier = GlanceModifier.padding(12.dp),
                        style = TextStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 360, heightDp = 240)
@Composable
private fun CalendarHomeScreenWidgetPreview() {
    WidgetTheme(ColorProviders(widgetDarkColorScheme)) {
        CalendarHomeScreenWidget(
            mapOf(
                "Monday 7, 2024" to listOf(
                    CalendarEvent(
                        id = 1,
                        title = "Event 1",
                        start = now(),
                        end = now() + 1.hours.inWholeMilliseconds,
                        calendarId = 1,
                        location = "Location 1",
                        color = Color.Red.toArgb()
                    )
                ),
                "Tuesday 8, 2024" to listOf(
                    CalendarEvent(
                        id = 3,
                        title = "Event 2",
                        start = now() + 4.hours.inWholeMilliseconds,
                        end = now() + 5.hours.inWholeMilliseconds,
                        calendarId = 3,
                        location = "Location 2",
                        color = Color.Green.toArgb()
                    ),
                    CalendarEvent(
                        id = 4,
                        title = "Event 3",
                        start = now() + 6.hours.inWholeMilliseconds,
                        end = now() + 7.hours.inWholeMilliseconds,
                        calendarId = 4,
                        location = "Location 3",
                        color = Color.Gray.toArgb()
                    )
                )
            ),
            true
        )
    }
}