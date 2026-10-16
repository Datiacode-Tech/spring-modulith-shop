-- Spring Modulith Event Publication Registry (JPA mapping of spring-modulith-events-jpa 2.1.1).
-- Generated from Hibernate's DDL for the entity; text columns because serialized events exceed 255 chars.
CREATE TABLE event_publication (
    id                     UUID                        NOT NULL PRIMARY KEY,
    listener_id            TEXT                        NOT NULL,
    event_type             TEXT                        NOT NULL,
    serialized_event       TEXT                        NOT NULL,
    publication_date       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP(6) WITH TIME ZONE,
    status                 VARCHAR(20)
        CHECK (status IN ('PUBLISHED', 'PROCESSING', 'COMPLETED', 'FAILED', 'RESUBMITTED')),
    completion_attempts    INTEGER                     NOT NULL,
    last_resubmission_date TIMESTAMP(6) WITH TIME ZONE
);

CREATE INDEX event_publication_completion_date_idx ON event_publication (completion_date);
CREATE INDEX event_publication_status_idx ON event_publication (status);
