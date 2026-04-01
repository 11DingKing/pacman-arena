FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .

# 利用 BuildKit 缓存 Maven 本地仓库，重复构建时不用重新下载依赖
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B -s /root/.m2/settings.xml \
       -Dmaven.wagon.http.retryHandler.count=5 \
       -Dmaven.resolver.transport=wagon \
       -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
       -Dmaven.wagon.rto=60000

COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn package -DskipTests -B -s /root/.m2/settings.xml \
       -Dmaven.wagon.http.retryHandler.count=5

FROM --platform=$TARGETPLATFORM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
