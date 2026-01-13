package ru.bloknot.news.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.bloknot.news.R.drawable
import ru.bloknot.news.activity.FullActivity
import ru.bloknot.news.databinding.MyTestBinding
import ru.bloknot.news.models.CardNews

class CustomAdapter(
    private val newsList: List<CardNews>,
) : RecyclerView.Adapter<CustomAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = MyTestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    override fun getItemCount(): Int = newsList.size

    // ViewHolder с ViewBinding — никаких findViewById!
    class NewsViewHolder(
        private val binding: MyTestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CardNews) {
            binding.titleText.text = item.getTitle()
            binding.catText.text = item.getCategory()
            binding.timeText.text = item.getTime()
            binding.descriptionText.text = item.getDescription()
            val urlFull = item.fullUrlLink

            binding.root.setOnClickListener {
                Log.d("CLICK_DEBUG", "Клик по новости: ${item.getTitle()}")
                Log.d("CLICK_DEBUG", "URL перед отправкой: ${item.getFullUrlLink() ?: "NULL или пусто!"}")

                val context = binding.root.context
                val intent = Intent(context, FullActivity::class.java)
                intent.putExtra("url", urlFull)  // ← здесь главное! передаём ссылку на полную новость

                context.startActivity(intent)
            }

            // Coil — лучшая библиотека для загрузки картинок в 2025 году
            binding.imageView.load(item.imageUrl) {
                crossfade(true)
                placeholder(drawable.lazy)     // твой плейсхолдер
                error(drawable.error)         // ошибка загрузки
            }
        }
    }
}