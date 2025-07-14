package com.example.snswithai

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load          // ← 이 줄
import coil.request.ImageRequest.Builder  // placeholder 사용 시 필요할 수 있음

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("imagePath")
    fun loadImagePath(view: ImageView, path: String?) {
        if (path.isNullOrBlank()) {
            view.setImageResource(R.drawable.ic_baseline_person_24)
            return
        }
        when {
            path.startsWith("@drawable/") -> {
                val name = path.removePrefix("@drawable/")
                val resId = view.context.resources.getIdentifier(
                    name, "drawable", view.context.packageName
                ).takeIf { it != 0 } ?: R.drawable.ic_baseline_person_24
                view.setImageResource(resId)
            }
            path.startsWith("http") -> {
                view.load(path) {
                    placeholder(R.drawable.ic_baseline_person_24)
                    error(R.drawable.ic_baseline_error_24)
                }
            }
            else -> {
                val resId = view.context.resources.getIdentifier(
                    path, "drawable", view.context.packageName
                ).takeIf { it != 0 } ?: R.drawable.ic_baseline_person_24
                view.setImageResource(resId)
            }
        }
    }
}