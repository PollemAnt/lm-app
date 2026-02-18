# lm-app

> 🇵🇱 Polski opis poniżej  
> 🇬🇧 English version below

---

# List Miłosny – Aplikacja Multiplayer (PL)

Klient gry multiplayer **„List Miłosny”**

Aplikacja łączy się z dedykowanym backendem, który odpowiada
za logikę gry oraz synchronizację stanu pomiędzy graczami.

Projekt jest częścią większego systemu:

- 📱 aplikacja kliencka (ten projekt)
- 🌐 serwer multiplayer (repozytorium: lm-serwer)

---

## 🎮 O grze

**List Miłosny** to cyfrowa wersja karcianej gry multiplayer.

Gracze dołączają do wspólnej rozgrywki i zagrywają karty,
z których każda posiada unikalny efekt wpływający na przebieg gry.

Aplikacja odpowiada za:

- prezentację interfejsu użytkownika
- wysyłanie akcji gracza do serwera
- odbieranie aktualizacji stanu gry w czasie rzeczywistym
- wizualizację eventów (zagranie karty, koniec rundy, dołączenie gracza)

Cała logika gry realizowana jest po stronie serwera.

---

## 🧠 Architektura klienta

Aplikacja została zbudowana w oparciu o architekturę:

- UI (Compose)
- Warstwa komunikacji sieciowej (REST + WebSocket)
- Modele współdzielone (commonMain – Kotlin Multiplatform)

Klient jest „cienki” – nie zawiera logiki gry,
a jedynie renderuje stan otrzymany z serwera.

---

## 🛠 Stack technologiczny

- **Kotlin Multiplatform**
- **Ktor Client**
- **Kotlinx Serialization**
- **Coroutines**
- **Gradle**

---

## 📚 Wykorzystane biblioteki i ich rola

### 🔹 Compose 
Służy do budowy interfejsu użytkownika

### 🔹 Ktor Client
Biblioteka do komunikacji z backendem.

W projekcie wykorzystywana do:
- wykonywania zapytań REST
- obsługi połączenia WebSocket

### 🔹 REST (HTTP)
Używany do operacji takich jak:

- dołączanie gracza do gry
- pobieranie aktualnego stanu gry
- wykonywanie ruchu
- aktualizacja konfiguracji gry

REST służy do operacji typu request → response,
gdzie klient oczekuje konkretnej odpowiedzi od serwera.

### 🔹 WebSocket
Używany do komunikacji w czasie rzeczywistym.

WebSocket odpowiada za:
- odbieranie eventów z serwera
- synchronizację stanu gry między wszystkimi graczami
- natychmiastową reakcję UI na zdarzenia (np. zagranie karty)

Dzięki WebSocket wszyscy gracze widzą ten sam stan gry w tym samym czasie.

### 🔹 Kotlinx Serialization
Służy do:
- serializacji i deserializacji JSON
- mapowania danych z serwera na modele aplikacji

### 🔹 Coroutines
Umożliwiają:
- asynchroniczną komunikację z serwerem
- obsługę WebSocket bez blokowania UI

---

## 🔄 Komunikacja z serwerem

Aplikacja współpracuje z backendem:
- REST → wysyłanie akcji gracza
- WebSocket → odbieranie eventów gry

Schemat komunikacji:

1. Gracz wykonuje akcję w UI
2. Klient wysyła żądanie REST do serwera
3. Serwer przetwarza ruch
4. Serwer wysyła event przez WebSocket
5. Wszyscy klienci aktualizują UI

---

# List Miłosny – Multiplayer App (EN)

Client application for the multiplayer game **“Love Letter”**,

The app connects to a dedicated backend server responsible
for game logic and state synchronization.

---

## 🎮 About the game

**Love Letter** is a digital multiplayer card game.

Players join a shared session and play cards,
each having a unique effect influencing the game flow.

The client is responsible for:

- rendering the user interface
- sending player actions to the server
- receiving real-time updates
- visualizing game events

All core game logic is handled on the server side.

---

## 🧠 Client architecture

The application follows a layered structure:

- UI layer (Compose)
- Network layer (REST + WebSocket)

The client does not implement game rules.
It renders state received from the backend.

---

## 🛠 Tech stack

- **Kotlin Multiplatform**
- **Ktor Client**
- **Kotlinx Serialization**
- **Coroutines**
- **Gradle**

---

## 📚 Libraries and their purpose

### 🔹 Compose 
Used to build UI 

### 🔹 Ktor Client
Used for communication with the backend:
- REST API calls
- WebSocket connection

### 🔹 REST (HTTP)
Used for request-response interactions:
- joining a game
- making a move
- retrieving game state
- updating configuration

### 🔹 WebSocket
Used for real-time communication:
- receiving game events
- synchronizing game state between players
- instant UI updates

### 🔹 Kotlinx Serialization
Used for JSON serialization/deserialization.

### 🔹 Coroutines
Used for asynchronous networking without blocking the UI.

---

## 🔄 Server communication flow

1. Player performs an action in the UI
2. Client sends a REST request
3. Server processes the action
4. Server broadcasts a WebSocket event
5. All connected clients update their UI

---