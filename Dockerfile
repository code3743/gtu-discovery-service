FROM openjdk:17-slim AS build

WORKDIR /app


COPY pom.xml mvnw ./
COPY .mvn .mvn/


RUN chmod +x mvnw && ./mvnw dependency:go-offline


COPY src src/


RUN ./mvnw clean package -DskipTests

FROM openjdk:17-slim AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
