package com.example.rijekabusapp.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NestedViewPager
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : ViewPager(context, attrs) {
        private var isParentInterceptingTouch = false

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            if (isParentInterceptingTouch) {
                return false
            }
            return try {
                super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        fun setParentInterceptingTouch(isIntercepting: Boolean) {
            isParentInterceptingTouch = isIntercepting
        }

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            parent.requestDisallowInterceptTouchEvent(true)
            return super.dispatchTouchEvent(ev)
        }
    }
