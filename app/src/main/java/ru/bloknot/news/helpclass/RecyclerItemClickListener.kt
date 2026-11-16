package ru.bloknot.news.helpclass

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(
    context: Context,
    private val recyclerView: RecyclerView,
    private val listener: OnItemClickListener?
) : RecyclerView.OnItemTouchListener {

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean = true

            override fun onLongPress(e: MotionEvent) {
                recyclerView.findChildViewUnder(e.x, e.y)?.let { child ->
                    listener?.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        }
    )

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && listener != null && gestureDetector.onTouchEvent(e)) {
            listener.onItemClick(childView, view.getChildAdapterPosition(childView))
            return true
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) = Unit

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onLongItemClick(view: View, position: Int)
    }
}