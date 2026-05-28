#!/bin/bash
java \
 -Djdbc.driver=org.h2.Driver \
 -Djdbc.url=jdbc:h2:file:${HOME}/flmane-data/flmane \
 -Djdbc.user=sa \
 -Djdbc.password= \
 -Dhibernate.dialect=org.hibernate.dialect.H2Dialect \
 -Dhibernate.hbm2ddl.auto=update \
 -jar flmane.jar