package com.porua.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.porua.app.data.content.ContentChapter
import com.porua.app.data.content.ContentRepository
import com.porua.app.data.content.ContentSubject
import com.porua.app.data.content.GroupInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContentUiState(
    val groups: List<GroupInfo> = emptyList(),
    val selectedGroup: String = "",
    val subjects: List<ContentSubject> = emptyList(),
    val selectedSubjectId: String = "",
    val selectedSubjectName: String = "",
    val chapters: List<ContentChapter> = emptyList(),
    val isLoading: Boolean = false
)

class ContentViewModel(private val appContext: Context) : ViewModel() {

    private val _state = MutableStateFlow(ContentUiState())
    val state: StateFlow<ContentUiState> = _state.asStateFlow()

    init {
        _state.update { it.copy(groups = ContentRepository.getAllGroups()) }
    }

    fun selectGroup(group: String) {
        val subjects = ContentRepository.getSubjectsForGroup(group)
        _state.update { it.copy(selectedGroup = group, subjects = subjects, chapters = emptyList()) }
    }

    fun loadChapters(subjectId: String, subjectName: String) {
        _state.update { it.copy(isLoading = true, selectedSubjectId = subjectId, selectedSubjectName = subjectName) }
        viewModelScope.launch(Dispatchers.IO) {
            val chapters = ContentRepository.getChaptersForSubject(appContext, subjectId)
            _state.update { it.copy(chapters = chapters, isLoading = false) }
        }
    }
}

class ContentViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContentViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: $modelClass")
    }
}
