FROM openjdk:21-jdk
WORKDIR /app
COPY build/libs/Petcare.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]