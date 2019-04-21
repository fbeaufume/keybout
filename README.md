# Keybout

Keybout started as a sample WebSocket application, then evolved to a simple
web based multiplayer game where opponents have to quickly catch words by typing them.

The first version started in 2014 and used JavaScript, WebSocket, Java EE 7, Java and Maven.

This second version is a full rewrite and uses Angular, TypeScript, WebSocket, Spring Boot, Kotlin and Maven.

The application is under development and **not working yet**.

## Usage

Prerequisites: Node.js 10+, Angular CLI 7 (install with `npm install -g @angular/cli`), Java 8+, Maven 3.5+.

To start the frontend, run `ng serve` in `keybout-frontend`.

To start the backend, run `mvn spring-boot:run` in `keybout-backend`.

To build a single fat jar containing the whole application, run `mvn package` from the home folder
then use `keybout-backend/target/keybout-backend.jar`.
