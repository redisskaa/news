package ru.bloknot.news.models

data class SettingsItem(
    val icon: Int,
    val title: String,
    val subtitle: String? = null,
    val hasSwitch: Boolean = false,
    var switchChecked: Boolean = false
)