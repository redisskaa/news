package ru.bloknot.news.ui.main

import ru.bloknot.news.models.CardNews

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(
        val news: List<CardNews>,
        val urls: List<String>
    ) : MainUiState()
    data class Error(
        val message: String,
        val noInternet: Boolean = false
    ) : MainUiState()
}