# ==========================================
# Stage 1: Compilação e Empacotamento
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copia pom.xml e baixa dependências para cache de camadas
COPY app/pom.xml ./
RUN mvn dependency:go-offline -B

# Copia código-fonte e compila o JAR sem executar testes
COPY app/src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Runtime Leve, Otimizado e Seguro
# ==========================================
FROM bellsoft/liberica-runtime-container:jre-17-musl
WORKDIR /app

# Cria usuário não-root por boas práticas de segurança em contêineres
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Configuração de fuso horário de Brasília e limites de memória para contêiner
ENV TZ=America/Sao_Paulo \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

# Copia o artefato executável gerado
COPY --from=builder /workspace/target/*.jar app.jar

# Define usuário sem privilégios administrativos
USER appuser:appgroup

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
