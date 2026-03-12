package edu.nd.pmcburne.hwapp.one.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hwapp.one.data.local.ScoreEntity
import edu.nd.pmcburne.hwapp.one.data.model.Gender
import edu.nd.pmcburne.hwapp.one.data.repo.ScoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class BasketballUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedGender: Gender = Gender.MEN,
    val games: List<ScoreEntity> = emptyList(),
    val isLoading: Boolean = false,
    val showingOfflineData: Boolean = false,
    val errorMessage: String? = null
)

class BasketballViewModel (private val repository: ScoreRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(BasketballUiState())
    val uiState: StateFlow<BasketballUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        loadForCurrentSelection(refreshFromNetwork = true)
    }

    fun setGender(gender: Gender) {
        if (gender == _uiState.value.selectedGender) return
        _uiState.value = _uiState.value.copy(selectedGender = gender)
        loadForCurrentSelection(refreshFromNetwork = true)
    }

    fun setDate (date: LocalDate) {
        if (date == _uiState.value.selectedDate) return
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadForCurrentSelection(refreshFromNetwork = true)
    }

    fun refresh() {
        loadForCurrentSelection(refreshFromNetwork = true)
    }

    private fun loadForCurrentSelection(refreshFromNetwork : Boolean) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observeScores(
                date = _uiState.value.selectedDate,
                gender = _uiState.value.selectedGender
            ).collect { cachedGames ->
                _uiState.value = _uiState.value.copy(
                    games = cachedGames
                )
            }
        }

        if (refreshFromNetwork) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    showingOfflineData = false
                )

                val result = repository.refreshScores(
                    date = _uiState.value.selectedDate,
                    gender = _uiState.value.selectedGender
                )

                _uiState.value = if (result.isSuccess) {
                    _uiState.value.copy(
                        isLoading = false,
                        showingOfflineData = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value.copy(
                        isLoading = false,
                        showingOfflineData = _uiState.value.games.isNotEmpty(),
                        errorMessage = if (_uiState.value.games.isEmpty()) {
                            "Could not load scores. Check internet connection."
                        } else {
                            "Offline mode: displaying saved scores."
                        }
                    )
                }
            }
        }
    }

    class Factory(private val repository: ScoreRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BasketballViewModel(repository) as T
        }
    }
}