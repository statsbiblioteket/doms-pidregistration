CREATE ROLE "pidreg" LOGIN PASSWORD 'pidreg'
NOINHERIT CREATEDB
VALID UNTIL 'infinity';

CREATE DATABASE "pidreg-devel"
WITH
TEMPLATE=template0
ENCODING='SQL_ASCII'
OWNER="pidreg";