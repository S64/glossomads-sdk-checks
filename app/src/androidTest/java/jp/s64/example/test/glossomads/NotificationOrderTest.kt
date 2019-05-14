package jp.s64.example.test.glossomads

import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.glossomads.sdk.GlossomAdsFullScreen
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@RunWith(AndroidJUnit4::class)
class NotificationOrderTest {

    @Test
    fun test() {
        val lock = ReentrantLock()
        val count = CountDownLatch(6)

        launch(MainActivity::class.java).use {

            it.onActivity { activity ->
                activity.interceptor = object : MainActivity.EventInterceptor {

                    override fun onEvent(e: MainActivity.Event) {
                        when (e.eventName) {
                            "GlossomAds.isConfigured" -> lock.withLock {
                                    assertThat(e.option, `is`(true))
                                    assertThat(count.count, `is`(6L))
                                    count.countDown()
                            }
                            "onGlossomAdsAdAvailabilityChange" -> lock.withLock {
                                if (count.count == 5L) {
                                    assertThat(e.option, `is`(true))
                                    assertThat(count.count, `is`(5L))
                                    count.countDown()
                                    activity.binding.showButton.performClick()
                                }
                            }
                            "onGlossomAdsVideoStart" -> lock.withLock {
                                assertThat(count.count, `is`(4L))
                                count.countDown()
                            }
                            "onGlossomAdsVideoFinish" -> {
                                lock.withLock {
                                    assertThat(count.count, `is`(3L))
                                }

                                Handler(activity.mainLooper).postDelayed(
                                    {
                                        lock.withLock {
                                            val ad = ActivityLifecycleMonitorRegistry.getInstance()
                                                .getActivitiesInStage(Stage.RESUMED)
                                                .find { it is GlossomAdsFullScreen }
                                            assertThat(ad, `is`(notNullValue()))

                                            val close = ad!!.findViewById<ViewGroup>(android.R.id.content)
                                                .getChildAt(0).let { it as ViewGroup }
                                                .getChildAt(0).let { it as ViewGroup }
                                                .getChildAt(0).let { it as ViewGroup }
                                                .getChildAt(2).let { it as ViewGroup }
                                                .getChildAt(2).let { it as View }

                                            assertThat(close, `is`(instanceOf(ImageButton::class.java)))

                                            count.countDown()
                                            close.performClick()
                                        }
                                    },
                                    1000 * 2
                                )
                            }
                            "onGlossomAdsVideoClose" -> lock.withLock {
                                assertThat(count.count, `is`(2L)) // FIXME: 本来は1になる
                                count.countDown()
                            }
                            "onGlossomAdsAdReward" -> lock.withLock {
                                assertThat(count.count, `is`(1L)) // FIXME: 本来は2になる
                                count.countDown()
                            }
                            else -> Log.d("NotificationOrderTest", e.toString())
                        }
                    }

                }

                activity.binding.configureButton.performClick()
            }

            count.await(40, TimeUnit.SECONDS)
        }
    }

}
