FROM openjdk:11-jre-slim-buster
RUN addgroup --gid 10007 filmbooking && \
    adduser --uid 10007 --gid 10007 keith

# ARG DEPENDENCY=target/dependency
# COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
# COPY ${DEPENDENCY}/META-INF /app/META-INF
# COPY ${DEPENDENCY}/BOOT-INF/classes /app
# ENTRYPOINT ["java","-cp","app:app/lib/*",\
#             "com.filmbooking.booking_service.BookingServiceApplication"]

RUN mkdir -p /app
RUN chown keith /app
USER keith:filmbooking
WORKDIR /app

ADD target/booking_service-1.1.0.jar \
    /app/booking_service-1.1.0.jar

CMD java -Xms256m -Xmx256m -Xss512k -XX:+UseContainerSupport \
        -Dserver.port=$PORT $JAVA_OPTS \
        -jar /app/booking_service-1.1.0.jar
