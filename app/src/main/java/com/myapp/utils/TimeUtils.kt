package com.myapp.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 클래스 없이 파일 최상단에 바로 함수를 선언
fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")
    return localDateTime.format(formatter)
}
