FROM camptocamp/postgis:9.6

ENV POSTGRES_USER qopuser
ENV POSTGRES_DB qop
ADD dump.sql /docker-entrypoint-initdb.d/01_qop.sql