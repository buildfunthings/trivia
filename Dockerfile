FROM alpine:latest
RUN apk --update add openjdk8-jre
RUN mkdir /opt
COPY target/uberjar/trivia.jar /opt
WORKDIR /opt
CMD ["java", "-jar", "/opt/trivia.jar"]
