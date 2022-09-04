FROM debezium/connect-base:2.0

USER root
COPY . /build
RUN cd /build                                                                  \
 && chmod +x ./gradlew                                                         \
 && ./gradlew --no-daemon shadowJar                                            \
 && cd /                                                                       \
 && mkdir -p /kafka/connect/connector                                          \
 && cp /build/build/libs/* /kafka/connect/connector/                           \
 && rm -rf /build
