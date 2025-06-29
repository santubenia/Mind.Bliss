package com.mhss.app.domain.use_case

import com.mhss.app.alarm.model.Alarm
import com.mhss.app.alarm.use_case.AddAlarmUseCase
import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.widget.WidgetUpdater
import org.koin.core.annotation.Single

@Single
class UpdateTaskUseCase(
    private val tasksRepository: TaskRepository,
    private val addAlarm: AddAlarmUseCase,
    private val deleteAlarm: DeleteAlarmUseCase,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(task: Task, updateDueDate: Boolean): Boolean {
        tasksRepository.updateTask(task)
        widgetUpdater.updateAll(WidgetUpdater.WidgetType.Tasks)
        return if (updateDueDate) {
            if (task.dueDate != 0L) {
                addAlarm(
                    Alarm(
                        task.id,
                        task.dueDate
                    )
                )
            } else {
                deleteAlarm(task.id)
                true
            }
        } else true
    }
}