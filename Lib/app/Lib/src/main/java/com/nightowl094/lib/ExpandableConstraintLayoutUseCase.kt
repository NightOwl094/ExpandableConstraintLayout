package com.nightowl094.lib

import androidx.annotation.IdRes

interface ExpandableConstraintLayoutUseCase {
    val isExpanded: Boolean
    fun foldLayout()
    fun foldLayout(height: Int)
    fun foldLayoutById(@IdRes targetViewId: Int)
    fun expandLayout()
    fun expandLayout(height: Int)
    fun invalidateLayout()
    fun toggleLayout()
}