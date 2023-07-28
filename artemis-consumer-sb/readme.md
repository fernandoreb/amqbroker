Referência: 

[https://developers.redhat.com/articles/2022/05/31/integrate-spring-boot-application-red-hat-data-grid#how_to_deploy_the_spring_boot_project](https://developers.redhat.com/articles/2022/05/31/integrate-spring-boot-application-red-hat-data-grid#how_to_deploy_the_spring_boot_project)

Gerar uma keystore:

pegar o arquivo tls.crt na secret example-infinispan-cert-secret

gerar um arquivo local e rodar o comando;
~~~
keytool -importcert -keystore truststore.jks -alias server -file tls.crt
~~~
colocar a senha password

criar a secret
~~~
oc create secret generic truststore-secret --from-file=truststore.jks
~~~

montar a secret na aplicação

~~~
oc set volume dc/portal-cache-funcionalidades-sb --add --name=truststore-secret -m /mnt/secrets1/ -t secret --secret-name=truststore-secret --default-mode='0755’
~~~

Imagem Docker 

~~~
mvn package
podman build . -t quay.io/rh_ee_fguimara/portal-cache-funcionalidades-sb:1.0.1
~~~

Criar essa cache do Datagrid
Nome: portal-perfil-funcionalidade
~~~ json
{
  "distributed-cache": {
    "mode": "SYNC",
    "encoding": {
      "media-type": "application/x-java-serialized-object"
    },
    "statistics": true
  }
}
~~~

**Para rodar a aplicação local:**

1. Executar o DataGrid local:
~~~ bash
podman run --replace -d --name jdg -p 11222:11222 -e "USER=teste" -e "PASS=teste" registry.redhat.io/datagrid/datagrid-8-rhel8
~~~

2. Entrar no container e criar o cache (usar o usuário e senha do passo anterior):
~~~ bash
podman exec -it jdg bash
vi cache.json  # salvar o json do cache (acima)
./bin/cli.sh
connect
create cache --file=cache.json portal-perfil-funcionalidade
~~~

3. Comentar a sessão "Encryption" do `application.properties`

4. Executar a app no podman/docker local:  
`DB_HOST` utilizado é o IP do gateway padrão da interface de rede do Podman/Docker
~~~ bash
podman run --replace -d --name portal-cache-funcionalidades-sb \
-e "DB_DATASOURCE=postgres" -e "DB_HOST=10.88.0.1" -e "DB_PASS=mysecretpassword" -e "DB_PORT=5432" -e "DB_USER=postgres" \
-e "SERVICE_HOSTNAME=10.88.0.1" -e "USER_NAME=teste" -e "USER_PASSWORD=teste" \
-p 8081:8081 -d quay.io/rh_ee_lalmeida/portal-cache-funcionalidades-sb:1.0.1
~~~
