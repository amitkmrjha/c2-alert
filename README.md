# Akka serverless c2-alert app

Prerequisite : Docker installation to test locally .


gradle jib plugin is used to create and publish the image to docker hub registry.
run below command in host-ip root directory
````
> gradle build jib
````
#### Test locally using docker

Step 1 : First we need to start side car(proxy) service . Run below command  

````
> docker run -it --rm --name c2-alert-proxy -p 9000:9000 --env USER_FUNCTION_HOST=host.docker.internal cloudstateio/cloudstate-proxy-dev-mode:0.5.1-98-a596eae7 -e "DEBUG=cloudstate*"
````
Step 2 : Download the latest version of docker image from docker hub.We pushed the image while performing `build jib` previously.
````
> docker pull <docker-username>/c2-alert   
````
Step 3 : Run c2-alert hostip service.
````
> docker run -it --rm --name c2-alert -p 8080:8080 -v `pwd`/logs:/var/log <docker-username>/c2-alert:latest  
````

#### Deploy to akka serverless
 Once the docker image is pushed to docker hub, we can deploy our service to akka serverless
 
Step 1. Deploy
````
> akkasls svc deploy amit-c2-alert <docker-username>/c2-alert:latest


Service 'amit-c2-alert' was successfully deployed.
````

Step 2. Check the status of the amit-c2-alert Service and confirm it is "Ready" before moving to the next step
````
> akkasls svc list


NAME          AGE  REPLICAS   STATUS   DESCRIPTION
amit-c2-alert   2m   1          Ready
````
Step 3. Expose the amit-c2-alert Service
````
> akkasls svc expose amit-c2-alert --enable-cors


Service 'amit-c2-alert' was successfully exposed at: <service-url>.apps.akkaserverless.com
Note: the expose command is only required after the first deployment of the service. Subsequent deployments will reuse the same configuration.
````
Step 4. If at some point you forget or misplace your service's URL, you may obtain it using the Akka Serverless command line interface:
````
> akkasls svc get amit-c2-alert

````

Step 5. Test Post Request . Add host ip.
````
> grpcurl -d '{"app_sha256":"entityid1","ip":12346}' <service-url>.apps.akkaserverless.com:443 com.hackathon.hostip.HostIp/AddHostIp

````
Http request can also be used to create a post on Host ip entity
````
> curl --location --request POST 'https://<service-url>.apps.akkaserverless.com/com.hackathon.hostip.IpEvent/hostip/entityid1/ip/add' \
--header 'Content-Type: application/json' \
--data-raw '{
    "app_sha256":"entityid1",
    "ip":12346

}'

````
Step 6. Query host ip
````
> grpcurl -d '{"app_sha256":"entityid1"}' <service-url>.apps.akkaserverless.com:443 com.hackathon.hostip.HostIp/GetHostIp

````
Http request can also be used to create a Get on Host ip entity
````
> curl --location --request GET 'https://<service-url>.apps.akkaserverless.com/com.hackathon.hostip.IpEvent/hostip/entityid1'

````