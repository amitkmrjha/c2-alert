# Akka serverless c2-alert app

Prerequisite : Docker installation to test locally .


gradle jib plugin is used to create and publish the image to docker hub registry.
run below command in host-ip root directory
````
gradle build jib
````
#### Test locally using docker

Step 1 : First we need to start side car(proxy) service . Run below command  

````
docker run -it --rm --name c2-alert-proxy -p 9000:9000 --env USER_FUNCTION_HOST=host.docker.internal cloudstateio/cloudstate-proxy-dev-mode:0.5.1-98-a596eae7 -e "DEBUG=cloudstate*"
````
Step 2 : Download the latest version of docker image from docker hub.We pushed the image while performing `build jib` previously.
````
docker pull amitjha12/c2-alert   
````
Step 3 : Run c2-alert hostip service.
````
docker run -it --rm --name c2-alert -p 8080:8080 -v `pwd`/logs:/var/log amitjha12/c2-alert:latest  
````

