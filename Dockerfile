FROM eclipse-temurin:17-jre

WORKDIR /app

RUN mkdir -p logs

# build/libs 에는 부트 실행 jar 와 plain jar 가 함께 생긴다.
# *SNAPSHOT.jar 로 받으면 둘 다 걸려 COPY 가 실패한다. plain 은 제외한다.
COPY build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","/app/app.jar"]
