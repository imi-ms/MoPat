FROM maven:3.9.11-eclipse-temurin-17-noble AS builder

WORKDIR /app

# Copy only files that affect Maven dependencies first.
COPY pom.xml .

# Reusable Maven dependency cache.
RUN --mount=type=cache,id=mopat-maven,target=/root/.m2 \
    mvn -B dependency:go-offline

# These files change frequently and therefore come later.
COPY src ./src
COPY .git ./.git

RUN --mount=type=cache,id=mopat-maven,target=/root/.m2 \
    mvn -B install -DskipTests


FROM tomcat:10-jdk17-temurin-noble

RUN rm -rf /usr/local/tomcat/webapps/*

COPY src/main/resources/docker-server.xml \
     /usr/local/tomcat/conf/server.xml

COPY --from=builder \
     /app/target/*.war \
     /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]