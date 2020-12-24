FROM maven:3.6-openjdk-11

RUN mkdir keybout-backend

WORKDIR keybout-backend

COPY . .

RUN mvn clean install

ENTRYPOINT java -jar target/*.jar