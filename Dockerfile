FROM openjdk:17
WORKDIR  /app
COPY ./target/dockerize-0.0.1.jar  /app
EXPOSE 9003
#VOLUME TEST_VOLUME
ENTRYPOINT ["java" ,"-jar", "dockerize-0.0.1.jar"]
