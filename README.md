# My project's README


## Running Instructions
1) docker run --name redis1 -d redis
2) docker run --name redis2 -d redis
3) docker run --name redis3 -d redis
4) docker run --name redis4 -d redis
5) docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 0 172.17.0.2
6) docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 1 172.17.0.3
7) docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 2 172.17.0.4
8) docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 3 172.17.0.5
9) docker run -t -i csd-work java -cp /home/csd/* -Djavax.net.ssl.keyStore=/home/csd/server.jks -Djavax.net.ssl.keyStorePassword=123456 server.ServerInterface 1001

## Command to get info about the Redis Server:
docker inspect #id
       

## Command to Start Docker on Linux
service docker start

## Docker stop 
docker stop $(docker ps -a -q)


## Docker remove all containers 
docker rm $(docker ps -a -q)
