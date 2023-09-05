FROM openjdk:17-slim
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar
COPY build/libs/*.jar /usr/src/lifusic/app.jar
WORKDIR /usr/src/lifusic
ENTRYPOINT ["java", "-jar", "app.jar"]
VOLUME /lifusic-account-server