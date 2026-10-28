package org.example.project.ui.screens.viewModel

import org.example.shared.protocol.CardPlayedFeedback

object GameMessageFormatter {
    fun format(
        feedback: CardPlayedFeedback,
        getPlayerName: (Int) -> String,
        getCardName: (Int) -> String
    ): String {
        return when (feedback) {
            is CardPlayedFeedback.Standard -> feedback.message
            is CardPlayedFeedback.GuardPlayed -> {
                val guesser = getPlayerName(feedback.guessingPlayerId)
                val target = getPlayerName(feedback.targetPlayerId)
                val cardName = getCardName(feedback.guessedCardNumber)
                if (feedback.wasCorrect) {
                    "$guesser zagrał Strażnika na $target i zgadł! ($cardName). $target odpada."
                } else {
                    "$guesser zagrał Strażnika na $target, ale nie zgadł. Zgadywał: $cardName."
                }
            }

            is CardPlayedFeedback.BaronPlayed -> {
                val p1 = getPlayerName(feedback.player1Id)
                val p2 = getPlayerName(feedback.player2Id)
                val winnerId = feedback.winnerId
                val loserId = feedback.loserId
                when {
                    winnerId != null && loserId != null -> {
                        val winner = getPlayerName(winnerId)
                        val loser = getPlayerName(loserId)
                        "Baron: $p1 i $p2 porównali karty. $winner wygrał, $loser odpada."
                    }

                    else -> "Baron: $p1 i $p2 porównali karty. Remis!"
                }
            }

            is CardPlayedFeedback.HandmaidPlayed ->
                "Gracz ${getPlayerName(feedback.protectedPlayerId)} zagrał Służącą i jest chroniony do następnej tury."

            is CardPlayedFeedback.KingPlayed ->
                "Król: ${getPlayerName(feedback.player1Id)} zamienił się kartami z graczem ${
                    getPlayerName(
                        feedback.player2Id
                    )
                }."

            is CardPlayedFeedback.PrincePlayed -> {
                val actor = getPlayerName(feedback.playerId)
                val target = getPlayerName(feedback.targetId)
                "$actor zagrał Księcia na $target. $target odrzuca kartę i dobiera nową."
            }

            is CardPlayedFeedback.PriestPlayed ->
                "Kapłan: ${getPlayerName(feedback.viewingPlayerId)} podejrzał kartę gracza ${
                    getPlayerName(
                        feedback.targetPlayerId
                    )
                }."

            is CardPlayedFeedback.SpyPlayed -> "Gracz ${getPlayerName(feedback.spyPlayerId)} zagrał Szpiega."

            is CardPlayedFeedback.ChancellorPlayed -> "Gracz ${getPlayerName(feedback.playerId)} zagrał Kanclerza i wybiera karty."
        }
    }
}
