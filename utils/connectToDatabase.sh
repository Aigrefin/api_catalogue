#!/bin/bash
docker run -it --rm --link some-postgres:postgres postgres psql -h postgres -U postgres
