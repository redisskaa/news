package ru.bloknot.news.ui.home

import ru.bloknot.news.models.CardNews

sealed class HomeUiState {
    object Loading : HomeUiState()                                      // Идёт загрузка
    data class Success(val news: List<CardNews>) : HomeUiState()        // Успешно загрузили
    data class Error(val message: String) : HomeUiState()               // Ошибка (с текстом)
    object Empty : HomeUiState()                                        // Нет новостей вообще
}