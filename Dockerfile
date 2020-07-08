FROM openjdk:8
ADD target/crossword-spring-boot-server.jar crossword-spring-boot-server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "crossword-spring-boot-server.jar", "--spring.profiles.active=prod"]