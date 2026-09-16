package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Job

@Entity(tableName = "saved_jobs")
data class SavedJobEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val workMode: String,
    val jobType: String,
    val experienceLevel: String,
    val salaryFormatted: String,
    val radarMatchScore: Int,
    val skillsCsv: String,
    val matchReasonsCsv: String,
    val description: String,
    val applicationUrl: String,
    val postedTime: String,
    val savedAt: Long = System.currentTimeMillis(),
    val userNotes: String = ""
) {
    fun toJob(): Job {
        val skillList = if (skillsCsv.isBlank()) emptyList() else skillsCsv.split("|")
        val reasonsList = if (matchReasonsCsv.isBlank()) emptyList() else matchReasonsCsv.split("|")
        return Job(
            id = id,
            title = title,
            company = company,
            location = location,
            workMode = workMode,
            jobType = jobType,
            experienceLevel = experienceLevel,
            radarMatchScore = radarMatchScore,
            skills = skillList,
            matchReasons = reasonsList,
            description = description,
            applicationUrl = applicationUrl,
            postedTime = postedTime
        )
    }

    companion object {
        fun fromJob(job: Job, notes: String = ""): SavedJobEntity {
            return SavedJobEntity(
                id = job.id,
                title = job.title,
                company = job.company,
                location = job.location,
                workMode = job.workMode,
                jobType = job.jobType,
                experienceLevel = job.experienceLevel,
                salaryFormatted = job.salaryFormatted,
                radarMatchScore = job.radarMatchScore,
                skillsCsv = job.skills.joinToString("|"),
                matchReasonsCsv = job.matchReasons.joinToString("|"),
                description = job.description,
                applicationUrl = job.applicationUrl,
                postedTime = job.postedTime,
                savedAt = System.currentTimeMillis(),
                userNotes = notes
            )
        }
    }
}
