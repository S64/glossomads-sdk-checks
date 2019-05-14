package jp.s64.example.test.glossomads

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import com.glossomads.listener.GlossomAdsAdAvailabilityListener
import com.glossomads.listener.GlossomAdsAdListener
import com.glossomads.listener.GlossomAdsAdRewardListener
import com.glossomads.sdk.GlossomAds
import com.glossomads.sdk.GlossomAdsAdReward
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import jp.s64.example.test.glossomads.databinding.ActivityMainBinding
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MainActivity : AppCompatActivity() {

    companion object {

        private const val CLIENT_OPTIONS = "" // TODO

        private val LOG_TIME_FORMAT = DateFormat("HH:mm:ss.SSS")

    }

    @VisibleForTesting
    lateinit var binding: ActivityMainBinding

    @VisibleForTesting
    var interceptor: EventInterceptor? = null

    private val lock = ReentrantLock()

    private val availabilityListener = object : GlossomAdsAdAvailabilityListener {

        override fun onGlossomAdsAdAvailabilityChange(p0: String?, p1: Boolean) {
            lock.withLock {
                binding.isAvailable = p1
                log(Event("onGlossomAdsAdAvailabilityChange", binding.isAvailable))
            }
        }

    }

    private val rewardListener = object : GlossomAdsAdRewardListener {

        override fun onGlossomAdsAdReward(p0: GlossomAdsAdReward?) {
            lock.withLock {
                binding.isRewarded = p0?.success()
                log(Event("onGlossomAdsAdReward", binding.isRewarded))
            }
        }

    }

    private val adListener = object : GlossomAdsAdListener {

        override fun onGlossomAdsVideoSkip(p0: String?) {
            binding.isSkip = true
            log(Event("onGlossomAdsVideoSkip"))
        }

        override fun onGlossomAdsVideoClose(p0: String?) {
            binding.isClosed = true
            log(Event("onGlossomAdsVideoClose"))
        }

        override fun onGlossomAdsVideoClick(p0: String?) {
            binding.isClicked = true
            log(Event("onGlossomAdsVideoClick"))
        }

        override fun onGlossomAdsVideoStart(p0: String?) {
            binding.isStarted = true
            log(Event("onGlossomAdsVideoStart"))
        }

        override fun onGlossomAdsVideoFinish(p0: String?, p1: Boolean) {
            binding.isFinish = true
            log(Event("onGlossomAdsVideoFinish"))
        }

        override fun onGlossomAdsVideoPause(p0: String?) {
            binding.isPause = true
            log(Event("onGlossomAdsVideoPause"))
        }

        override fun onGlossomAdsVideoResume(p0: String?) {
            binding.isResume = true
            log(Event("onGlossomAdsVideoResume"))
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.logs = ""
        GlossomAds.setAdRewardListener(rewardListener)
        GlossomAds.addAdAvailabilityListener(availabilityListener)

        refreshState()

        binding.configureButton.setOnClickListener {
            if (!GlossomAds.isConfigured()) {
                GlossomAds.configure(
                    this@MainActivity,
                    CLIENT_OPTIONS,
                    BuildConfig.ADCORSA_APP_ID,
                    BuildConfig.ADCORSA_ZONE_ID
                )
                refreshState()
            }
        }

        binding.showButton.setOnClickListener {
            GlossomAds.showRewardVideo(BuildConfig.ADCORSA_ZONE_ID, adListener)
        }
    }

    private fun refreshState() {
        lock.withLock {
            binding.isConfigured = GlossomAds.isConfigured()
            log(Event("GlossomAds.isConfigured", binding.isConfigured))
        }
    }

    private fun log(e: Event) {
        lock.withLock {
            binding.logs += "[${e.time.format(LOG_TIME_FORMAT)}] ${e.eventName} (${e.option})${System.lineSeparator()}"
            interceptor?.onEvent(e)
        }
    }

    data class Event(
        val eventName: String,
        val option: Boolean? = null,
        val time: DateTime = DateTime.now()
    )

    @VisibleForTesting
    interface EventInterceptor {

        fun onEvent(e: Event)

    }

}
