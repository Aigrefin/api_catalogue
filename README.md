# api_catalogue

Basically, a very simple API catalogue with an human interface and a http/json API.
Linked article coming soon.

## How to start from the sources

You can create the Postgre database with [this script](https://github.com/Aigrefin/api_catalogue/blob/master/utils/createDatabase.sh). It start a docker container.

Connect to the database with [this script](https://github.com/Aigrefin/api_catalogue/blob/master/utils/connectToDatabase.sh) and `CREATE DATABASE cataloguedb;`.

Don't forget to update the [application.properties](https://github.com/Aigrefin/api_catalogue/blob/master/application-default.properties) with the database port. You can get this port with `docker inspect <containerId>` (find this id with `docker ps`) and find `HostPort` in `NetworkSettings`.

Then `mvn spring-boot:run` in the project directory, and hit [http://localhost:8082](http://localhost:8082) in your browser.
