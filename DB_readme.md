# SNSwithAI 데이터베이스 설명서

이 문서는 SNSwithAI 안드로이드 앱의 로컬 데이터베이스 구조와 사용법을 설명합니다.

## 1. 개요

이 앱은 Android Room Persistence Library를 사용하여 로컬 데이터베이스를 관리합니다. Room은 SQLite 데이터베이스 위에 추상화 계층을 제공하여 더 강력하고 편리한 데이터베이스 액세스를 가능하게 합니다.

- **데이터베이스 클래스**: `AppDatabase.kt`
- **데이터베이스 버전**: 1
- **위치**: `app/src/main/java/com/example/snswithai/data/local/db/`

## 2. 데이터베이스 스키마 (Entities)

데이터베이스는 10개의 테이블(Entity)로 구성되어 있습니다. 각 Entity는 `entity` 패키지 안에 정의되어 있습니다.

1.  **User.kt**: 사용자 정보
2.  **Character.kt**: AI 캐릭터 정보
3.  **Relationship.kt**: 사용자와 AI 캐릭터 간의 관계 정보
4.  **ChatRoom.kt**: 채팅방 정보
5.  **ChatRoomMember.kt**: 채팅방 멤버 정보
6.  **ChatMessage.kt**: 채팅 메시지
7.  **TimelinePost.kt**: 타임라인 게시물
8.  **TimelineComment.kt**: 타임라인 댓글
9.  **Call.kt**: 통화 기록
10. **CallUtterance.kt**: 통화 중 발생한 대화 내용

## 3. 데이터 접근 객체 (DAOs)

각 Entity에 대한 데이터베이스 조작(CRUD)은 DAO(Data Access Object)를 통해 이루어집니다. 각 DAO는 `dao` 패키지 안에 인터페이스로 정의되어 있습니다.

1.  **UserDao**: `User` 테이블 관련 쿼리
2.  **CharacterDao**: `Character` 테이블 관련 쿼리
3.  **RelationshipDao**: `Relationship` 테이블 관련 쿼리
4.  **ChatRoomDao**: `ChatRoom` 테이블 관련 쿼리
5.  **ChatRoomMemberDao**: `ChatRoomMember` 테이블 관련 쿼리
6.  **ChatMessageDao**: `ChatMessage` 테이블 관련 쿼리
7.  **TimelinePostDao**: `TimelinePost` 테이블 관련 쿼리
8.  **TimelineCommentDao**: `TimelineComment` 테이블 관련 쿼리
9.  **CallDao**: `Call` 테이블 관련 쿼리
10. **CallUtteranceDao**: `CallUtterance` 테이블 관련 쿼리

## 4. 사용법

데이터베이스를 사용하기 위해서는 `AppDatabase` 인스턴스를 통해 각 DAO를 얻은 후, 해당 DAO에 정의된 메소드를 호출하면 됩니다.

**예시: 사용자 정보 저장 및 조회 (Kotlin)**

```kotlin
// 1. 데이터베이스 인스턴스 가져오기
val db = Room.databaseBuilder(
    applicationContext,
    AppDatabase::class.java, "database-name"
).build()

// 2. UserDao 가져오기
val userDao = db.userDao()

// 3. 새로운 사용자 생성 및 저장
val newUser = User(id = 1, name = "John Doe", email = "john.doe@example.com")
userDao.insertUser(newUser)

// 4. 모든 사용자 정보 조회
val allUsers = userDao.getAllUsers()

```

**주의**: 데이터베이스 작업은 메인 스레드에서 직접 호출할 수 없습니다. Coroutines, LiveData, RxJava 등을 사용하여 비동기적으로 처리해야 합니다.
