# My project's README

#Command to run the application on Docker:
docker run -t -i csd-work java -cp /home/csd/* foo.gettingstarted.RedisJava

#Command to run redis on Docker:
docker run --name some-redis -d redis

#Command to get info about the Redis Server:
docker inspect #id

#Command to run bft-SMaRt server:
java -cp bin/BFT-SMaRt.jar:lib/*:dist/foo.jar foo.gettingstarted.server.TreeMapServer 0

#Command to run bft-SMaRt client:
java -cp bin/BFT-SMaRt.jar:lib/*:dist/foo.jar foo.gettingstarted.client.ConsoleClient 1001
        
# Compile on maven in c9
mvn package

# Code to run client in c9
java -cp foo/bin/BFT-SMaRt.jar:foo/lib/*:target/CSD-TP1-0.0.1-SNAPSHOT.jar foo.gettingstarted.client.ConsoleClient 1001  

# Command to run server in c9
java -cp foo/bin/BFT-SMaRt.jar:foo/lib/*:target/CSD-TP1-0.0.1-SNAPSHOT.jar foo.gettingstarted.server.TreeMapServer 2

# Command to Start Docker on Linux
service docker start

# Docker stop and remove all containers
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

docker run -t -i csd-work java -cp /home/csd/* server.TreeMapServer 0
docker run -t -i csd-work java -cp /home/csd/* client.ConsoleClient 1001

TODO:   key PutSet (String key, Entry set)
        Entry = GetSet(String key)
        Status AddElement(String key)
        Status RemoveSet(String key)
        Status WriteElem(String key, type element, int Pos)
        Elem ReadElem(String key, int Pos)
        Boolean isElement(String key, String element)
        int Sum(int Pos, String key, String key)
        int Mult(int Pos, String key, String key)



