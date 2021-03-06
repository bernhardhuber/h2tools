---
--- Create Tabls BUG_ENTITY, and BUG_CONFIG_ENTITY
---

---
DROP TABLE IF EXISTS "PUBLIC"."BUG_ENTITY";

CREATE SEQUENCE IF NOT EXISTS "PUBLIC"."BUG_ENTITY_SEQ" START WITH 1 BELONGS_TO_TABLE; 

CREATE CACHED TABLE IF NOT EXISTS "PUBLIC"."BUG_ENTITY"(
    "ID" BIGINT DEFAULT (NEXT VALUE FOR "PUBLIC"."BUG_ENTITY_SEQ") NOT NULL NULL_TO_DEFAULT SEQUENCE "PUBLIC"."BUG_ENTITY_SEQ",
    "VERSION" BIGINT NOT NULL,
    "CREATED_WHEN" TIMESTAMP,
    "UPDATED_WHEN" TIMESTAMP,
    "BUG_ID" VARCHAR(128) NOT NULL,
    "BUG_TITLE" VARCHAR(512) NOT NULL,
    "BUG_DESCRIPTION" CLOB,
    "BUG_PRIORITY" VARCHAR(64) NOT NULL,
    "BUG_STATUS" VARCHAR(64) NOT NULL
);     
ALTER TABLE "PUBLIC"."BUG_ENTITY" ADD CONSTRAINT "PUBLIC"."PK_BUG_ENTITY" PRIMARY KEY("ID");    
ALTER TABLE "PUBLIC"."BUG_ENTITY" ADD CONSTRAINT "PUBLIC"."UK_BUG_ENTITY_BUG_ID" UNIQUE("BUG_ID");        

---
DROP TABLE IF EXISTS "PUBLIC"."BUG_CONFIG_ENTITY";

CREATE SEQUENCE IF NOT EXISTS "PUBLIC"."BUG_CONFIG_ENTITY_SEQ" START WITH 1 BELONGS_TO_TABLE; 

CREATE CACHED TABLE IF NOT EXISTS "PUBLIC"."BUG_CONFIG_ENTITY"(
    "ID" BIGINT DEFAULT (NEXT VALUE FOR "PUBLIC"."BUG_CONFIG_ENTITY_SEQ") NOT NULL NULL_TO_DEFAULT SEQUENCE "PUBLIC"."BUG_CONFIG_ENTITY_SEQ",
    "VERSION" BIGINT NOT NULL,
    "CREATED_WHEN" TIMESTAMP,
    "CONFIG_DESCRIPTION" VARCHAR(2048),
    "CONFIG_KEY" VARCHAR(256) NOT NULL,
    "UPDATED_WHEN" TIMESTAMP,
    "CONFIG_VALUE" VARCHAR(255)
); 

---
ALTER TABLE "PUBLIC"."BUG_CONFIG_ENTITY" ADD CONSTRAINT "PUBLIC"."PK_BUG_CONFIG_ENTITY" PRIMARY KEY("ID");             
ALTER TABLE "PUBLIC"."BUG_CONFIG_ENTITY" ADD CONSTRAINT "PUBLIC"."UK_BUG_CONFIG_ENTITY_CONFIG_KEY" UNIQUE("CONFIG_KEY");         
