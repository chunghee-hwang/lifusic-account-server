FROM openjdk:17-slim
COPY build/libs/*.jar /usr/src/lifusic/app.jar
WORKDIR /usr/src/lifusic
ENTRYPOINT ["java", "-jar", "*.jar"]