---
--- Create TABLE EVENT_ENTITY
---

DROP TABLE IF EXISTS "PUBLIC"."EVENT_ENTITY";

CREATE SEQUENCE IF NOT EXISTS "PUBLIC"."EVENT_ENTITY_SEQ" START WITH 1 BELONGS_TO_TABLE; 

CREATE CACHED TABLE "PUBLIC"."EVENT_ENTITY"(
    "ID" BIGINT DEFAULT (NEXT VALUE FOR "PUBLIC"."EVENT_ENTITY_SEQ") NOT NULL NULL_TO_DEFAULT SEQUENCE "PUBLIC"."EVENT_ENTITY_SEQ",
    "CREATED_BY" VARCHAR(255),
    "CREATED_WHEN" TIMESTAMP,
    "UPDATED_BY" VARCHAR(255),
    "UPDATED_WHEN" TIMESTAMP,
    "VERSION" BIGINT NOT NULL,
    "BOOKING_STATUS" VARCHAR(20),
    "CODE" VARCHAR(128) NOT NULL,
    "DESCRIPTION" VARCHAR(1024),
    "COMMUNE" VARCHAR(256),
    "DATETIME_END" TIMESTAMP,
    "DATETIME_FULL_DAY" BOOLEAN,
    "DATETIME_START" TIMESTAMP,
    "DATETIME_SERIE" BOOLEAN,
    "LOCATION" VARCHAR(256),
    "ORGANIZER" VARCHAR(256),
    "NAME" VARCHAR(256) NOT NULL
); 

ALTER TABLE "PUBLIC"."EVENT_ENTITY" ADD CONSTRAINT "PUBLIC"."PK_EVENT_ENTITY" PRIMARY KEY("ID");  
