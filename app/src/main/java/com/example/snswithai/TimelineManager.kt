package com.example.snswithai

import android.util.Log
import com.example.snswithai.data.model.TimelinePost
import com.example.snswithai.data.model.TimelinePostComment
import com.google.firebase.ai.type.content
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object TimelineManager {

    // 각 AI의 설정을 정의하는 데이터 클래스
    data class TimelineAIConfig(
        val id: Int,
        val systemInstruction: String,
        val intervalMillis: Long,
        val aiService: AIService
    )

    // 5가지 AI 캐릭터의 성격과 시간 간격 정의
    val aiConfigs: List<TimelineAIConfig> = run {
        val instructions = listOf(
            "당신은 1번 AI, 친절한 사람입니다. SNS 사용자처럼 짧은 일상 글을 작성해주세요.",
            "당신은 2번 AI, 츤데레 캐릭터입니다. SNS 사용자처럼 짧은 일상 글을 작성해주세요.",
            "당신은 3번 AI, 유쾌하고 활기찬 친구입니다. SNS 사용자처럼 짧은 일상 글을 작성해주세요.",
            "당신은 4번 AI, 모든 것을 알고 있는 박사님입니다. SNS 사용자처럼 짧은 일상 글을 작성해주세요.",
            "당신은 5번 AI, 순수한 어린아이입니다. SNS 사용자처럼 짧은 일상 글을 작성해주세요."
        )
        val intervals = listOf(100000L, 80000L, 120000L, 60000L, 130000L)

        instructions.mapIndexed { index, instruction ->
            TimelineAIConfig(
                id = index + 1,
                systemInstruction = instruction,
                intervalMillis = intervals[index],
                aiService = AIService(content { text(instruction) })
            )
        }
    }

    private val aiJobs = mutableListOf<Job>()

    fun startTimeline() {
        if (aiJobs.any { it.isActive }) {
            Log.d("TimelineManager", "Timeline is already running.")
            return
        }
        aiJobs.clear()

        val scope = CoroutineScope(Dispatchers.IO)
        for (config in aiConfigs) {
            val job = scope.launch {
                while (true) {
                    delay(config.intervalMillis)
                    generateTimelineMessage(config)
                }
            }
            aiJobs.add(job)
        }
        Log.d("TimelineManager", "Timeline started for ${aiConfigs.size} AIs.")
    }

    fun stopTimeline() {
        if (aiJobs.isEmpty()) {
            Log.d("TimelineManager", "Timeline is not running.")
            return
        }
        aiJobs.forEach { it.cancel() }
        aiJobs.clear()
        Log.d("TimelineManager", "Timeline stopped.")
    }

    private suspend fun generateTimelineMessage(config: TimelineAIConfig) {
        try {
            val prompt = "일상적인 내용의 짧은 SNS 게시글을 하나 생성해줘. (예: 오늘 날씨 정말 좋다!, 점심 뭐 먹지?)"
            val response = config.aiService.generateContent(prompt)
            Log.d("TimelineManager", "Generated timeline message from AI ${config.id}: $response")

            // 생성된 메시지를 직접 DB에 저장
            savePostToDB(config.id, response)

        } catch (e: Exception) {
            Log.e("TimelineManager", "Error generating timeline message for AI ${config.id}", e)
        }
    }

    private suspend fun savePostToDB(authorAiNumber: Int, postContent: String) {
        try {
            val db = FirebaseDatabase.getInstance()
            val userTimelineRef = db.getReference("user_data")
                .child("ZCtJmhJKr7RklOwkOjJa2OvunbA3") // NOTE: Harcoded user ID
                .child("timeline")
            val newPostRef = userTimelineRef.push()
            val postId = newPostRef.key!!

            // 1. AI 댓글 생성
            val comments = mutableMapOf<String, TimelinePostComment>()
            val commentAiConfigs = aiConfigs.filter { it.id != authorAiNumber }

            for (config in commentAiConfigs) {
                val prompt = "다음 SNS 게시글에 대한 댓글을 작성해줘: \"$postContent\""
                val commentText = config.aiService.generateContent(prompt)
                val comment = TimelinePostComment(
                    authorId = "ai_${config.id}",
                    authorName = "AI ${config.id}",
                    text = commentText,
                    createdAt = System.currentTimeMillis()
                )
                comments[db.reference.push().key!!] = comment
            }

            // 2. 게시물 데이터 생성
            val timelinePost = TimelinePost(
                postId = postId,
                author_id = "ai_$authorAiNumber",
                author_name = "AI $authorAiNumber",
                content = postContent,
                created_at = System.currentTimeMillis()
            )

            // 3. 게시물 저장
            newPostRef.setValue(timelinePost).await()

            // 4. 댓글 저장
            val commentsRef = db.getReference("timeline_comments").child(postId)
            commentsRef.setValue(comments).await()

            Log.d("TimelineManager", "New post and comments saved successfully with key: $postId")
        } catch (e: Exception) {
            Log.e("TimelineManager", "Failed to save post to DB for AI $authorAiNumber", e)
        }
    }
}