// create keyspace
CREATE KEYSPACE product WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'};
// create table
CREATE TABLE PRODUCT_DETAILS(PRICE DECIMAL,PRODUCT_ID BIGINT PRIMARY KEY);
// create index

INSERT INTO PRODUCT_DETAILS(PRICE,PRODUCT_ID) VALUES (12.99,13860428);
INSERT INTO PRODUCT_DETAILS(PRICE,PRODUCT_ID) VALUES (12.99,13860429);
INSERT INTO PRODUCT_DETAILS(PRICE,PRODUCT_ID) VALUES (12.99,1386042);