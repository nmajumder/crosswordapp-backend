FROM maven:3-jdk-8 as maven
WORKDIR /tmp
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src
RUN mvn package && cp target/crossword-spring-boot-server.jar crossword-spring-boot-server.jar

FROM openjdk:8
WORKDIR /tmp
COPY --from=maven /tmp/crossword-spring-boot-server.jar ./crossword-spring-boot-server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "crossword-spring-boot-server.jar", "--spring.profiles.active=prod"]