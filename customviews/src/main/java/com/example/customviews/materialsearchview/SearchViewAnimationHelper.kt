package com.example.customviews.materialsearchview

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.example.customviews.R
import com.example.customviews.materialsearchview.MaterialSearchView.TransitionState
import kotlin.math.hypot

class SearchViewAnimationHelper(private val searchView: MaterialSearchView) {
    private val rootView = searchView.findViewById<View>(R.id.search_view_root)
    private val searchBar = searchView.findViewById<View>(R.id.search_bar)
    private val contentContainer = searchView.findViewById<View>(R.id.content_container)
    private val window = (searchView.context as Activity).window

    private val colorSurface = searchView.context.resolveAttribute(android.R.attr.statusBarColor)
    private val colorSurfaceContainer = searchView.context.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainer)

    fun show() {
        val cx = searchBar.width
        val cy = searchBar.height / 2
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        val statusBarAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorSurface, colorSurfaceContainer).apply {
            duration = CIRCLE_REVEAL_DURATION_MS
            addUpdateListener {
                window.statusBarColor = it.animatedValue as Int
            }
        }

        val revealAnimator = ViewAnimationUtils.createCircularReveal(searchBar, cx, cy, 0f, finalRadius).apply {
            duration = CIRCLE_REVEAL_DURATION_MS
            doOnStart {
                rootView.visibility = View.VISIBLE
                searchView.setTransitionState(TransitionState.SHOWING)
            }
            doOnEnd {
                searchView.setTransitionState(TransitionState.SHOWN)
            }
        }

        val contentAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = CIRCLE_REVEAL_DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                contentContainer.alpha = it.animatedValue as Float
            }
        }

        statusBarAnimator.start()
        revealAnimator.start()
        contentAnimator.start()
    }

    fun hide() {
        val cx = searchBar.width
        val cy = searchBar.height / 2
        val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        val statusBarAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorSurfaceContainer, colorSurface).apply {
            duration = CIRCLE_REVEAL_DURATION_SHORT_MS
            addUpdateListener {
                window.statusBarColor = it.animatedValue as Int
            }
        }

        val revealAnimator = ViewAnimationUtils.createCircularReveal(searchBar, cx, cy, initialRadius, 0f).apply {
            duration = CIRCLE_REVEAL_DURATION_SHORT_MS
            doOnStart {
                searchView.setTransitionState(TransitionState.HIDING)
            }
            doOnEnd {
                rootView.visibility = View.GONE
                searchView.setTransitionState(TransitionState.HIDDEN)
            }
        }

        val contentAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = CIRCLE_REVEAL_DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                contentContainer.alpha = it.animatedValue as Float
            }
        }

        statusBarAnimator.start()
        revealAnimator.start()
        contentAnimator.start()
    }

    companion object {
        const val CIRCLE_REVEAL_DURATION_MS = 200L
        const val CIRCLE_REVEAL_DURATION_SHORT_MS = 100L
    }
}