FROM bellsoft/liberica-openjdk-alpine:21

ENV BIND=0.0.0.0
EXPOSE 8080

RUN mkdir /app
ADD server/build/libs/server-all.jar /app
ADD server/static /app/static

RUN apk add --no-cache ffmpeg && \
    chown -R 1000 /app
WORKDIR /app
ENV HOME=/app

CMD java -jar /app/server-all.jar
