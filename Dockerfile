# Build the jar using a maven image
FROM maven:3.9-amazoncorretto-11 AS build

COPY . /home/liger/src

RUN mvn -f /home/liger/src/pom.xml clean package

# liger_resources/rules is a local-dev symlink into the xleplusglue checkout (not
# resolvable in a Docker build context). Pull the real files from xleplusglue's
# public repo so this rarely-built image doesn't ship a dangling symlink.
FROM alpine/git:latest AS liger_resources_rules

RUN git clone --depth 1 --filter=blob:none --sparse https://github.com/Mmaz1988/xleplusglue.git /xleplusglue \
    && cd /xleplusglue \
    && git sparse-checkout set liger_resources/rules

# Start from a jdk base image
FROM eclipse-temurin:17.0.9_9-jdk

EXPOSE 8080

# install jemalloc as alternative malloc implementation (to be more robust to memory fragmentation)
RUN apt-get update && apt-get install --yes --no-install-recommends libjemalloc2

# set jemalloc as default malloc in env variable
ENV LD_PRELOAD=/usr/lib/x86_64-linux-gnu/libjemalloc.so.2

# Copy from build stage to keep image size small
COPY --from=build /home/liger/src/target/syntax-annotator-glue-0.0.1-SNAPSHOT.jar app.jar

# copy necessary resource files
COPY liger_resources liger_resources

# replace the dangling liger_resources/rules symlink with the real files
RUN rm -f liger_resources/rules
COPY --from=liger_resources_rules /xleplusglue/liger_resources/rules liger_resources/rules

## ENTRYPOINT ["java", "-jar", "/app.jar"]
ENTRYPOINT exec java -jar /app.jar -web
