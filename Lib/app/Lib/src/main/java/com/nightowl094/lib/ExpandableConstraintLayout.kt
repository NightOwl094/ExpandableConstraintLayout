package com.nightowl094.lib

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use

class ExpandableConstraintLayout
@JvmOverloads constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int = 0) :
    ConstraintLayout(context!!, attrs, defStyleAttr), ExpandableConstraintLayoutUseCase {

    private var defaultHeight: Int = 0
    private var oldExpandedHeight: Int? = null
    private var oldFoldedHeight: Int? = null
    private var isCollapsed = false
    private var toggleControl = true
    var firstTargetId: String? = null
    var duration: Long = 400
    var onLayoutStateChangeListener: OnLayoutStateChangeListener? = null

    override val isExpanded: Boolean
        get() = isCollapsed

    private val getValueAnimator =
        { containerView: ExpandableConstraintLayout, oldHeight: Int, newHeight: Int, animationTime: Long? ->
            ValueAnimator.ofInt(oldHeight, newHeight)
                .apply {
                    duration = animationTime?.run {
                        this
                    } ?: containerView.duration

                }.also {
                    var isStarted = false

                    it.addUpdateListener { va ->
                        containerView.layoutParams.height = va.animatedValue as Int
                        containerView.requestLayout()

                        onLayoutStateChangeListener?.run {
                            if (isStarted.not()) {
                                isStarted = true

                                onAnimationStart()
                            }

                            this.onAnimation()

                            if ((va.animatedValue as Int) == newHeight) {
                                isCollapsed = isCollapsed.not()
                                toggleControl = true

                                onAnimationEnd()
                            }

                        } ?: run {
                            if ((va.animatedValue as Int) == newHeight) {
                                isCollapsed = isCollapsed.not()
                                toggleControl = true
                            }
                        }

                    }
                }
        }


    init {
        inflate(context, R.layout.custom_expandable_constraint_layout, this)

        context?.theme?.obtainStyledAttributes(attrs, R.styleable.ExpandableConstraintLayout, 0, 0)
            ?.use {
                it.getString(R.styleable.ExpandableConstraintLayout_first_target_view_id)
                    ?.let { firstTargetViewId ->
                        firstTargetId = firstTargetViewId
                    }
            }

        willSetFirstHeight()
    }

    override fun foldLayout(duration: Long?) {
        oldFoldedHeight?.let { oldFoldedHeight ->
            if (oldFoldedHeight < this.height) {
                oldExpandedHeight = this.height

                startHeightChangeAnimation(oldFoldedHeight, duration)
            }
        }
    }

    override fun foldLayout(height: Int, duration: Long?) {
        if (height < this.height) {
            oldExpandedHeight = this.height
            oldFoldedHeight = height
            startHeightChangeAnimation(height, duration)
        }
    }

    override fun foldLayoutById(@IdRes targetViewId: Int, duration: Long?) {
        findYPositionById(targetViewId).run {
            foldLayout(this, duration)
        }
    }


    override fun expandLayout(duration: Long?) {
        oldExpandedHeight?.let { oldExpandedHeight ->
            if (oldExpandedHeight > this.height) {
                oldFoldedHeight = this.height

                startHeightChangeAnimation(oldExpandedHeight, duration)
            }
        }
    }

    override fun expandLayout(height: Int, duration: Long?) {
        if (height > this.height) {
            oldFoldedHeight = this.height
            oldExpandedHeight = height
            startHeightChangeAnimation(height, duration)
        }
    }

    override fun expandByLastChild() {
        if (isCollapsed) return

        oldFoldedHeight = this.height

        this.layoutParams = layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        isCollapsed = true
    }

    override fun invalidateLayout(callback: (() -> Unit)?) {
        willSetFirstHeight(callback)
    }

    override fun toggleLayout(duration: Long?) {
        if (toggleControl) {
            toggleControl = false

            if (isCollapsed)
                foldLayout(duration)
            else
                expandLayout(duration)
        }
    }

    private fun findYPositionById(viewId: String) =
        this.findViewById<View>(findResourceByIdString(viewId, "id")).run {
            (y + height).toInt()
        }

    private fun findYPositionById(@IdRes viewId: Int) =
        this.findViewById<View>(viewId).run {
            (y + height).toInt()
        }

    private fun setHeight(height: Int) {
        this.layoutParams.height = height
        this.requestLayout()
    }

    private fun startHeightChangeAnimation(height: Int, duration: Long? = null) {
        getValueAnimator(this, this.height, height, duration).start()
    }

    private fun findResourceByIdString(id: String, type: String) =
        resources.getIdentifier(id, type, context.packageName)

    private fun willSetFirstHeight(callback: (() -> Unit)? = null) {
        this@ExpandableConstraintLayout.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    this@ExpandableConstraintLayout.height.let {
                        defaultHeight = it
                        oldExpandedHeight = it

                        firstTargetId?.run {
                            setHeight(findYPositionById(this))
                        }

                        isCollapsed = false

                        callback?.invoke()
                    }

                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

            layoutParams?.apply {
                height = LayoutParams.WRAP_CONTENT
            }

            requestLayout()
        }
    }

}