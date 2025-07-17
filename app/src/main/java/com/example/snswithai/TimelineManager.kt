package com.example.snswithai

import android.util.Log
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object TimelineManager {

    // 각 AI의 설정을 정의하는 데이터 클래스
    private data class TimelineAIConfig(
        val id: Int,
        val systemInstruction: String,
        val intervalMillis: Long,
        val aiService: AIService
    )

    // 5가지 AI 캐릭터의 성격과 시간 간격 정의
    private val aiConfigs: List<TimelineAIConfig> = run {
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

            val jsonMessage = """
                {
                    "AI의 번호": ${config.id},
                    "대화 프롬프트": "${response.replace("\"", "\\\"")}",
                    "대화 방식": "timeline"
                }
            """.trimIndent()

            JsonDataManager.postJsonMessage(jsonMessage)
            Log.d("TimelineManager", "Posted timeline JSON message from AI ${config.id}")

        } catch (e: Exception) {
            Log.e("TimelineManager", "Error generating timeline message for AI ${config.id}", e)
        }
    }
}
