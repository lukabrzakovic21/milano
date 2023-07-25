FROM openjdk:17-jdk-slim
EXPOSE 8082
ARG JAR_FILE=target/milano-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]