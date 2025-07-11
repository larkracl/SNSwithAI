# 데이터 아키텍처 설명 (Entity, DAO, Repository)

이 문서는 "DB 스키마.md"에 정의된 Firebase Firestore 데이터베이스 구조를 기반으로, 안드로이드 애플리케이션의 데이터 계층을 구성하는 **Entity**, **DAO**, **Repository**에 대해 상세히 설명합니다.

## 1. 아키텍처 개요

- **Entity (Data Class)**: Firestore의 각 컬렉션 문서(Document)를 Kotlin의 데이터 클래스로 매핑한 것입니다. Firestore의 필드와 1:1로 대응되며, 데이터의 구조를 정의합니다.
- **DAO (Data Access Object)**: 데이터에 접근하기 위한 **인터페이스(Interface)**입니다. 어떤 데이터를 어떻게 가져오고, 저장하고, 수정하고, 삭제할지에 대한 **규칙(Contract)**을 정의합니다. 실제 구현은 Repository에서 이루어집니다.
- **Repository**: DAO 인터페이스를 **구현(Implement)**하는 클래스입니다. 실제 Firebase Firestore SDK를 사용하여 데이터 통신을 수행하고, 비즈니스 로직을 처리합니다. ViewModel은 Repository를 통해 데이터에 접근하게 됩니다.

---

## 2. Entity (Data Class) 정의

"DB 스키마.md"의 각 컬렉션은 아래와 같은 Kotlin 데이터 클래스로 정의됩니다.

**주요 규칙:**
- `@DocumentId`: Firestore 문서의 고유 ID를 매핑하기 위해 사용합니다.
- `@ServerTimestamp`: 서버의 시간을 기준으로 타임스탬프를 자동 기록하기 위해 사용합니다.
- 필드 기본값: Firestore에서 데이터를 객체로 매핑할 때, 필드가 없는 경우를 대비해 안전하게 기본값을 지정합니다.

#### `User.kt`
```kotlin
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val email: String = "",
    val name: String = "",
    val profileImageUrl: String? = null
)
```

#### `Character.kt`
```kotlin
import com.google.firebase.firestore.DocumentId

data class Character(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = ""
)
```

#### `Relationship.kt`
```kotlin
import com.google.firebase.firestore.DocumentId

data class Relationship(
    @DocumentId val id: String = "",
    val userId: String = "",
    val characterId: String = "",
    val status: String = "", // "customized", "using_default", "not_using"
    val customName: String? = null,
    val customDescription: String? = null
)
```

#### `ChatRoom.kt`
```kotlin
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatRoom(
    @DocumentId val id: String = "",
    val members: List<String> = emptyList(), // [userId, characterId]
    val name: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
```

#### `ChatMessage.kt`
```kotlin
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatMessage(
    @DocumentId val id: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val content: String = "",
    @ServerTimestamp val timestamp: Date? = null
)
```

#### `TimelinePost.kt`
```kotlin
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TimelinePost(
    @DocumentId val id: String = "",
    val ownerId: String = "", // 타임라인 소유자 ID
    val authorId: String = "", // 실제 작성자 ID
    val authorType: String = "", // "user" or "character"
    val content: String = "",
    val imageUrl: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    @ServerTimestamp val createdAt: Date? = null
)
```

#### `TimelineComment.kt`
```kotlin
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TimelineComment(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val postId: String = "",
    val authorId: String = "",
    val content: String = "",
    val likeCount: Int = 0,
    val parentCommentId: String? = null, // 대댓글인 경우 부모 댓글 ID
    @ServerTimestamp val createdAt: Date? = null
)
```

#### `PostLike.kt`
```kotlin
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PostLike(
    @DocumentId val id: String = "",
    val postId: String = "",
    val likedByUserId: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
```

#### `CommentLike.kt`
```kotlin
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CommentLike(
    @DocumentId val id: String = "",
    val commentId: String = "",
    val likedByUserId: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
```

#### `Call.kt`
```kotlin
import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Call(
    @DocumentId val id: String = "",
    val userId: String = "",
    val characterId: String = "",
    val startTime: Date? = null,
    val endTime: Date? = null
)
```

#### `CallUtterance.kt`
```kotlin
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CallUtterance(
    @DocumentId val id: String = "",
    val callId: String = "",
    val speaker: String = "", // "user" or "character"
    val text: String = "",
    @ServerTimestamp val timestamp: Date? = null
)
```

---

## 3. DAO (Data Access Object) 정의

DAO는 각 Entity에 대한 CRUD(Create, Read, Update, Delete) 작업을 정의하는 인터페이스입니다. `Flow`를 사용하여 데이터 변경을 실시간으로 감지하고 UI에 반영할 수 있도록 설계합니다.

#### 예시: `TimelineDao.kt`
```kotlin
import kotlinx.coroutines.flow.Flow

interface TimelineDao {
    // 특정 사용자의 타임라인 게시물 목록을 실시간으로 가져오기
    fun getTimelinePosts(ownerId: String): Flow<List<TimelinePost>>

    // 특정 게시물의 댓글 목록을 실시간으로 가져오기
    fun getComments(postId: String): Flow<List<TimelineComment>>

    // 새 게시물 추가
    suspend fun addPost(post: TimelinePost)

    // 게시물 삭제
    suspend fun deletePost(postId: String)

    // 게시물 좋아요 추가/삭제 (Transaction)
    suspend fun togglePostLike(postId: String, userId: String)
}
```
> 이와 같은 방식으로 `UserDao`, `CharacterDao`, `ChatDao` 등 다른 데이터 모델에 대한 DAO 인터페이스를 각각 정의합니다.

---

## 4. Repository 정의

Repository는 DAO 인터페이스의 실제 구현체입니다. Firebase SDK를 사용하여 비동기 데이터 작업을 수행합니다.

#### 예시: `TimelineRepository.kt`
```kotlin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TimelineRepository(
    private val firestore: FirebaseFirestore
) : TimelineDao {

    override fun getTimelinePosts(ownerId: String): Flow<List<TimelinePost>> {
        val collection = firestore.collection("timelinePosts")
            .whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        // collection.snapshots()는 실시간 변경을 감지하는 Flow를 반환
        return collection.snapshots().map { snapshot ->
            snapshot.toObjects(TimelinePost::class.java)
        }
    }

    override fun getComments(postId: String): Flow<List<TimelineComment>> {
        val collection = firestore.collection("timelineComments")
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.ASCENDING)

        return collection.snapshots().map { snapshot ->
            snapshot.toObjects(TimelineComment::class.java)
        }
    }

    override suspend fun addPost(post: TimelinePost) {
        firestore.collection("timelinePosts").add(post).await()
    }

    override suspend fun deletePost(postId: String) {
        firestore.collection("timelinePosts").document(postId).delete().await()
    }

    override suspend fun togglePostLike(postId: String, userId: String) {
        // 실제 구현 시에는 Transaction을 사용하여 likeCount 업데이트와
        // postLikes 컬렉션에 문서 추가/삭제를 원자적으로 처리해야 함
    }
}
```

이러한 구조를 통해 데이터 로직을 UI 코드로부터 분리하여, 더 깔끔하고 테스트하기 쉬우며 유지보수가 용이한 애플리케이션을 만들 수 있습니다.

---

## 5. ViewModel에서의 Repository 사용 예시

ViewModel은 Repository를 주입받아 UI에 필요한 데이터를 요청하고, UI 상태를 관리합니다. Hilt와 같은 의존성 주입 라이브러리를 사용하면 이 과정을 쉽게 관리할 수 있습니다.

#### 예시: `TimelineViewModel.kt`
```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository // Hilt를 통해 Repository 주입
) : ViewModel() {

    // 현재 로그인한 사용자의 ID (실제로는 AuthService 등에서 가져와야 함)
    private val currentUserId = "test_user_id"

    // 타임라인 게시물 목록을 StateFlow로 관리하여 UI에 실시간으로 제공
    val posts = timelineRepository.getTimelinePosts(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 새 게시물을 추가하는 함수
    fun onAddPost(content: String) {
        viewModelScope.launch {
            val newPost = TimelinePost(
                ownerId = currentUserId,
                authorId = currentUserId,
                authorType = "user",
                content = content
            )
            timelineRepository.addPost(newPost)
        }
    }
}
```

#### UI (Composable 함수)에서의 사용
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TimelineScreen(viewModel: TimelineViewModel = hiltViewModel()) {
    // ViewModel의 StateFlow를 구독하여 데이터 변경 시 자동으로 UI를 다시 그림
    val posts by viewModel.posts.collectAsState()

    // posts를 사용하여 UI 목록을 표시
    LazyColumn {
        items(posts) { post ->
            Text(text = post.content)
        }
    }
}
```