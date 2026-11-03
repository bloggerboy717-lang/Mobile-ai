package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ApiKeyStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

  @Test
  fun `verify official gemini voice models exist for settings`() {
    val personas = com.example.persona.ALL_PERSONAS
    val names = personas.map { it.name }
    assertTrue(names.any { it.contains("Gemini Live") })
    assertTrue(names.any { it.contains("Aoede") })
    assertTrue(names.any { it.contains("Charon") })
    assertTrue(names.any { it.contains("Puck") })
    assertTrue(names.any { it.contains("Fenrir") })
    assertTrue(names.any { it.contains("Kore") })
  }

  @Test
  fun `verify sos trigger detection and cancellation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val emergencyStorage = com.example.data.EmergencyStorage(context)
    val profileStorage = com.example.data.StudentProfileStorage(context)
    val sosManager = com.example.sos.EmergencySosManager(context, emergencyStorage, profileStorage) {}

    assertTrue(sosManager.isSosTrigger("बचाओ बचाओ बचाओ"))
    assertTrue(sosManager.isSosTrigger("bachao bachao bachao"))
    assertTrue(sosManager.isSosTrigger("मुझे बचाओ बचाओ बचाओ जल्दी"))
    assertTrue(sosManager.isSosCancelTrigger("मैं सुरक्षित हूँ"))
    assertTrue(sosManager.isSosCancelTrigger("I am safe now"))
  }

  @Test
  fun `verify student profile memory and emergency contacts storage`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val profileStorage = com.example.data.StudentProfileStorage(context)
    val profile = com.example.data.StudentProfile(
      name = "Aman",
      gradeOrCourse = "Class 12th PCM",
      goal = "IIT JEE Advanced Rank < 500",
      weakSubject = "Physical Chemistry",
      isConfigured = true
    )
    profileStorage.saveProfile(profile)
    val retrieved = profileStorage.getProfile()
    assertEquals("Aman", retrieved.name)
    assertEquals("Class 12th PCM", retrieved.gradeOrCourse)
    assertEquals("IIT JEE Advanced Rank < 500", retrieved.goal)
    assertEquals("Physical Chemistry", retrieved.weakSubject)

    val emergencyStorage = com.example.data.EmergencyStorage(context)
    emergencyStorage.saveContact(1, "Father", "9876543210")
    emergencyStorage.saveContact(2, "Mother", "9876543211")
    emergencyStorage.saveContact(3, "Friend", "9876543212")
    val contacts = emergencyStorage.getContacts()
    assertEquals(3, contacts.size)
    assertEquals("Father", contacts[0].name)
    assertEquals("9876543210", contacts[0].phoneNumber)
  }

  @Test
  fun `verify code builder request detection`() {
    assertTrue(com.example.codebuilder.CodeBuilderEngine.isCodeBuildRequest("मेरे लिए एक पोर्टफोलियो वेबसाइट बनाओ"))
    assertTrue(com.example.codebuilder.CodeBuilderEngine.isCodeBuildRequest("create a calculator app in html and javascript"))
    assertTrue(com.example.codebuilder.CodeBuilderEngine.isCodeBuildRequest("make a responsive landing page website"))
  }

  @Test
  fun `verify theme presets and custom hex resolution`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val themeStorage = com.example.data.ThemeStorage(context)
    assertEquals(com.example.data.ThemePreset.SUNSET, themeStorage.getThemePreset())

    themeStorage.saveThemePreset(com.example.data.ThemePreset.RED)
    assertEquals(com.example.data.ThemePreset.RED, themeStorage.getThemePreset())

    themeStorage.saveCustomHex("#123456")
    assertEquals("#123456", themeStorage.getCustomHex())

    val sunsetColors = com.example.ui.theme.resolveThemeColors(com.example.data.ThemePreset.SUNSET, "#FFF4EC")
    assertEquals(com.example.data.ThemePreset.SUNSET, sunsetColors.preset)
    assertEquals(false, sunsetColors.isDark)

    val redColors = com.example.ui.theme.resolveThemeColors(com.example.data.ThemePreset.RED, "#8E6B74")
    assertEquals(com.example.data.ThemePreset.RED, redColors.preset)
    assertEquals(true, redColors.isDark)

    val customColors = com.example.ui.theme.createCustomThemeColors("#FFFFFF")
    assertEquals(false, customColors.isDark)
  }

  @Test
  fun `verify sub-settings sections exist and have English titles`() {
    val sections = com.example.ui.screens.settings.SettingsSection.values()
    assertEquals(7, sections.size)
    val titles = sections.map { it.title }
    assertTrue(titles.contains("ORB & Theme Settings"))
    assertTrue(titles.contains("Student Profile"))
    assertTrue(titles.contains("Voice Models"))
    assertTrue(titles.contains("Emergency SOS"))
    assertTrue(titles.contains("AI & Model Settings"))
    assertTrue(titles.contains("Permissions Center"))
    assertTrue(titles.contains("About ALIC-AI"))
  }

  @Test
  fun `verify voice call and SMS action parsing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = com.example.agent.AgentActionEngine(context)

    val answerAction1 = engine.parseUserIntent("फोन उठाओ")
    assertNotNull(answerAction1)
    assertEquals(com.example.agent.ActionType.ANSWER_CALL, answerAction1?.type)

    val answerAction2 = engine.parseUserIntent("call uthao please")
    assertNotNull(answerAction2)
    assertEquals(com.example.agent.ActionType.ANSWER_CALL, answerAction2?.type)

    val endAction1 = engine.parseUserIntent("फोन काटो")
    assertNotNull(endAction1)
    assertEquals(com.example.agent.ActionType.END_CALL, endAction1?.type)

    val endAction2 = engine.parseUserIntent("call cut karo")
    assertNotNull(endAction2)
    assertEquals(com.example.agent.ActionType.END_CALL, endAction2?.type)

    val sendSmsAction = engine.parseUserIntent("मैसेज भेजो 9876543210 को हेलो")
    assertNotNull(sendSmsAction)
    assertEquals(com.example.agent.ActionType.SEND_SMS, sendSmsAction?.type)

    val readSmsAction = engine.parseUserIntent("मैसेज पढ़ो")
    assertNotNull(readSmsAction)
    assertEquals(com.example.agent.ActionType.READ_SMS, readSmsAction?.type)
  }

  @Test
  fun `verify hacker code download helper generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val script = com.example.util.HackerCodeDownloadHelper.generateHackerScriptSample(123456789L)
    assertTrue(script.contains("HackerMatrixPayload"))
    assertTrue(script.contains("ALIC-AI Autonomous Hacker Core Script"))

    val savedFileName = com.example.util.HackerCodeDownloadHelper.saveCodeToDownloads(context, script, "TEST_CODE")
    assertNotNull(savedFileName)
    assertTrue(savedFileName!!.endsWith(".kt"))
  }
}


