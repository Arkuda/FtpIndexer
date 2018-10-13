***Ftp Indexer***

Small ftp indexer written in kotlin + apache.commons.net

---
To run jar execute
`java -jar indexer.jar args`

Help arguments:\
`-ip=X`     To set ip of server to index\
`-p=X`      To set port of server\
`-o=tbl`  Exit file to be csv table\
`-o=txt`  Exit file to be txt, default method\
`-l=X`    File with ip's \
`-d`      Full log
---
To build project into jar file run\
`mvn jar:jar`
