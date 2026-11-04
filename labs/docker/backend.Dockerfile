# 빌드 컨텍스트는 labs/backend/week25/build/libs (jar 가 들어 있는 폴더)입니다.
# 사용법은 ../../fe-collab/week13-collab-practice.md 의 3.1절을 보세요.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY week25.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
