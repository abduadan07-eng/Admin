package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AdminRole
import com.example.data.model.AppSettings
import com.example.data.repository.AdminRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AdminRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun superAdmin_canBootstrapAndManageSettings(): Unit = runBlocking {
    val uid = signInTestUser("yahy38989@gmail.com")
    val repository = AdminRepository(firestore, auth)

    // Bootstrap initial owner
    repository.bootstrapInitialOwner(uid, "yahy38989@gmail.com", "Initial Owner")
    val admin = repository.getAdministrator(uid)
    assertNotNull(admin)
    assertEquals(AdminRole.SUPER_ADMIN.roleKey, admin?.role)

    // Update app settings
    val settings = AppSettings(
      maintenanceMode = false,
      announcementText = "CV Maker platform operational",
      announcementActive = true,
      communitySubmissionsEnabled = true
    )
    repository.updateAppSettings(settings)

    // Record audit
    repository.recordAudit("test_action", "doc_1", "settings", "Test audit details")
  }

  @Test
  fun unauthenticatedUser_cannotCreateAdministrator(): Unit = runBlocking {
    auth.signOut()
    val repository = AdminRepository(firestore, auth)

    try {
      repository.createAdministrator(
        targetUid = "some_uid",
        email = "test@example.com",
        displayName = "Test Admin",
        role = AdminRole.USER_MANAGER
      )
      fail("Expected exception when unauthenticated")
    } catch (e: Exception) {
      assertTrue(e is IllegalStateException || e is FirebaseFirestoreException)
    }
  }
}
