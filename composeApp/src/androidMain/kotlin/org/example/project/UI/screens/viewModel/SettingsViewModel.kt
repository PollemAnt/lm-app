package org.example.project.UI.screens.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.NetworkClient
import org.example.models.CardCount
import org.example.models.GameConfig
import org.example.repository.GameRepository
import org.example.repository.api.GameApi

class SettingsViewModel(private val repository: GameRepository) : ViewModel() {
    constructor() : this(
        GameRepository(
            GameApi(
                client = NetworkClient.httpClient,
                "http://192.168.18.109:8080"
            )
        )
    )

    private val _configState = MutableStateFlow(createDefaultConfig())
    val configState = _configState.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()


    init {
        loadConfigFromServer()
    }

    fun loadConfigFromServer() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val config = repository.getGameConfig()
                _configState.value = config
                println("Pobrano konfigurację z serwera: $config")
            } catch (e: Exception) {
                _errorMessage.value = "Nie udało się pobrać konfiguracji: ${e.message}"
                println("Błąd podczas pobierania konfiguracji: ${e.message}")

                _configState.value = createDefaultConfig()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setMaxPlayers(count: Int) {
        val newCount = count.coerceIn(2, 6)
        _configState.value.let { currentConfig ->
            _configState.value = currentConfig.copy(maxPlayers = newCount)
        }
    }

    fun updateCardCount(cardNumber: Int, change: Int) {
        _configState.value.let { currentConfig ->
            val updatedDeck = currentConfig.deckComposition.map { cardCount ->
                if (cardCount.cardNumber == cardNumber) {
                    val newCount = (cardCount.count + change).coerceAtLeast(0)
                    cardCount.copy(count = newCount)
                } else {
                    cardCount
                }
            }
            _configState.value = currentConfig.copy(deckComposition = updatedDeck)
        }
    }

    fun sendConfigToServer() {
        viewModelScope.launch {
            val currentConfig = _configState.value ?: return@launch

            println("Wysyłanie konfiguracji do serwera: $currentConfig")

            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.updateGameConfig(currentConfig)

                _errorMessage.value = "Konfiguracja zapisana pomyślnie"
            } catch (e: Exception) {
                _errorMessage.value = "Nie udało się zapisać konfiguracji: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        fun createDefaultConfig(): GameConfig {
            return GameConfig(
                maxPlayers = 4,
                deckComposition = listOf(
                    CardCount("Szpieg", 0, 2),
                    CardCount("Strażnik", 1, 6),
                    CardCount("Kapłan", 2, 2),
                    CardCount("Baron", 3, 2),
                    CardCount("Służaca", 4, 2),
                    CardCount("Książe", 5, 2),
                    CardCount("Kanclerz", 6, 2),
                    CardCount("Król", 7, 1),
                    CardCount("Hrabina", 8, 1),
                    CardCount("Księżniczka", 9, 1)
                )
            )
        }
    }
}