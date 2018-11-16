# Privacy-Enhanced Dependable Searchable Homomorphic Key Value Store
Academic project developed for the Distributed Systems Reliability course. Development of a storage system that supports search and operation functions over encrypted data using a Homomorphic Encryption Library. The system is also Byzantine-fault and Intrusion tolerant.

## Running Instructions

```
docker run --name redis1 -d redis

docker run --name redis2 -d redis

docker run --name redis3 -d redis

docker run --name redis4 -d redis

docker run --name redis5 -d redis

docker run --name redis6 -d redis

docker run --name redis7 -d redis

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 0 172.17.0.2

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 1 172.17.0.3

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 2 172.17.0.4

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 3 172.17.0.5

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 4 172.17.0.6

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 5 172.17.0.7

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 6 172.17.0.8

docker run -t -i csd-work java -cp /home/csd/* -Djavax.net.ssl.keyStore=/home/csd/server.jks -Djavax.net.ssl.keyStorePassword=123456 server.ServerInterface 1001

docker run -t -i csd-work java -cp /home/csd/* -Djavax.net.ssl.trustStore=/home/csd/client.jks  -Djavax.net.ssl.trusttorePassword=123456 -Djavax.net.ssl.keyStore=/home/csd/server.jks -Djavax.net.ssl.keyStorePassword=123456 server.Proxy https://172.17.0.16:11100/

docker run -t -i csd-work java -cp /home/csd/* -Djavax.net.ssl.trustStore=/home/csd/client.jks -Djavax.net.ssl.trusttorePassword=123456 client.ClientInterface https://172.17.0.17:11100/



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
## Example benchmark command
``` 
docker run -t -i csd-work java -cp /home/csd/* -Djavax.net.ssl.trustStore=/home/csd/client.jks -Djavax.net.ssl.trustStorePassword=123456 client.Benchmark https://172.17.0.16:11100/ 1 3

``` 
