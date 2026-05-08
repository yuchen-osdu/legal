FROM azul/zulu-openjdk:17
WORKDIR /app
ARG PROVIDER_NAME
ENV PROVIDER_NAME core-plus
ARG PORT
ENV PORT $PORT

COPY tmp/os-oqm-rabbitmq-*.jar plugins/oqm-rabbitmq.jar
COPY tmp/os-obm-s3-*.jar plugins/osm-s3.jar
COPY tmp/os-osm-postgres-*.jar plugins/osm-postgres.jar

# Copy the jar to the production image from the builder stage.
COPY legal-${PROVIDER_NAME}/target/legal-${PROVIDER_NAME}-*-spring-boot.jar legal-${PROVIDER_NAME}.jar
# Add a non-root user
RUN groupadd -g 10001 -r nonroot \
  && useradd -g 10001 -r -u 10001 nonroot
# Run as non-root user
USER 10001:10001
# Run the web service on container startup.
#--add-opens args used to open modules and allow illegal(reflection\private classes and fields) access from the code.
CMD java --add-opens java.base/java.lang=ALL-UNNAMED \
         --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
         -Djava.security.egd=file:/dev/./urandom \
         -Dserver.port=${PORT} \
         -Dlog4j.formatMsgNoLookups=true \
         -Dloader.path=plugins/ \
         -Dloader.main=org.opengroup.osdu.legal.LegalApplication \
         -jar /app/legal-${PROVIDER_NAME}.jar
