FROM navikt/java:11-appdynamics

ENV APPD_ENABLED=true \
    APPD_NAME=dp-regel-api-arena-adapter

COPY build/libs/*.jar app.jar
