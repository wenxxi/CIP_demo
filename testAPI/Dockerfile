FROM openjdk:17
EXPOSE 8090
ADD obfuscate/obfuscate_testAPI.jar api.jar
ENTRYPOINT ["java", "-jar", "api.jar"]