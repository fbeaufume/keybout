# Keybout

KeyBout is a simple web-based multiplayer keyboard racing game, where the players
have to type random words before the other players.

The first version started in 2014 as a sample WebSocket application and used JavaScript,
WebSocket, Java EE 7 and Java.

This second version is a full rewrite and uses Angular, SockJS, Spring Boot and Kotlin.

![Home page](doc/keybout-1.png)

![Game creation](doc/keybout-2.png)

![Gameplay](doc/keybout-3.png)

## Usage

Prerequisites: Node.js 10+, Angular CLI 7 (install with `npm install -g @angular/cli`), Java 8+, Maven 3.5+.

To start the frontend, run `ng serve` in `keybout-frontend` or use your IDE.

To start the backend, run `mvn spring-boot:run` in `keybout-backend` or use your IDE.

To build a single fat jar containing the whole application, run `mvn package -Pfull` from the home folder
then use `keybout-backend/target/keybout-backend.jar`. Omit `-Pfull` if you prefer to separate the
fontend and backend packages.
