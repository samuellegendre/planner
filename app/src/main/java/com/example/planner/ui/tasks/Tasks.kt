package com.example.planner.ui.tasks

import android.content.Context
import com.example.planner.utils.CalendarSerializer
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.utils.DragDropUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

@Serializable
data class Task(
    var id: Long,
    var title: String,
    var description: String,
    @Serializable(with = CalendarSerializer::class)
    var calendar: Calendar,
    var hasDate: Boolean,
    var hasTime: Boolean,
    var isChecked: Boolean = false
)

class Tasks(private val context: Context, private val fastItemAdapter: FastItemAdapter<TaskItem>) {
    private val fileName = "task_data"
    private val format = Json { prettyPrint = true }

    private var tasks = mutableListOf<Task>()
    var items = mutableListOf<TaskItem>()

    private fun taskToItem(task: Task): TaskItem {
        val item = TaskItem()
        item.id = task.id
        item.title = task.title
        item.description = task.description
        item.dateTime = task.calendar
        item.hasDate = task.hasDate
        item.hasTime = task.hasTime
        item.isChecked = task.isChecked
        return item
    }

    fun itemToTask(item: TaskItem): Task {
        return Task(
            item.id!!,
            item.title.toString(),
            item.description.toString(),
            item.dateTime!!,
            item.hasDate!!,
            item.hasTime!!,
            item.isChecked!!
        )
    }

    fun addTask(task: Task) {
        val item = taskToItem(task)
        tasks.add(0, task)
        items.add(0, item)
        fastItemAdapter.add(0, item)
        save()
    }

    fun updateTask(task: Task) {
        val item = taskToItem(task)
        val position = tasks.indexOf(tasks.first { it.id == task.id })
        val taskItem = fastItemAdapter.getAdapterItem(position)
        tasks[position] = task
        items[position] = item
        taskItem.title = item.title
        taskItem.description = item.description
        taskItem.dateTime = item.dateTime
        taskItem.hasDate = item.hasDate
        taskItem.hasTime = item.hasTime
        taskItem.isChecked = item.isChecked
        fastItemAdapter.notifyItemChanged(position)
        save()
    }

    fun removeTask(task: Task) {
        val position = tasks.indexOf(task)
        tasks.removeAt(position)
        items.removeAt(position)
        fastItemAdapter.itemFilter.remove(position)
        save()
    }

    fun moveTask(oldPosition: Int, newPosition: Int) {
        DragDropUtil.onMove(fastItemAdapter.itemAdapter, oldPosition, newPosition)
        items = fastItemAdapter.adapterItems.toMutableList()
        tasks.clear()
        items.forEach {
            tasks.add(itemToTask(it))
        }
        save()
    }

    fun getLastId(): Long {
        return if (tasks.isEmpty()) 0 else tasks.maxOf { it.id }
    }

    fun save() {
        val fileContents = format.encodeToString(tasks)
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(fileContents.toByteArray())
        }
    }

    fun fetchItems(): MutableList<TaskItem> {
        if (File(context.filesDir, fileName).exists()) {
            val fileContents = context.openFileInput(fileName).bufferedReader().readText()
            val data = format.decodeFromString<MutableList<Task>>(fileContents)
            tasks = data
        }
        items.clear()
        tasks.forEach {
            items.add(taskToItem(it))
        }
        return items
    }
}


