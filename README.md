# My project's README


#Command to run the application on Docker:
docker run -t -i csd-work java -cp /home/csd/* foo.gettingstarted.RedisJava


#Command to run redis on Docker:
docker run --name some-redis -d redis


#Command to get info about the Redis Server:
docker inspect #id
       

# Command to Start Docker on Linux
service docker start


# Docker stop 
docker stop $(docker ps -a -q)


# Docker remove all containers 
docker rm $(docker ps -a -q)


# Docker run examples
docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 0
docker run -t -i csd-work java -cp /home/csd/* client.ConsoleClient 1001


# Testing:
1) docker run --name redis1 -d redis
2) docker run --name redis2 -d redis
3) docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 0 172.17.0.2
4) docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 1 172.17.0.3
5) docker run -t -i csd-work java -cp /home/csd/* client.ConsoleClient 1001

# Run Server and Client
1) docker run --network=sd-net -t -i --name redis1 -d redis
2) docker run --network=sd-net -t -i csd-work java -cp /home/csd/* -Djavax.net.ssl.keyStore=/home/csd/server.jks -Djavax.net.ssl.keyStorePassword=123456 server.ServerInterface 172.18.0.2
3) docker run --network=sd-net -t -i csd-work java -cp /home/csd/* -Djavax.net.ssl.trustStore=/home/csd/client.jks -Djavax.net.ssl.trustStorePassword=123456 client.ClientInterface https://172.18.0.3:8080/

