package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DbTestFragment : Fragment(R.layout.fragment_db_test) {

    private lateinit var tvDbOutput: TextView
    private lateinit var btnStartMain: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDbOutput = view.findViewById(R.id.tvDbOutput)
        btnStartMain = view.findViewById(R.id.btnStartMain)

        // 1) Firebase 데이터 읽기
        FirebaseDatabase.getInstance()
            .reference
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val json = snapshotToJson(snapshot)
                    tvDbOutput.text = json
                    Log.d("DBTEST", json)
                }
                override fun onCancelled(error: DatabaseError) {
                    tvDbOutput.text = "데이터 로드 실패: ${error.message}"
                    Log.w("DBTEST", "Failed to read.", error.toException())
                }
            })

        // 2) 버튼 클릭 시 StartMainActivity 호출
        btnStartMain.setOnClickListener {
            val intent = Intent(requireContext(), StartMainActivity::class.java)
            startActivity(intent)
        }
    }

    //--- DataSnapshot → JSON 변환 함수
    private fun snapshotToJson(snapshot: DataSnapshot, indent: String = ""): String {
        val sb = StringBuilder()
        if (snapshot.hasChildren()) {
            sb.append("$indent\"${snapshot.key}\": {\n")
            val childIndent = "$indent  "
            val children = snapshot.children.toList()
            children.forEachIndexed { idx, child ->
                sb.append(snapshotToJson(child, childIndent))
                sb.append(if (idx < children.lastIndex) ",\n" else "\n")
            }
            sb.append("$indent}")
        } else {
            sb.append("$indent\"${snapshot.key}\": \"${snapshot.value}\"")
        }
        return sb.toString()
    }
}
