# 直接使用轻量级的 JRE 运行时
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 将本地已经打包好的 JAR 文件复制进去
# 注意：这里路径要匹配你本地 mvn package 生成的文件名
COPY target/*.jar app.jar

# 优化 Java 启动参数，限制内存占用（针对小内存服务器）
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]