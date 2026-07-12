# Stage 1: Build
# Usa a imagem oficial do Maven com JDK 17 para compilar o projeto
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o pom.xml primeiro e baixa as dependencias separadamente
# Assim o Docker reutiliza esse layer em cache enquanto o pom.xml nao mudar
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o codigo-fonte e empacota sem rodar os testes
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
# Usa apenas o JRE Alpine (imagem enxuta, sem Maven nem JDK)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia apenas o JAR gerado no stage de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
