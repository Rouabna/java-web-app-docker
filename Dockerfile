FROM tomcat:9.0-jdk11

LABEL maintainer="rouabna"

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file
COPY target/java-web-app*.war /usr/local/tomcat/webapps/java-web-app.war

# Expose port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
