package jp.s64.example.test.glossomads

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigurationTest {

    @Test
    fun testConfiguration() {
        assertThat("Defined `adcorsa.appId` in local.properties", BuildConfig.ADCORSA_APP_ID, not(nullValue()))
        assertThat("Defined `adcorsa.zoneId` in local.properties", BuildConfig.ADCORSA_ZONE_ID, not(nullValue()))
    }

}
