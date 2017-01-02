#!/bin/bash
docker run --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword -P -d postgres
