FROM openjdk:8
VOLUME /tmp
EXPOSE 8080
ENV JAVA_OPTS=""
ADD addonindex-*.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]