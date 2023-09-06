FROM openjdk:17-slim
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src
RUN chmod +x ./gradlew && \
    ./gradlew bootJar && \
    mkdir -p /usr/src/lifusic && \
    cp build/libs/*.jar /usr/src/lifusic/app.jar
WORKDIR /usr/src/lifusic
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "app.jar"]