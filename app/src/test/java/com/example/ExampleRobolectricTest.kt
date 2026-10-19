package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ApiKeyStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name and developer name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    val developer = context.getString(R.string.developer_name)
    assertEquals("Alic AI", appName)
    assertEquals("Rohit Raj", developer)
  }

  @Test
  fun `verify api key masking`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = ApiKeyStorage(context)
    val masked = storage.maskKey("AIzaSyB1234567890XYZW")
    assertTrue(masked.startsWith("AIzaSy"))
    assertTrue(masked.endsWith("XYZW"))
    assertTrue(masked.contains("••••••••"))
  }
}
