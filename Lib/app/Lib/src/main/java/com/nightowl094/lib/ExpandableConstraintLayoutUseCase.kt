package com.nightowl094.lib

import androidx.annotation.IdRes

interface ExpandableConstraintLayoutUseCase {
    val isExpanded: Boolean
    fun invalidateLayout(callback: (() -> Unit)? = null)
    fun expandByLastChild()
    fun expandLayout(height: Int, duration: Long? = null)
    fun expandLayout(duration: Long? = null)
    fun foldLayoutById(@IdRes targetViewId: Int, duration: Long? = null)
    fun foldLayout(height: Int, duration: Long? = null)
    fun foldLayout(duration: Long? = null)
    fun toggleLayout(duration: Long? = null)
}