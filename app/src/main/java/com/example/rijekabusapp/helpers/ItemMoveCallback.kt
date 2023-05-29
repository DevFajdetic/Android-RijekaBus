package com.example.rijekabusapp.helpers

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemMoveCallback(
    private val adapter: ItemTouchHelperAdapter,
    private val isEditModeEnabled: () -> Boolean
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (isEditModeEnabled.invoke()) {
            val sourcePosition = viewHolder.adapterPosition
            val targetPosition = target.adapterPosition

            adapter.onItemMove(sourcePosition, targetPosition)
            return true
        }
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not used in this case
    }

    interface ItemTouchHelperAdapter {
        fun onItemMove(sourcePosition: Int, targetPosition: Int)
    }
}
