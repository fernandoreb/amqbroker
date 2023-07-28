
# AMQ Broker com TLS interno e externo, Autenticação e Monitoramento

Cenário contendo uma instalação de um Artemis, com TLS habilitado, autenticação e monitoramento.

Referência
https://github.com/amq-broker-hub/amq-on-openshift/tree/main/quickstart


## Openshift

Versão: 4.13   
Operator AMQ Streams: versão 2.4

## Instalação do Operator do AMQBroker

Via console web

- Red Hat Integration - AMQ Broker
  https://access.redhat.com/documentation/en-us/red_hat_amq/7.5/html/deploying_amq_broker_on_openshift/broker-operator-broker-ocp#operator-overview-broker-ocp

## Ajustes na monitoração do cluster para permitir monitoramento do usuário

Passos, via console web:

1. No projeto openshift-monitoring 
2. Editar o ConfigMap cluster-monitoring-config (OpenShift 4 web console, click em Workloads > ConfigMaps > cluster-monitoring-config)
3. Adicionar enableUserWorkload: true

Se não existir, criar:  
~~~
kind: ConfigMap
apiVersion: v1
metadata:
  name: cluster-monitoring-config
  namespace: openshift-monitoring
data:
  config.yaml: |
    enableUserWorkload: true
~~~

Maiores informações:   
* https://docs.openshift.com/container-platform/4.13/monitoring/enabling-monitoring-for-user-defined-projects.html

## Criação da namespace para instalação do broker
~~~
oc new-project infra-amqbroker
~~~

## Geração Keystore e Secret para conexão com TLS

### Geração da Keystore

As configurações abaixo foram criadas utilizando o nome e namespace do broker.
~~~
CN=amq-broker
amq-broker-wconsj-0-svc
amq-broker-wconsj-0-svc.infra-amqbroker.svc
amq-broker-ssl-0-svc
amq-broker-ssl-0-svc.infra-amqbroker.svc
~~~

Criação da Keystore
~~~
keytool -genkeypair -keyalg RSA -keystore broker.ks -storetype pkcs12 -storepass changeit -keypass changeit -dname 'CN=amq-broker' -ext san=dns:amq-broker-wconsj-0-svc,dns:amq-broker-wconsj-0-svc.infra-amqbroker.svc,dns:amq-broker-ssl-0-svc,dns:amq-broker-ssl-0-svc.infra-amqbroker.svc
~~~

### Criação da Secret
~~~
oc create secret generic broker-tls \
  --from-file=broker.ks=broker.ks \
  --from-file=client.ts=broker.ks \
  --from-literal=keyStorePassword=changeit \
  --from-literal=trustStorePassword=changeit -n infra-amqbroker
~~~
ou
~~~
kind: Secret
apiVersion: v1
metadata:
  name: broker-tls
  namespace: infra-amqbroker
data:
  broker.ks: >-
    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
  client.ts: >-
    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
  keyStorePassword: Y2hhbmdlaXQ=
  trustStorePassword: Y2hhbmdlaXQ=
type: Opaque
~~~

## Criação do Artemis

Os seguintes protocolos foram habilitados: *amqp,core,openwire,hornetq e stomp*.   

Algumas parametrizações:   
* sslSecret  - indica a secret a ser utilizada para o TLS.
* expose  - indica se o acceptor ou console serão expostos (via route).
* protocols - lista de protocolos habilitados. Pode-se criar um acceptor para cada um também.   

Outras parametrizações:   
https://access.redhat.com/documentation/en-us/red_hat_amq/7.5/html/deploying_amq_broker_on_openshift/reference-broker-ocp-broker-ocp#crd-params_broker-ocp

~~~
apiVersion: broker.amq.io/v2alpha5
kind: ActiveMQArtemis
metadata:
  name: amq-broker
  labels:
    application: amq-broker-app
spec:
  adminUser: admin
  adminPassword: admin
  acceptors:
    - name: ssl
      port: 61617
      protocols: amqp,core,openwire,hornetq,stomp
      sslEnabled: true
      sslSecret: broker-tls
      expose: true      
  console:
    expose: true
    sslEnabled: true
    sslSecret: broker-tls
  deploymentPlan:
    image: placeholder
    size: 1
    requireLogin: true
    persistenceEnabled: true
    enableMetricsPlugin: true
    storage:
      size: 2Gi
    livenessProbe:
      initialDelaySeconds: 120
    resources:
      requests:
        cpu: 100m
        memory: 600Mi
      limits:
        cpu: "1"
        memory: 2Gi
~~~

## Testando o broker - Aplicações Locais

### Aplicação Consumer

Copiar o arquivo broker.ks para a pasta *artemis-consumer-sb*

Ajustar as propriedades em: *artemis-consumer-sb/src/main/resources/application.properties*.

A string de conexão tem os seguintes parametros:

amqphub.amqp10jms.remoteUrl=<AMQ_PROTOCOL>://<AMQ_HOST>:<AMQ_PORT>?transport.trustStoreLocation=<AMQ_KEYSTORE>&transport.trustStorePassword=<AMQ_KEYSTORE_PASS>&transport.verifyHost=false

* AMQ_PROTOCOL - No caso amqp ou amqps
* AMQ_HOST - Host
* AMQ_PORT - Porta
* AMQ_KEYSTORE - Caminho da keystore
* AMQ_KEYSTORE_PASS - password da keystore

A aplicação está parametrizada para utilizar environments ou usar valores default. Lembrando de atualizar os valores default para a execução local.   
~~~
# AMQ
amqphub.amqp10jms.remoteUrl=${AMQ_PROTOCOL:amqps}://${AMQ_HOST:amq-broker-ssl-0-svc-rte-infra-amqbroker.apps-crc.testing}:${AMQ_PORT:443}?transport.trustStoreLocation=${AMQ_KEYSTORE:/home/fguimara/Fernando/projects/repos/middleware/amq-broker/broker.ks}&transport.trustStorePassword=${AMQ_KEYSTORE_PASS:changeit}&transport.verifyHost=false
amqphub.amqp10jms.username=${AMQ_USER:admin}
amqphub.amqp10jms.password=${AMQ_PASS:admin}
~~~

Executando 
~~~
mvn spring-boot:run
~~~

### Aplicação Producer

Copiar o arquivo broker.ks para a pasta *artemis-producer-sb*

Ajustar as propriedades em: *artemis-producer-sb/src/main/resources/application.properties*.

A string de conexão tem os seguintes parametros:

amqphub.amqp10jms.remoteUrl=<AMQ_PROTOCOL>://<AMQ_HOST>:<AMQ_PORT>?transport.trustStoreLocation=<AMQ_KEYSTORE>&transport.trustStorePassword=<AMQ_KEYSTORE_PASS>&transport.verifyHost=false

* AMQ_PROTOCOL - No caso amqp ou amqps
* AMQ_HOST - Host
* AMQ_PORT - Porta
* AMQ_KEYSTORE - Caminho da keystore
* AMQ_KEYSTORE_PASS - password da keystore
* AMQ_USER - Usuário
* AMQ_PASS - Senha

A aplicação está parametrizada para utilizar environments ou usar valores default. Lembrando de atualizar os valores default para a execução local.   
~~~
# AMQ
amqphub.amqp10jms.remoteUrl=${AMQ_PROTOCOL:amqps}://${AMQ_HOST:amq-broker-ssl-0-svc-rte-infra-amqbroker.apps-crc.testing}:${AMQ_PORT:443}?transport.trustStoreLocation=${AMQ_KEYSTORE:./broker.ks}&transport.trustStorePassword=${AMQ_KEYSTORE_PASS:changeit}&transport.verifyHost=false
amqphub.amqp10jms.username=${AMQ_USER:admin}
amqphub.amqp10jms.password=${AMQ_PASS:admin}
~~~

Executando 
~~~
mvn spring-boot:run
~~~

Enviando uma mensagem
~~~
curl --location --request POST 'http://localhost:8082/api/producer' \
--header 'Content-Type: application/json' \
--data-raw '{"message":"Hello"}'
~~~

Resultado - nos logs no consumer
~~~
2023-07-27 13:36:10.657  INFO 129082 --- [           main] c.r.cloudnative.ConsumerApplication      : Started ConsumerApplication in 1.446 seconds (JVM running for 1.658)
2023-07-27 13:36:28.994  INFO 129082 --- [ntContainer#0-1] c.r.c.controller.ConsumerController      : [mensagem] Mensagem recebida ({"message":"Hello"})
~~~

## Testando o broker - Aplicações no Openshift

### Deploy e Implantação no cluster - Aplicação Consumer

Os seguintes passos criam uma aplicação que irá consumir uma fila.   

Criação da namespace e environments
~~~
oc new-project amq-broker-app
NAME_SPACE=amq-broker-app
APP_NAME_CONSUMER=artemis-consumer-sb
~~~

Build da aplicação
~~~
oc new-build --binary --strategy=docker --name=${APP_NAME_CONSUMER} -n ${NAME_SPACE}
~~~

~~~
oc start-build ${APP_NAME_CONSUMER} --from-dir . -F
~~~

Após finalização do build, vamos criar a da aplicação
~~~
oc new-app ${APP_NAME_CONSUMER} -n ${NAME_SPACE} \
-e AMQ_HOST=amq-broker-hdls-svc.infra-amqbroker.svc.cluster.local \
-e AMQ_PORT=61617 \
-e AMQ_USER=admin \
-e AMQ_PASS=admin \
-e AMQ_KEYSTORE=/broker.ks \
-e AMQ_KEYSTORE_PASS=changeit
~~~

## Deploy e Implantação no cluster - Producer

Os seguintes passos criam uma aplicação que irá produzir dados uma fila, a partir de um endpoint.   

Criação da namespace e environments. (a namespace foi criada no passo anterior)
~~~
oc new-project amq-broker-app
NAME_SPACE=amq-broker-app
APP_NAME_PRODUCER=artemis-producer-sb
~~~

Build da aplicação
~~~
oc new-build --binary --strategy=docker --name=${APP_NAME_PRODUCER} -n ${NAME_SPACE}
~~~

~~~
oc start-build ${APP_NAME_PRODUCER} --from-dir . -F
~~~

Após finalização do build, vamos criar a da aplicação
~~~
oc new-app ${APP_NAME_PRODUCER} -n ${NAME_SPACE} \
-e AMQ_HOST=amq-broker-hdls-svc.infra-amqbroker.svc.cluster.local \
-e AMQ_PORT=61617 \
-e AMQ_USER=admin \
-e AMQ_PASS=admin \
-e AMQ_KEYSTORE=/broker.ks \
-e AMQ_KEYSTORE_PASS=changeit
~~~

Enviando uma mensagem
~~~
curl --location --request POST 'http://localhost:8080/api/producer' \
--header 'Content-Type: application/json' \
--data-raw '{"message":"Hello"}'
~~~

Resultado - nos logs no consumer
~~~
2023-07-27 13:36:10.657  INFO 129082 --- [           main] c.r.cloudnative.ConsumerApplication      : Started ConsumerApplication in 1.446 seconds (JVM running for 1.658)
2023-07-27 13:36:28.994  INFO 129082 --- [ntContainer#0-1] c.r.c.controller.ConsumerController      : [mensagem] Mensagem recebida ({"message":"Hello"})
~~~

~~~
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: amq-broker
spec:
  endpoints:
    - interval: 30s
      port: wconsj-.*
      scheme: https
      tlsConfig:
        insecureSkipVerify: true
  podTargetLabels:
    - ActiveMQArtemis
    - application
  selector:
    matchLabels:
      ActiveMQArtemis: amq-broker
  jobLabel: ActiveMQArtemis
~~~

## Grafana   

1 - Retornar ao namespace do broker
~~~
oc project infra-amqbroker
~~~

2 - Criação de Service Account
~~~
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: grafana-serviceaccount
  namespace: infra-amqbroker    
  labels:
    app: strimzi
~~~

3 - Criação da RoleBinding
~~~
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: grafana-cluster-monitoring-binding-broker
  namespace: infra-amqbroker    
  labels:
    app: strimzi
subjects:
  - kind: ServiceAccount
    name: grafana-serviceaccount
    namespace: infra-amqbroker    
roleRef:
  kind: ClusterRole
  name: cluster-monitoring-view
  apiGroup: rbac.authorization.k8s.io
~~~

4 - Recuperar o token da conta de serviço

~~~
--- In OCP 3.x and OCP 4.10 or lower ---
$ oc sa get-token -n ${NAMESPACE} ${SERVICE_ACCOUNT}

--- In OCP 4.11 or higher ---
$ oc create token -n ${NAMESPACE} ${SERVICE_ACCOUNT}
~~~
Vamos usar:
~~~
oc create token -n infra-amqbroker grafana-serviceaccount
~~~

5 - Atualizar o token no arquivo grafana/datasource.yaml (no lugar do XXXXXX)   
**Obs: Quando o token expirar, é possível atualizar do datasource do grafana, seguindo o padrão "Bearer TOKEN" no campo value em custom headers no header Auhorization.**

6 - Criar o configmap com o token atualizado.
~~~
oc create configmap grafana-config --from-file=grafana/datasource.yaml -n infra-amqbroker
~~~

7 - Criar o grafana
~~~
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  labels:
    app: strimzi
spec:
  replicas: 1
  selector:
    matchLabels:
      name: grafana
  template:
    metadata:
      labels:
        name: grafana
    spec:
      serviceAccountName: grafana-serviceaccount
      containers:
      - name: grafana
        image: grafana/grafana:7.5.15
        ports:
        - name: grafana
          containerPort: 3000
          protocol: TCP
        volumeMounts:
        - name: grafana-data
          mountPath: /var/lib/grafana
        - name: grafana-logs
          mountPath: /var/log/grafana
        - name: grafana-config
          mountPath: /etc/grafana/provisioning/datasources/datasource.yaml
          readOnly: true
          subPath: datasource.yaml
        readinessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 5
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 15
          periodSeconds: 20
      volumes:
      - name: grafana-data
        emptyDir: {}
      - name: grafana-logs
        emptyDir: {}
      - name: grafana-config
        configMap:
          name: grafana-config
---
apiVersion: v1
kind: Service
metadata:
  name: grafana
  labels:
    app: strimzi
spec:
  ports:
  - name: grafana
    port: 3000
    targetPort: 3000
    protocol: TCP
  selector:
    name: grafana
  type: ClusterIP
~~~

8 - Expor a rota
~~~
oc create route edge grafana-route --service=grafana --namespace=infra-amqbroker
~~~

9 - Acessando

Usuário e senha: admin/admin
