#stage1 - build da aplicação
FROM registry.access.redhat.com/ubi8/openjdk-11:1.15-1.1679485219 as builder

COPY src src
COPY pom.xml .

RUN mvn clean package

#stage2 - geração da imagem final
FROM registry.access.redhat.com/ubi8/openjdk-11-runtime:1.15-1.1679485252

ENV JAR_NAME=artemis-consumer-sb-1.0-SNAPSHOT

COPY broker.ks /broker.ks

COPY --from=builder /home/jboss/target .
ENV JAR_FILE=/home/jboss/${JAR_NAME}.jar
RUN cp ${JAR_FILE} /home/jboss/app.jar
EXPOSE 8080 8081
ENTRYPOINT ["java","-jar","/home/jboss/app.jar","-Djava.net.preferIPv4Stack=true -Dspring.cloud.kubernetes.enabled=false"]
