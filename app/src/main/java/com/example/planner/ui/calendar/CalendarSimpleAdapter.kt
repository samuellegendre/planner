package com.example.planner.ui.calendar

import android.graphics.Color
import android.graphics.RectF
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import java.util.*

data class Event(
    var id: Long,
    var title: String,
    var subtitle: String,
    var startTime: Calendar,
    var endTime: Calendar,
    var color: String
)

class CalendarSimpleAdapter : WeekView.SimpleAdapter<Event>() {

    override fun onCreateEntity(item: Event): WeekViewEntity {
        val style = WeekViewEntity.Style.Builder()
            .setBackgroundColor(Color.parseColor(item.color))
            .build()

        return WeekViewEntity.Event.Builder(item)
            .setId(item.id)
            .setTitle(item.title)
            .setSubtitle(item.subtitle)
            .setStartTime(item.startTime)
            .setEndTime(item.endTime)
            .setStyle(style)
            .build()
    }

    override fun onEventClick(data: Event) {
        // TODO
    }

    override fun onEventClick(data: Event, bounds: RectF) {
        // TODO
    }

    override fun onEventLongClick(data: Event) {
        // TODO
    }

    override fun onEventLongClick(data: Event, bounds: RectF) {
        // TODO
    }

    override fun onEmptyViewClick(time: Calendar) {
        // TODO
    }

    override fun onEmptyViewLongClick(time: Calendar) {
        // TODO
    }

    override fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar) {
        // TODO
    }

}