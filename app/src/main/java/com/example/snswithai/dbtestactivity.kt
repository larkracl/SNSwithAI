package com.example.snswithai

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log

class DbTestActivity : AppCompatActivity() {

    private lateinit var tvDbOutput: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_db_test)

        tvDbOutput = findViewById(R.id.tvDbOutput)

        // 데이터베이스 인스턴스 가져오기
        val database = FirebaseDatabase.getInstance()
        val rootRef = database.reference

        // 한 번만 읽어오기
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 전체 데이터를 JSON 형태로 변환해서 출력
                val allData = snapshotToJson(snapshot)
                tvDbOutput.text = allData
                Log.d("DBTEST", allData)
            }

            override fun onCancelled(error: DatabaseError) {
                tvDbOutput.text = "데이터 로드 실패: ${error.message}"
                Log.w("DBTEST", "Failed to read value.", error.toException())
            }
        })
    }

    // DataSnapshot을 JSON 스타일 문자열로 변환하는 재귀 함수
    private fun snapshotToJson(snapshot: DataSnapshot, indent: String = ""): String {
        val builder = StringBuilder()
        if (snapshot.hasChildren()) {
            builder.append("$indent\"${snapshot.key}\": {\n")
            val childIndent = indent + "  "
            val children = snapshot.children.toList()
            children.forEachIndexed { index, child ->
                builder.append(snapshotToJson(child, childIndent))
                if (index < children.lastIndex) builder.append(",\n")
                else builder.append("\n")
            }
            builder.append("$indent}")
        } else {
            // Leaf node: key: value
            builder.append("$indent\"${snapshot.key}\": \"${snapshot.value}\"")
        }
        return builder.toString()
    }
}
