FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "build/libs/gigatester-0.0.1-SNAPSHOT.jar"]
