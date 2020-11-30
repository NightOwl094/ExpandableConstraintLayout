package com.nightowl094.lib

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
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
        { containerView: ExpandableConstraintLayout, oldHeight: Int, newHeight: Int ->
            ValueAnimator.ofInt(oldHeight, newHeight)
                .apply {
                    duration = containerView.duration
                }.also {
                    var isStarted = false

                    it.addUpdateListener { va ->
                        containerView.layoutParams.height = va.animatedValue as Int
                        containerView.requestLayout()

                        onLayoutStateChangeListener?.run {
                            if (isStarted.not()) {
                                onAnimationStart()
                                isStarted = true
                            }

                            this.onAnimation()

                            if ((va.animatedValue as Int) == newHeight) {
                                onAnimationEnd()

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

    override fun foldLayout() {
        oldFoldedHeight?.let { oldFoldedHeight ->
            if (oldFoldedHeight < this.height) {
                oldExpandedHeight = this.height

                startHeightChangeAnimation(oldFoldedHeight)
            }
        }
    }

    override fun foldLayout(height: Int) {
        if (height < this.height) {
            oldExpandedHeight = this.height
            oldFoldedHeight = height
            startHeightChangeAnimation(height)
        }
    }

    override fun foldLayoutById(@IdRes targetViewId: Int) {
        findYPositionById(targetViewId).run {
            foldLayout(this)
        }
    }


    override fun expandLayout() {
        oldExpandedHeight?.let { oldExpandedHeight ->
            if (oldExpandedHeight > this.height) {
                oldFoldedHeight = this.height

                startHeightChangeAnimation(oldExpandedHeight)
            }
        }
    }

    override fun expandLayout(height: Int) {
        if (height > this.height) {
            oldFoldedHeight = this.height
            oldExpandedHeight = height
            startHeightChangeAnimation(height)
        }
    }

    override fun invalidateLayout(callback: (() -> Unit)?) {
        willSetFirstHeight(callback)
    }

    override fun toggleLayout() {
        if (toggleControl) {
            toggleControl = false

            if (isCollapsed)
                foldLayout()
            else
                expandLayout()
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

    private fun startHeightChangeAnimation(height: Int) {
        getValueAnimator(this, this.height, height).start()
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