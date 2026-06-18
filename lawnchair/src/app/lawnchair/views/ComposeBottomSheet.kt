/*
 * Copyright 2026, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair.views

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import app.lawnchair.ui.theme.LawnchairTheme
import app.lawnchair.util.ProvideLifecycleState
import com.android.launcher3.Launcher
import com.android.launcher3.views.AbstractSlideInView
import com.android.launcher3.views.ActivityContext
import com.android.launcher3.views.BaseDragLayer

class ComposeBottomSheet<T>(context: Context) : AbstractSlideInView<T>(context, null, 0) where T : Context, T : ActivityContext {

    private val container = ComposeView(context)

    init {
        layoutParams = BaseDragLayer.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            .apply { ignoreInsets = true }
        mContent = container
        mNoIntercept = true
    }

    fun show() {
        val parent = parent
        if (parent is ViewGroup) {
            parent.removeView(this)
        }
        removeAllViews()
        addView(container)
        attachToContainer()
        mIsOpen = true
    }

    fun setContent(
        content: @Composable ComposeBottomSheet<T>.() -> Unit,
    ) {
        container.setContent {
            Providers {
                content(this)
            }
        }
    }

    override fun handleClose(animate: Boolean) {
        if (mActivityContext is Launcher) {
            mActivityContext.hideKeyboard()
        }
        mIsOpen = false
        val parent = parent
        if (parent is ViewGroup) {
            parent.removeView(this)
        }
    }

    override fun isOfType(type: Int): Boolean {
        return type and TYPE_COMPOSE_VIEW != 0
    }

    @Composable
    private fun Providers(
        content: @Composable () -> Unit,
    ) {
        LawnchairTheme {
            ProvideLifecycleState {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                ) {
                    content()
                }
            }
        }
    }

    companion object {
        fun <T> show(
            context: T,
            content: @Composable ComposeBottomSheet<T>.() -> Unit,
        ) where T : Context, T : ActivityContext {
            closeAllOpenViews(context)
            val view = ComposeBottomSheet<T>(context)
            view.setContent(content)
            view.show()
        }
    }
}
