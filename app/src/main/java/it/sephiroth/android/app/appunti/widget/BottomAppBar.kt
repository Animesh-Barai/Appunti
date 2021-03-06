package it.sephiroth.android.app.appunti.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.snackbar.Snackbar
import it.sephiroth.android.app.appunti.R
import kotlinx.android.synthetic.main.appunti_main_bottomappbar.view.*


class BottomAppBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    var hideOnScroll = false

    private var newNoteListener: (() -> Unit)? = null
    private var displayAsListChangeListener: ((value: Boolean) -> Unit)? = null

    fun doOnNewNoteClick(action: () -> Unit) {
        newNoteListener = action
    }

    fun doOnDisplayAsListChanged(action: (Boolean) -> Unit) {
        displayAsListChangeListener = action
    }

    fun setDisplayAsList(value: Boolean) {
        buttonDisplayAsList.isChecked = value
    }

    init {
        orientation = LinearLayout.HORIZONTAL
    }

    override fun onFinishInflate() {
        super.onFinishInflate()


        buttonDisplayAsList.doOnCheckedChanged { value ->
            displayAsListChangeListener?.invoke(value)
        }

        buttonNewNote.setOnClickListener {
            newNoteListener?.invoke()
        }
    }

    private fun findDependent(): View? {
        if (this.parent !is CoordinatorLayout) {
            return null
        } else {
            val dependents = (this.parent as CoordinatorLayout).getDependents(this)
            val var2 = dependents.iterator()

            while (var2.hasNext()) {
                val v: View = var2.next()
                if ((v.layoutParams as CoordinatorLayout.LayoutParams).anchorId == id) return v
                if (v is FrameLayout) return v
                if (v is Snackbar.SnackbarLayout) return v
            }

            return null
        }
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<BottomAppBar> {
        return BottomAppBar.Behavior(context)
    }

    class Behavior(context: Context) : HideBottomViewOnScrollBehavior<BottomAppBar>() {

        private var shadowHeight: Int = 0
        private val snackBarBottomMargin: Int

        init {
            shadowHeight = context.resources.getDimensionPixelSize(R.dimen.appunti_bottomappbar_shadow_height)
            snackBarBottomMargin = context.resources.getDimensionPixelSize(R.dimen.appunti_snackbar_appbar_bottom_margin)
        }

        override fun onLayoutChild(parent: CoordinatorLayout, child: BottomAppBar, layoutDirection: Int): Boolean {
//            Timber.i("onLayoutChild(layoutDirection=$layoutDirection)")

            val view = child.findDependent()
            view?.let { view ->
                val params = view.layoutParams as CoordinatorLayout.LayoutParams
//                Timber.d("found dependent:$view,  child.height=${child.height}, bottomMargin=${params.bottomMargin}")
                params.bottomMargin = child.measuredHeight - shadowHeight
                view.layoutParams = params
            }

            parent.onLayoutChild(child, layoutDirection)
            return super.onLayoutChild(parent, child, layoutDirection)
        }

        override fun layoutDependsOn(parent: CoordinatorLayout, child: BottomAppBar, dependency: View): Boolean {
            if (dependency is Snackbar.SnackbarLayout) return true
            return super.layoutDependsOn(parent, child, dependency)
        }

        override fun onDependentViewChanged(parent: CoordinatorLayout, child: BottomAppBar, dependency: View): Boolean {
//            Timber.i("onDependentViewChanged($dependency)")

            if (dependency is Snackbar.SnackbarLayout) {
                val params = dependency.layoutParams as CoordinatorLayout.LayoutParams
                params.bottomMargin = child.measuredHeight - shadowHeight + snackBarBottomMargin
                dependency.layoutParams = params
                return true
            }

            return super.onDependentViewChanged(parent, child, dependency)
        }

        override fun onDependentViewRemoved(parent: CoordinatorLayout, child: BottomAppBar, dependency: View) {
//            Timber.i("onDependentViewRemoved($dependency)")
            super.onDependentViewRemoved(parent, child, dependency)
        }

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: BottomAppBar, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
            return child.hideOnScroll && super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        }

        override fun slideUp(child: BottomAppBar) {
//            Timber.i("slideUp")
            super.slideUp(child)
        }

        override fun slideDown(child: BottomAppBar) {
//            Timber.i("slideDown")
            super.slideDown(child)
        }
    }
}