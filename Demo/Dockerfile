# Stage 1: 构建 JAR
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 先复制 pom.xml 单独下载依赖，利用 Docker 层缓存
COPY pom.xml .
RUN mvn dependency:go-offline -q

# 再复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: 运行时镜像（精简版）
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
