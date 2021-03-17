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

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.DirectExecutor
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
// [START guava_import]
import com.google.common.util.concurrent.ListenableFuture
// [END guava_import]
// [START rx_import]
import io.ashdavies.rx.rxtasks.toSingle
import java.util.concurrent.TimeUnit
// [END rx_import]
import kotlinx.coroutines.launch
// [START ktx_import]
import kotlinx.coroutines.tasks.await
// [END ktx_import]

@Keep
class MainActivity : AppCompatActivity() {
  private val TAG = "MainActivity"
  private var listenableFuture: ListenableFuture<String>? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val textView = findViewById<TextView>(R.id.textview_first)

    // Simple Google Play services Task example
    findViewById<Button>(R.id.tasks_button).setOnClickListener {
      textView.text = getString(R.string.waiting_txt)
      // [START simple_task]
      // [START_EXCLUDE]
      val simpleTask = createSimpleTask(getString(R.string.tasks_txt))
      // [END_EXCLUDE]
      simpleTask.addOnCompleteListener(this) {
        completedTask -> textView.text = completedTask.result
      }
      // [END simple_task]
    }

    // Kotlin Coroutine Task example
    findViewById<Button>(R.id.ktx_button).setOnClickListener {
      textView.text = getString(R.string.waiting_txt)
      // [START ktx_task]
      // [START_EXCLUDE]
      val simpleTask = createSimpleTask(getString(R.string.ktx_txt))
      // Jetpack lifecycle library
      lifecycleScope.launch {
        // [END_EXCLUDE]
        textView.text = simpleTask.await()
      }
      // [END ktx_task]
    }

    // Simple Guava example
    findViewById<Button>(R.id.guava_button).setOnClickListener {
      textView.text = getString(R.string.waiting_txt)
      // [START guava_task]
      // [START_EXCLUDE]
      val simpleTask = createSimpleTask(getString(R.string.guava_txt))
      // [END_EXCLUDE]
      this.listenableFuture = taskToListenableFuture(simpleTask)
      this.listenableFuture?.addListener(
        Runnable {
          textView.text = listenableFuture?.get()
        },
        ContextCompat.getMainExecutor(this)
      )
      // [END guava_task]
    }

    // Simple RX Java example
    findViewById<Button>(R.id.rx_button).setOnClickListener {
      textView.text = getString(R.string.waiting_txt)
      // [START rx_task]
      // [START_EXCLUDE]
      val simpleTask = createSimpleTask(getString(R.string.rx_txt))
      // [END_EXCLUDE]
      simpleTask.toSingle(this).subscribe { result -> textView.text = result }
      // [END rx_task]
    }

    findViewById<Button>(R.id.license_button).setOnClickListener {
      startActivity(Intent(this, OssLicensesMenuActivity::class.java))
    }
  }

  override fun onStop() {
    super.onStop()
    this.listenableFuture?.cancel(/*mayInterruptIfRunning=*/false)
  }
}

// [START guava_conversion]
/** Convert Task to ListenableFuture. */
fun <T> taskToListenableFuture(task: Task<T>): ListenableFuture<T> {
  return CallbackToFutureAdapter.getFuture { completer ->
    task.addOnCompleteListener { completedTask ->
      if (completedTask.isCanceled) {
        completer.setCancelled()
      } else if (completedTask.isSuccessful) {
        completer.set(completedTask.result)
      } else {
        val e = completedTask.exception
        if (e != null) {
          completer.setException(e)
        } else {
          throw IllegalStateException()
        }
      }
    }
  }
}
// [END guava_conversion]

/** Extension on Task for conversion to ListenableFuture. */
@Keep
fun <T> Task<T>.toListenableFuture() = taskToListenableFuture(this)

/** Create a simple task on new thread that waits 1 second. */
@Keep
fun createSimpleTask(value: String) = TaskCompletionSource<String>().apply {
  Thread {
    Thread.sleep(TimeUnit.SECONDS.toMillis(1))
    setResult(value)
  }.start()
}.task
