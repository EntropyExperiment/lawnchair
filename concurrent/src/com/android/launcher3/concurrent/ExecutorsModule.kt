/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.concurrent

import com.android.launcher3.concurrent.annotations.Background
import com.android.launcher3.concurrent.annotations.LightweightBackground
import com.android.launcher3.concurrent.annotations.LightweightBackgroundPriority
import com.android.launcher3.concurrent.annotations.ThreadPool
import com.android.launcher3.concurrent.annotations.Ui

import com.android.launcher3.concurrent.annotations.UiContext
import com.android.launcher3.concurrent.annotations.LightweightBackgroundContext
import com.android.launcher3.concurrent.annotations.BackgroundContext
import com.android.launcher3.concurrent.annotations.ThreadPoolContext

import com.google.common.util.concurrent.ListeningExecutorService

import dagger.Binds
import dagger.Module
import dagger.Provides

import kotlin.coroutines.CoroutineContext
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import javax.inject.Singleton

import kotlinx.coroutines.asCoroutineDispatcher

/**
 * Module that stipulates the executors that are usable by common launcher
 * modules.
 *
 * This module does not provide the executors directly, but rather provides
 * the qualifiers that are used to inject the executors and their derivatives
 * when modules themselves need to utilize them.
 *
 * The executors are provided as follows:
 * - @ThreadPool: Provides an unordered background executor or derivative
 * backed by a thread pool.
 * - @Background: Provides an ordered background executor or derivative.
 * - @LightweightBackground: Provides a lightweight background executor or
 * derivative that is either time sensitive or not depending on the
 * [LightweightBackgroundPriority] flag.
 * - @Ui: Provides a UI executor or derivative.
 *
 * The executors are provided as [Executor], [ExecutorService] or
 * [ListeningExecutorService].
 *
 * Implementations for the executors should be provided as
 * [ListeningExecutorService] by the host.
 */
@Module
interface ExecutorsModule {

    // The following methods provide the CoroutineContext for the executors.
    companion object {

        @Provides
        @UiContext
        fun provideUiContext(
          @Ui executor: Executor
        ): CoroutineContext {
            return executor.asCoroutineDispatcher()
        }

        @Provides
        @LightweightBackgroundContext(LightweightBackgroundPriority.DATA)
        fun provideDataLightweightContext(
            @LightweightBackground(LightweightBackgroundPriority.DATA)
            executor: Executor
        ): CoroutineContext {
            return executor.asCoroutineDispatcher()
        }

        @Provides
        @LightweightBackgroundContext(LightweightBackgroundPriority.UI)
        fun provideUiLightweightContext(
            @LightweightBackground(LightweightBackgroundPriority.UI)
            executor: Executor
        ): CoroutineContext {
            return executor.asCoroutineDispatcher()
        }

        @Provides
        @BackgroundContext
        fun provideBackgroundContext(
            @Background executor: Executor
        ): CoroutineContext {
            return executor.asCoroutineDispatcher()
        }

        @Provides
        @ThreadPoolContext
        fun provideThreadPoolContext(
            @ThreadPool executor: Executor
        ): CoroutineContext {
            return executor.asCoroutineDispatcher()
        }
    }
}
