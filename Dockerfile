FROM eclipse-temurin:17-jre
WORKDIR /app
COPY ShadowHackers.jar app.jar
CMD ["java","-jar","app.jar"]
