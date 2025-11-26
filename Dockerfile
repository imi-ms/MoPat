# ------------------------------
# Runtime stage: Tomcat + Java 17
# ------------------------------
FROM tomcat:10-jdk17-temurin-noble
RUN rm -rf /usr/local/tomcat/webapps/*
COPY target/MoPat.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]