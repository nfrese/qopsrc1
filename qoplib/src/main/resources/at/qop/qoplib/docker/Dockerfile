FROM camptocamp/postgis:9.5

ENV POSTGRES_USER qopuser
ENV POSTGRES_DB qop
ADD dump.sql /docker-entrypoint-initdb.d/01_qop.sql