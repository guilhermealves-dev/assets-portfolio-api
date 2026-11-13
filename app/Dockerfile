# ==========================================
# Stage 1: Compilação e Empacotamento
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Runtime Leve, Otimizado e Seguro
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

ENV TZ=America/Sao_Paulo \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

COPY --from=builder /workspace/target/*.jar app.jar

USER appuser:appgroup

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
