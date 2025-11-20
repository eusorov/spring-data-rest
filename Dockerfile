# Using Oracle GraalVM for JDK 25
FROM container-registry.oracle.com/graalvm/native-image:25 AS builder

# Set the working directory to /home/app
WORKDIR /app

# Copy build configuration files first (for better caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies (this layer will be cached if build files don't change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build native image
RUN ./gradlew nativeCompile --no-daemon

# The deployment Image
FROM container-registry.oracle.com/os/oraclelinux:9-slim
EXPOSE 8080

# Copy the native executable into the containers
WORKDIR /app
COPY --from=builder /app/build/native/nativeCompile .
ENTRYPOINT ["/app/spring-data-rest"]