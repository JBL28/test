# DOCKER FILE
FROM eclipse-temurin:17-jre
LABEL maintainer="joungbok8263@gmail.com"
ARG JAR_FILE=build/libs/toy-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} docker-springboot.jar
ENTRYPOINT ["java","-jar","/docker-springboot.jar"]
