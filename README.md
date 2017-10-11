# My project's README

Command to run the application on Docker:
docker run -t -i csd-work java -cp /home/csd/* RedisJava

Command to run redis on Docker:
docker run --name some-redis -d redis

Command to get info about the Redis Server:
docker inspect #id


Command to run bft-SMaRt server:
java -cp bin/BFT-SMaRt.jar:lib/*:dist/foo.jar foo.gettingstarted.server.TreeMapServer 0

Command to run bft-SMaRt client:
java -cp bin/BFT-SMaRt.jar:lib/*:dist/foo.jar foo.gettingstarted.client.ConsoleClient 1001