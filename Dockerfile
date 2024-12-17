# Use a Maven base image with Java 17
FROM maven:3.9.8-eclipse-temurin-17-focal as builder

# Copy the local .m2 cache for Maven dependencies
VOLUME ["/root/.m2"]

# Define the cache path
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository"

# Copy the pom.xml and source code
COPY pom.xml .
COPY .git ./.git

RUN mvn -B -f pom.xml dependency:go-offline
COPY src ./src

# Build the project while skipping tests
RUN mvn -B install -DskipTests

# Use the official Tomcat image that supports Java 17
FROM tomcat:10.1.28-jdk17-temurin-jammy

# Remove default web applications from Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file from the builder stage
COPY --from=builder target/*.war /usr/local/tomcat/webapps/ROOT.war

# Expose port 8080
EXPOSE 8080

# Run Tomcat
CMD ["catalina.sh", "run"]