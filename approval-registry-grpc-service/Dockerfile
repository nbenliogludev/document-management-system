# ===== Stage 1: build =====
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline || true

COPY src src

# Сборка jar (protobuf + grpc codegen пройдет в Debian-based image)
RUN mvn clean package -DskipTests

# Берем итоговый jar (не original)
RUN JAR_FILE=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*original*" | head -n 1) \
    && echo "Using jar: $JAR_FILE" \
    && cp "$JAR_FILE" /app/app.jar

# ===== Stage 2: runtime =====
FROM eclipse-temurin:21-jre

WORKDIR /app
VOLUME /tmp

COPY --from=builder /app/app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]