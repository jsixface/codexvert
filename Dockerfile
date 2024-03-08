FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache ffmpeg && \
    mkdir /app

ENV BIND=0.0.0.0

WORKDIR /app
EXPOSE 8080

ADD server/static /app/static
ADD server/build/libs/server-all.jar /app

CMD java -jar /app/server-all.jar
