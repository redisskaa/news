package ru.bloknot.news.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.bloknot.news.databinding.ItemSettingBinding
import ru.bloknot.news.databinding.ItemSettingSwitchBinding
import ru.bloknot.news.models.SettingsItem

class SettingsAdapter(
    private val items: List<SettingsItem>,
    private val onSwitchChanged: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_SWITCH = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].hasSwitch) TYPE_SWITCH else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SWITCH) {
            val binding = ItemSettingSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SwitchViewHolder(binding)
        } else {
            val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ItemViewHolder -> holder.bind(item)
            is SwitchViewHolder -> holder.bind(item, position, onSwitchChanged)
        }
    }

    override fun getItemCount() = items.size

    class ItemViewHolder(private val binding: ItemSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem) {
            binding.icon.setImageResource(item.icon)
            binding.title.text = item.title
            binding.subtitle.text = item.subtitle
            binding.subtitle.visibility = if (item.subtitle.isNullOrBlank()) View.GONE else View.VISIBLE
        }
    }

    class SwitchViewHolder(private val binding: ItemSettingSwitchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem, position: Int, onChanged: (Int, Boolean) -> Unit) {
            binding.icon.setImageResource(item.icon)
            binding.switchItem.isChecked = item.switchChecked
            binding.switchItem.setOnCheckedChangeListener { _, isChecked ->
                onChanged(position, isChecked)
            }
        }
    }
}