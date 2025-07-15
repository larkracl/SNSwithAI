# Firebase Realtime Database 사용법

이 문서는 프로젝트에서 Firebase Realtime Database를 사용하는 방법을 설명합니다. 기존의 Firestore에서 Realtime Database로 마이그레이션되었습니다.

## 1. 의존성 설정

`app/build.gradle.kts` 파일에 다음 의존성이 추가되었는지 확인합니다.

```kotlin
// build.gradle.kts
dependencies {
    // ... other dependencies
    implementation('com.google.firebase:firebase-database-ktx')
    // ... other dependencies
}
```

기존의 Firestore 의존성(`implementation 'com.google.firebase:firebase-firestore-ktx'`)은 제거되었습니다.

## 2. 데이터베이스 인스턴스 가져오기

Realtime Database를 사용하려면 `FirebaseDatabase` 인스턴스를 가져와야 합니다.

```kotlin
val database = FirebaseDatabase.getInstance()
val myRef = database.getReference("message") // "message"는 데이터베이스의 경로입니다.
```

## 3. Repository 패턴 사용

프로젝트에서는 Repository 패턴을 사용하여 데이터베이스와 상호 작용합니다. 각 Repository는 `FirebaseDatabase` 인스턴스를 주입받아 사용합니다.

### 예시: UserRepository

**기존 Firestore 방식 (`UserRepository.kt`)**

```kotlin
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(private val db: FirebaseFirestore) {
    private val usersCollection = db.collection("users")

    suspend fun createUser(user: User) {
        usersCollection.document(user.userId).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        return usersCollection.document(userId).get().await().toObject(User::class.java)
    }
    // ...
}
```

**새로운 Realtime Database 방식 (`UserRepository.kt`)**

```kotlin
import com.google.firebase.database.FirebaseDatabase

class UserRepository(private val db: FirebaseDatabase) {
    private val usersRef = db.getReference("users")

    suspend fun createUser(user: User) {
        usersRef.child(user.userId).setValue(user).await()
    }

    suspend fun getUser(userId: String): User? {
        return usersRef.child(userId).get().await().getValue(User::class.java)
    }
    // ...
}
```

### 주요 변경 사항

| Firestore | Realtime Database | 설명 |
| :--- | :--- | :--- |
| `FirebaseFirestore.getInstance()` | `FirebaseDatabase.getInstance()` | 데이터베이스 인스턴스를 가져옵니다. |
| `db.collection("path")` | `db.getReference("path")` | 데이터의 특정 위치에 대한 참조를 가져옵니다. |
| `.document("id")` | `.child("id")` | 하위 노드에 대한 참조를 가져옵니다. |
| `.set(data)` | `.setValue(data)` | 데이터를 해당 위치에 씁니다. |
| `.get().await().toObject(Class)` | `.get().await().getValue(Class)` | 데이터를 읽고 객체로 변환합니다. |
| `.delete()` | `.removeValue()` | 데이터를 삭제합니다. |
| `FieldValue.increment(1)` | `ServerValue.increment(1)` | 원자적으로 값을 증가시킵니다. |
| `.whereEqualTo("field", "value")` | `.orderByChild("field").equalTo("value")` | 쿼리를 사용하여 데이터를 필터링합니다. |

## 4. 데이터 모델

데이터 모델 클래스(`User`, `TimelinePost` 등)는 변경되지 않았습니다. Realtime Database는 객체를 직접 저장하고 읽을 수 있습니다.

## 5. 전체적인 마이그레이션 요약

프로젝트의 모든 Repository (`UserRepository`, `TimelinePostRepository` 등)와 Activity (`LoginActivity`)에서 Firestore API를 Realtime Database API로 교체했습니다. 이제 프로젝트는 Firestore가 아닌 Realtime Database와 통신합니다.
