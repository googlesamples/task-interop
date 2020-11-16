/* Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.taskinterop

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 * Testing conversion from Task to ListenableFuture
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TaskListenableFutureTest {
  @Test
  fun taskToListenableFuture() {
    val successText = "Successful ListenableFuture Conversion"
    val simpleTask = createSimpleTask(successText)
    // Note: ListenableFuture.get() call is blocking, so you can't make it on the main thread,
    // but it is ok here for testing.
    assertEquals(successText, simpleTask.toListenableFuture().get())
  }
}
