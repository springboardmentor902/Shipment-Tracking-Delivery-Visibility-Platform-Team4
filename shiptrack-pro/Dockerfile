FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean install -DskipTests

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar"]