FROM maven:3.9.4-eclipse-temurin-21 AS build


WORKDIR /app

COPY .mvn/ .mvn

COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jdk


COPY --from=build /app/target/Uber-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

CMD ["java", "-jar", "Uber-0.0.1-SNAPSHOT.jar"]