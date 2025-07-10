# Android 앱 데이터 아키텍처: Entity, DAO, Repository

Android 앱 개발에서 데이터 지속성(persistence)을 다룰 때, 특히 Room 데이터베이스와 함께 사용되는 일반적인 아키텍처 패턴입니다.

## 1. Entity (엔티티)

*   **역할:** 데이터베이스 테이블을 나타내는 클래스입니다. 각 엔티티 클래스는 데이터베이스의 한 테이블에 매핑되며, 클래스의 각 필드는 테이블의 한 열(column)에 해당합니다.
*   **포함하는 내용:** 데이터 모델(POJO - Plain Old Java Object 또는 Kotlin Data Class)과 테이블 스키마 정의(예: `@Entity`, `@PrimaryKey`, `@ColumnInfo` 등의 Room 어노테이션).
*   **예시:** 사용자 정보를 저장하는 `User` 엔티티, 게시물 정보를 저장하는 `Post` 엔티티 등.

## 2. DAO (Data Access Object - 데이터 접근 객체)

*   **역할:** 데이터베이스에 접근하여 데이터를 조작하는 메서드(쿼리)를 정의하는 인터페이스 또는 추상 클래스입니다. SQL 쿼리를 직접 작성하는 대신, DAO 인터페이스에 정의된 메서드를 호출하여 데이터베이스 작업을 수행합니다. Room 라이브러리가 이 인터페이스를 기반으로 실제 구현체를 자동으로 생성합니다.
*   **포함하는 내용:** `@Insert`, `@Update`, `@Delete`, `@Query` 등의 Room 어노테이션과 함께 데이터 삽입, 업데이트, 삭제, 조회 등의 메서드 시그니처.
*   **예시:** `UserDao`는 `insertUser()`, `getUserById()`, `getAllUsers()` 등의 메서드를 가질 수 있습니다.

## 3. Repository (레포지토리)

*   **역할:** 데이터 소스(예: 로컬 데이터베이스, 원격 서버 API, 캐시 등)에 대한 추상화 계층을 제공합니다. UI(ViewModel)는 직접 데이터 소스에 접근하지 않고 Repository를 통해 데이터를 요청합니다. Repository는 여러 데이터 소스(예: 네트워크에서 데이터를 가져오거나, 로컬 데이터베이스에서 데이터를 가져오는 등) 간의 데이터 충돌을 해결하고, 데이터 일관성을 유지하는 역할을 합니다.
*   **포함하는 내용:** 하나 이상의 DAO 인스턴스, 네트워크 서비스 클라이언트, 그리고 이들을 사용하여 데이터를 가져오거나 저장하는 비즈니스 로직.
*   **예시:** `UserRepository`는 `UserDao`를 사용하여 로컬 데이터베이스에서 사용자 정보를 가져오거나, 네트워크 API를 통해 서버에서 사용자 정보를 가져올 수 있습니다.

### 이러한 아키텍처의 이점:

*   **관심사 분리 (Separation of Concerns):** 각 계층이 명확한 역할과 책임을 가집니다.
*   **테스트 용이성:** 각 계층을 독립적으로 테스트하기 용이합니다.
*   **유지보수성:** 데이터 소스가 변경되더라도 Repository 계층만 수정하면 되므로, UI나 ViewModel 계층에 영향을 주지 않습니다.
*   **확장성:** 새로운 데이터 소스를 추가하거나 기존 데이터 소스를 변경하기 용이합니다.

## 데이터베이스 접근 (CRUD) 사용법 예시

간단한 `User` 엔티티를 사용하여 데이터베이스에 접근(추가, 검색, 수정, 삭제)하는 방법을 예시로 설명합니다.

### 1. Entity 정의 (User.kt)

```kotlin
// app/src/main/java/com/example/snswithai/data/User.kt
package com.example.snswithai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String
)
```

### 2. DAO 정의 (UserDao.kt)

```kotlin
// app/src/main/java/com/example/snswithai/data/UserDao.kt
package com.example.snswithai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): Flow<User>

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}
```

### 3. Repository 정의 (UserRepository.kt)

```kotlin
// app/src/main/java/com/example/snswithai/data/UserRepository.kt
package com.example.snswithai.data

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    suspend fun addUser(user: User) {
        userDao.insert(user)
    }

    fun getUser(id: Int): Flow<User> {
        return userDao.getUserById(id)
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.delete(user)
    }
}
```

### 4. 데이터베이스 (AppDatabase.kt)

```kotlin
// app/src/main/java/com/example/snswithai/data/AppDatabase.kt
package com.example.snswithai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### 5. ViewModel에서 Repository 사용 예시

```kotlin
// app/src/main/java/com/example/snswithai/ui/UserViewModel.kt
package com.example.snswithai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snswithai.data.User
import com.example.snswithai.data.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    val allUsers: Flow<List<User>> = repository.getAllUsers()

    fun insertUser(user: User) = viewModelScope.launch {
        repository.addUser(user)
    }

    fun getUserById(id: Int): Flow<User> {
        return repository.getUser(id)
    }

    fun updateUser(user: User) = viewModelScope.launch {
        repository.updateUser(user)
    }

    fun deleteUser(user: User) = viewModelScope.launch {
        repository.deleteUser(user)
    }
}

class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

### 6. Activity/Fragment에서 ViewModel 사용 예시

```kotlin
// app/src/main/java/com/example/snswithai/MainActivity.kt (예시)
package com.example.snswithai

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.snswithai.data.AppDatabase
import com.example.snswithai.data.User
import com.example.snswithai.data.UserRepository
import com.example.snswithai.ui.UserViewModel
import com.example.snswithai.ui.UserViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(this).userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Hello World 화면

        // 예시: 사용자 추가
        lifecycleScope.launch {
            val newUser = User(name = "John Doe", email = "john.doe@example.com")
            userViewModel.insertUser(newUser)
            Log.d("DB_EXAMPLE", "User added: ${newUser.name}")
        }

        // 예시: 모든 사용자 검색
        lifecycleScope.launch {
            userViewModel.allUsers.collect { users ->
                Log.d("DB_EXAMPLE", "All users: $users")
            }
        }

        // 예시: 특정 사용자 검색 (ID 1번 사용자)
        lifecycleScope.launch {
            userViewModel.getUserById(1).collect { user ->
                if (user != null) {
                    Log.d("DB_EXAMPLE", "User with ID 1: ${user.name}")
                    // 예시: 사용자 수정
                    val updatedUser = user.copy(name = "Jane Doe")
                    userViewModel.updateUser(updatedUser)
                    Log.d("DB_EXAMPLE", "User updated: ${updatedUser.name}")
                }
            }
        }

        // 예시: 사용자 삭제 (ID 1번 사용자)
        lifecycleScope.launch {
            // 실제 앱에서는 사용자 ID를 통해 User 객체를 먼저 가져와야 합니다.
            // 여기서는 예시를 위해 임시 User 객체를 생성합니다.
            val userToDelete = User(id = 1, name = "Jane Doe", email = "john.doe@example.com")
            userViewModel.deleteUser(userToDelete)
            Log.d("DB_EXAMPLE", "User deleted: ${userToDelete.name}")
        }
    }
}
