package com.atpp.rgs.model

enum class Suit(val prefix: String) {
    CLUBS("clubs"),
    DIAMONDS("diamonds"),
    HEARTS("hearts"),
    SPADES("spades")
}

// 2. Figury i ich wartości punktowe
enum class Rank(val value: Int, val suffix: String) {
    TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"),
    SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"),
    JACK(10, "jack"), QUEEN(10, "queen"), KING(10, "king"),
    ACE(11, "ace") // As domyślnie ma wartość 11, matematyka zajmie się resztą
}

// 3. Obiekt Karty
data class Card(
    val suit: Suit,
    val rank: Rank,
    val imageName: String
)

// Funkcja licząca punkty ręki (listy kart)
fun calculateScore(hand: List<Card>): Int {
    // Krok 1: Sumujemy wszystkie karty na sztywno
    var score = hand.sumOf { it.rank.value }

    // Krok 2: Liczymy ile mamy Asów w ręce
    var acesCount = hand.count { it.rank == Rank.ACE }

    // Krok 3: Jeśli mamy "furę" (ponad 21), a w ręce jest As, obniżamy wartość Asa z 11 na 1 (czyli odejmujemy 10 od wyniku)
    while (score > 21 && acesCount > 0) {
        score -= 10
        acesCount -= 1
    }

    return score
}