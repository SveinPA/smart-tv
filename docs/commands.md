# Commands to run application

## Build all (Without IT)
```text
mvn -q -DskipITs package
```
## Run server (From root)
```text
mvn -q -pl tv-server exec:java -Dexec.args="--port 1238"
```
## Run client (From Root)
```text
mvn -q -pl remote-client exec:java -Dexec.args="localhost 1238"
```
## Run Integration tests
```text
mvn -q verify
```