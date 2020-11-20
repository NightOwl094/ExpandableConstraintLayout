package com.nightowl094.lib

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.constraintlayout.widget.ConstraintLayout

class ExpandableConstraintLayout
@JvmOverloads constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int = 0) :
    ConstraintLayout(context!!, attrs, defStyleAttr), ExpandalbeConstraintLayoutUseCase {

    private var defaultHeight: Int = 0
    private var oldExpandedHeight: Int? = null
    private var oldFoldedHeight: Int? = null
    private var isCollapsed = false
    var duration: Long = 400
    var onLayoutStateChangeListener: OnLayoutStateChangeListener? = null

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

                            if ((va.animatedValue as Int) == newHeight)
                                onAnimationEnd()

                        }
                    }
                }
        }


    init {
        inflate(context, R.layout.custom_expandable_constraint_layout, this)

        // 화면이 배치된 후 height 저장
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                this@ExpandableConstraintLayout.height.run {
                    defaultHeight = this
                    oldExpandedHeight = this
                }

                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

    }

    override fun foldLayout() {
        oldFoldedHeight?.run {
            startHeightChangeAnimation(this)
            isCollapsed = false
        }
    }

    override fun foldLayout(height: Int) {
        if (height < this.height) {
            oldExpandedHeight = this.height
            oldFoldedHeight = height
            startHeightChangeAnimation(height)

            isCollapsed = false
        }
    }

    override fun foldLayoutById(targetViewId: Int) {
        this.findViewById<View>(targetViewId).apply {
            foldLayout((y + this.height).toInt())
        }
    }

    override fun expandLayout() {
        oldExpandedHeight?.run {
            startHeightChangeAnimation(this)
            isCollapsed = true
        }
    }

    override fun expandLayout(height: Int) {
        if (height > this.height) {
            oldFoldedHeight = this.height
            oldExpandedHeight = height
            startHeightChangeAnimation(height)

            isCollapsed = true
        }
    }

    override fun invalidateLayout() {
        oldExpandedHeight = null
        oldFoldedHeight = null
        startHeightChangeAnimation(defaultHeight)
        isCollapsed = false
    }

    override fun toggleLayout() {
        if (isCollapsed)
            foldLayout()
        else
            expandLayout()
    }

    private fun startHeightChangeAnimation(height: Int) {
        getValueAnimator(this, this.height, height).start()
    }

}