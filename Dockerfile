# Using Oracle GraalVM for JDK 25
FROM container-registry.oracle.com/graalvm/native-image:25 AS builder

# Set the working directory to /home/app
WORKDIR /app

# Copy the source code into the image for building
COPY . /app

# Build
RUN ./gradlew nativeCompile

# The deployment Image
FROM container-registry.oracle.com/os/oraclelinux:9-slim
EXPOSE 8080

# Copy the native executable into the containers
WORKDIR /app
COPY --from=builder /app/build/native/nativeCompile .
ENTRYPOINT ["/app/spring-data-rest"]