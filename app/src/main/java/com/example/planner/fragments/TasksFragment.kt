package com.example.planner.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.planner.R
import com.example.planner.dialogs.AddTaskDialog
import com.example.planner.dialogs.ConfirmDeletionDialog
import com.example.planner.dialogs.ModifyTaskDialog
import com.example.planner.models.TaskItem
import com.example.planner.utils.Task
import com.example.planner.utils.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.swipe.SimpleSwipeCallback
import com.mikepenz.fastadapter.swipe_drag.SimpleSwipeDragCallback
import java.util.*

class TasksFragment : Fragment(), AddTaskDialog.AddTaskDialogListener,
    ModifyTaskDialog.ModifyTaskDialogListener, ItemTouchCallback,
    SimpleSwipeCallback.ItemSwipeCallback,
    ConfirmDeletionDialog.ConfirmDeletionDialogListener {

    private lateinit var addTaskButton: FloatingActionButton

    private lateinit var fastAdapter: FastAdapter<TaskItem>
    private lateinit var itemAdapter: ItemAdapter<TaskItem>
    private lateinit var tasks: Tasks

    private lateinit var touchCallback: SimpleDragCallback
    private lateinit var touchHelper: ItemTouchHelper

    private var showDoneTasks = true
    private var inAscendingAlphabeticalOrder = false
    private var inAscendingDateOrder = false

    private val deleteHandler = Handler {
        val item = it.obj as TaskItem

        item.swipedAction = null
        val position = itemAdapter.getAdapterPosition(item)
        if (position != RecyclerView.NO_POSITION) {
            val list = mutableListOf<Task>()
            list.add(tasks.itemToTask(item))
            tasks.removeTasks(list)
        }
        true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val view: View = inflater.inflate(R.layout.fragment_tasks, container, false)

        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter)

        fastAdapter.onClickListener =
            { _: View?, _: IAdapter<TaskItem>, item: TaskItem, _: Int ->
                val dialog = ModifyTaskDialog(tasks.itemToTask(item))
                dialog.show(childFragmentManager, "modifyTask")
                false
            }

        fastAdapter.addEventHook(object : ClickEventHook<TaskItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return if (viewHolder is TaskItem.ViewHolder) {
                    viewHolder.checkBox
                } else {
                    null
                }
            }

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<TaskItem>,
                item: TaskItem
            ) {
                item.isChecked = !item.isChecked!!
                tasks.updateTask(tasks.itemToTask(item))
                if (!showDoneTasks) toggleShowDoneTasks(!showDoneTasks)
                if (item.isChecked!!) {
                    tasks.moveTask(position, tasks.items.lastIndex)
                } else {
                    tasks.moveTask(position, 0)
                }
                if (!showDoneTasks) toggleShowDoneTasks(showDoneTasks)
            }

        })

        tasks = Tasks(requireContext(), fastAdapter, itemAdapter)
        val recyclerView: RecyclerView = view.findViewById(R.id.taskRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = fastAdapter

        itemAdapter.add(tasks.fetchItems())

        val leaveBehindDrawableLeft =
            ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_delete, null)

        touchCallback = SimpleSwipeDragCallback(
            this,
            this,
            leaveBehindDrawableLeft,
            ItemTouchHelper.LEFT,
            Color.RED
        )
            .withNotifyAllDrops(true)
            .withSensitivity(10f)
            .withSurfaceThreshold(0.8f)

        touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(recyclerView)

        fastAdapter.withSavedInstanceState(savedInstanceState)

        addTaskButton = view.findViewById(R.id.addTaskButton)

        addTaskButton.setOnClickListener {
            val dialog = AddTaskDialog()
            dialog.show(childFragmentManager, "addTask")
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_tasks_menu, menu)
    }

    override fun onResume() {
        super.onResume()
        addTaskButton.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.showDoneTasks -> {
                item.isChecked = !item.isChecked
                showDoneTasks = item.isChecked
                toggleShowDoneTasks(showDoneTasks)
                true
            }
            R.id.deleteCheckedTasks -> {
                val dialog = ConfirmDeletionDialog()
                dialog.show(childFragmentManager, "confirmDeletion")
                true
            }
            R.id.actionSortByDueDate -> {
                inAscendingDateOrder = !inAscendingDateOrder
                if (inAscendingDateOrder) {
                    tasks.sortTasks(
                        compareBy<Task> { it.isChecked }
                            .thenBy { it.calendar }
                            .thenBy { it.title },
                        compareBy<TaskItem> { it.isChecked }
                            .thenBy { it.dateTime }
                            .thenBy { it.title }
                    )
                } else {
                    tasks.sortTasks(
                        compareBy<Task> { it.isChecked }.reversed()
                            .thenBy { it.calendar }
                            .thenBy { it.title }.reversed(),
                        compareBy<TaskItem> { it.isChecked }.reversed()
                            .thenBy { it.dateTime }
                            .thenBy { it.title }.reversed()

                    )
                }
                true
            }
            R.id.actionSortByAlphabetical -> {
                inAscendingAlphabeticalOrder = !inAscendingAlphabeticalOrder
                if (inAscendingAlphabeticalOrder) {
                    tasks.sortTasks(
                        compareBy<Task> { it.isChecked }.thenBy { it.title },
                        compareBy<TaskItem> { it.isChecked }.thenBy { it.title })
                } else {
                    tasks.sortTasks(
                        compareBy<Task> { it.isChecked }.reversed().thenBy { it.title }.reversed(),
                        compareBy<TaskItem> { it.isChecked }.reversed().thenBy { it.title }
                            .reversed()
                    )
                }
                true
            }
            else -> false
        }
    }

    override fun onPause() {
        super.onPause()
        addTaskButton.hide()
    }

    private fun toggleShowDoneTasks(isChecked: Boolean) {
        if (isChecked) {
            itemAdapter.setNewList(tasks.items)
        } else {
            itemAdapter.setNewList(tasks.items.filter { it.isChecked == false })
        }
        fastAdapter.notifyDataSetChanged()
    }

    override fun onAddTaskDialogPositiveClick(dialog: DialogFragment, task: Task) {
        task.id = tasks.getLastId() + 1
        tasks.addTask(task)
    }

    override fun onModifyTaskDialogPositiveClick(dialog: DialogFragment, task: Task) {
        tasks.updateTask(task)
    }

    override fun onModifyTaskDialogNegativeClick(dialog: DialogFragment, task: Task) {
        val list = mutableListOf<Task>()
        list.add(task)
        tasks.removeTasks(list)
    }

    override fun confirmDeletionDialogPositiveClick(dialog: DialogFragment) {
        val checkedTasks = mutableListOf<Task>()
        tasks.tasks.forEach { if (it.isChecked) checkedTasks.add(it) }

        if (!showDoneTasks) toggleShowDoneTasks(!showDoneTasks)
        tasks.removeTasks(checkedTasks)
        if (!showDoneTasks) toggleShowDoneTasks(showDoneTasks)
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        tasks.moveTask(oldPosition, newPosition)
        return true
    }

    override fun itemSwiped(position: Int, direction: Int) {
        val item = fastAdapter.getItem(position) ?: return
        item.swipedDirection = direction

        val message = Random().nextInt()
        deleteHandler.sendMessageDelayed(
            Message.obtain().apply { what = message; obj = item },
            3000
        )

        item.swipedAction = Runnable {
            deleteHandler.removeMessages(message)

            item.swipedDirection = 0
            val position = itemAdapter.getAdapterPosition(item)
            if (position != RecyclerView.NO_POSITION) {
                fastAdapter.notifyItemChanged(position)
            }
        }
        fastAdapter.notifyItemChanged(position)
    }
}