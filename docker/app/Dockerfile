FROM adoptopenjdk/openjdk11:alpine

LABEL io.k8s.description="Platform for Java (fatjar) runtime" \
      io.openshift.expose-services="8080:http" \
      io.openshift.tags="java,kotlin,microservices,fatjar,spring"

WORKDIR /deployments

# TODO - Application
COPY build/kotlin-api-rest-service-example.jar .
# NewRelic java agent
COPY build/newrelic.jar /opt/newrelic/newrelic.jar
COPY build/newrelic.yml /opt/newrelic/newrelic.yml

EXPOSE 8080

# TODO - needs to be fixed with the correct command to run
CMD ["java", "-jar", "kotlin-api-rest-service-example.jar"]
