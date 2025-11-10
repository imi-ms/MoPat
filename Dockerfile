# Use a Maven base image with Java 17
FROM maven:3.9.11-eclipse-temurin-17-noble as builder

# Copy the pom.xml and source code
COPY pom.xml .

COPY src ./src
COPY .git ./.git
COPY lib ./lib

RUN mvn install:install-file -Dfile=lib/de/unimuenster/imi/org.cdisc.odm.v132/2.0.2/org.cdisc.odm.v132-2.0.2.jar -DgroupId=de.unimuenster.imi -DartifactId=org.cdisc.odm.v132 -Dversion=2.0.2 -Dpackaging=jar
RUN mvn -B -f pom.xml dependency:go-offline

# Build the project while skipping tests
RUN mvn -B install -DskipTests

# Use the official Tomcat image that supports Java 17
FROM tomcat:10-jdk17-temurin-noble

# Remove default web applications from Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file from the builder stage
COPY --from=builder target/*.war /usr/local/tomcat/webapps/ROOT.war

# Expose port 8080
EXPOSE 8080

# Run Tomcat
CMD ["catalina.sh", "run"]