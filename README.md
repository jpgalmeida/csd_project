# My project's README


## Running Instructions

```
docker run --name redis1 -d redis

docker run --name redis2 -d redis

docker run --name redis3 -d redis

docker run --name redis4 -d redis

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 0 172.17.0.2

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 1 172.17.0.3

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 2 172.17.0.4

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 3 172.17.0.5

docker run -t -i csd-work java -cp /home/csd/* -Djavax.net.ssl.keyStore=/home/csd/server.jks -Djavax.net.ssl.keyStorePassword=123456 server.ServerInterface 1001

docker run -t -i csd-work java -cp /home/csd/* -Djavax.net.ssl.trustStore=/home/csd/client.jks -Djavax.net.ssl.trusttorePassword=123456 client.ClientInterface https://172.17.0.10:11100/

```

## Command to get info about the Redis Server:
```
docker inspect #id
       
```
## Command to Start Docker on Linux
```
service docker start
```
## Docker stop
```
docker stop $(docker ps -a -q)
```

## Docker remove all containers
``` 
docker rm $(docker ps -a -q)
```