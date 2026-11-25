package ru.bloknot.news.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.size.Scale
import ru.bloknot.news.R
import ru.bloknot.news.activity.FullActivity
import ru.bloknot.news.databinding.MyTestBinding
import ru.bloknot.news.models.CardNews

class CustomAdapter : ListAdapter<CardNews, CustomAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = MyTestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateNews(newNews: List<CardNews>) {
        submitList(newNews)
    }

    class NewsViewHolder(
        private val binding: MyTestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CardNews) {
            binding.titleText.text = item.title
            binding.catText.text = item.category
            binding.timeText.text = item.time
            binding.descriptionText.text = item.description
            val urlFull = item.fullUrlLink

            binding.root.setOnClickListener {
                Log.d("CLICK_DEBUG", "Клик по новости: ${item.title}")
                Log.d("CLICK_DEBUG", "URL перед отправкой: ${item.fullUrlLink ?: "NULL или пусто!"}")

                val context = binding.root.context
                val intent = Intent(context, FullActivity::class.java)
                intent.putExtra("url", urlFull)
                intent.putExtra("title", binding.titleText.text)
                context.startActivity(intent)
            }

            binding.imageView.load(item.imageUrl) {
                crossfade(false)
                placeholder(R.drawable.lazy)
                error(R.drawable.error)
                scale(Scale.FILL)
                allowHardware(false)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }
    }

    class NewsDiffCallback : DiffUtil.ItemCallback<CardNews>() {
        override fun areItemsTheSame(oldItem: CardNews, newItem: CardNews): Boolean {
            return oldItem.title == newItem.title && oldItem.time == newItem.time
        }

        override fun areContentsTheSame(oldItem: CardNews, newItem: CardNews): Boolean {
            return oldItem.fullUrlLink == newItem.fullUrlLink
        }
    }
}