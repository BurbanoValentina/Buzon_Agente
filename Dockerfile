# --- Etapa 1: Compilación ---
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia los archivos de configuración y el código fuente del backend
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src

# Compila el proyecto y empaqueta el JAR, saltando los tests para agilizar el despliegue
RUN mvn clean package -DskipTests

# --- Etapa 2: Ejecución ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no root para mayor seguridad en contenedor
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia el JAR generado desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

# Límite de memoria optimizado para el plan gratuito de Render (512 MB)
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseG1GC"

# Expone el puerto configurable (por defecto 8081 o 8080)
EXPOSE 8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8081} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
