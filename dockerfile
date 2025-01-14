# Use a base image with Java 21
FROM openjdk:21-jdk

LABEL maintainer="indrajeet"

Add target/login-0.0.1-SNAPSHOT.jar springboot-docker-demo.jar
# Set the working directory inside the container
# WORKDIR /app
#
# # Copy the JAR file into the container
# COPY target/*.jar /app/app.jar

# Expose the port that your application will run on
EXPOSE 8080

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "springboot-docker-demo.jar"]
