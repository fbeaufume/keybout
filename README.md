# Keybout

Keybout is a simple web-based multiplayer keyboard racing game, where the players
have to type random words before the other players.

The first version started in 2014 as a sample WebSocket application and used JavaScript,
WebSocket, Java EE 7 and Java.

This second version is a full rewrite and uses Angular, SockJS, Spring Boot and Kotlin.

![Home page](doc/keybout-1.png)

![Game creation](doc/keybout-2.png)

![Gameplay](doc/keybout-3.png)

![Scores](doc/keybout-4.png)

## Usage

Prerequisites: Java 8+, Node.js 10+ with NPM

To start the frontend locally, run `npm install` then `ng serve` in `keybout-frontend`
or use your IDE.

To start the backend locally, run `mvnw spring-boot:run` in `keybout-backend`
or use your IDE.

To build a single deployable fat jar containing the whole application,
run `mvnw package -Pfull` from the home folder then use `keybout-backend/target/keybout-backend.jar`.
Omit `-Pfull` if you prefer to separate the fontend and backend packages.
