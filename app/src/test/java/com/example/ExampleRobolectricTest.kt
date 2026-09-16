package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.ApiClientProvider
import com.example.data.db.SavedJobEntity
import com.example.data.model.Job
import com.example.data.model.JobFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Career Radar", appName)
  }

  @Test
  fun `saved job entity converts correctly to domain job`() {
    val job = Job(
      id = "test_01",
      title = "Senior Android Engineer",
      company = "Tech Radar",
      location = "San Francisco, CA",
      workMode = "Remote",
      radarMatchScore = 95,
      skills = listOf("Kotlin", "Jetpack Compose", "FastAPI"),
      matchReasons = listOf("Direct stack match"),
      description = "Great opportunity"
    )

    val entity = SavedJobEntity.fromJob(job, notes = "Applied on Monday")
    assertEquals("test_01", entity.id)
    assertEquals("Applied on Monday", entity.userNotes)

    val convertedJob = entity.toJob()
    assertEquals(job.id, convertedJob.id)
    assertEquals(job.title, convertedJob.title)
    assertEquals(job.company, convertedJob.company)
    assertEquals(job.radarMatchScore, convertedJob.radarMatchScore)
    assertEquals(job.skills, convertedJob.skills)
  }

  @Test
  fun `job filter active count calculates properly`() {
    val filter = JobFilter(
      workMode = "Remote",
      minRadarScore = 90,
      minSalary = 120
    )
    assertEquals(3, filter.activeFilterCount)
  }

  @Test
  fun `sample radar jobs are populated`() {
    assertTrue(ApiClientProvider.sampleJobs.isNotEmpty())
    val topJob = ApiClientProvider.sampleJobs.first()
    assertNotNull(topJob.title)
    assertTrue(topJob.radarMatchScore >= 80)
  }
}
