FROM maven:3.9.9-amazoncorretto-21-debian AS build
MAINTAINER Pichan Vasantakitkumjorn

LABEL stage=builder

COPY src /app/src
COPY pom.xml /app
WORKDIR /app
RUN mvn clean package


FROM openjdk:21-jdk-slim AS runner
RUN mkdir -p /app/saving-account-web-service/
COPY --from=build /app/target/saving-account-web-service.jar /app/saving-account-web-service/saving-account-web-service.jar
WORKDIR /app/saving-account-web-service

CMD ["java", "-Xms128m", "-jar", "saving-account-web-service.jar"]