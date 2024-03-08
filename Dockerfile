FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache ffmpeg && \
    mkdir /app

ENV BIND=0.0.0.0

WORKDIR /app
EXPOSE 8080

ADD ./static /app/static
ADD ./build/libs/server-all.jar /app

ENTRYPOINT java -jar /app/server-all.jar
