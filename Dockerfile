FROM alpine:latest
RUN apk --update add openjdk8-jre
RUN mkdir /opt
COPY target/uberjar/trivia-0.0.1-SNAPSHOT-standalone.jar /opt
WORKDIR /opt
CMD ["java", "-jar", "/opt/trivia-0.0.1-SNAPSHOT-standalone.jar"]
