CREATE TABLE ATOM_LINK  (
        ENTRY_ID char(16) for bit data NOT NULL,
        REL VARCHAR(100) NOT NULL,
        HREF VARCHAR(200) NOT NULL,
        CONTENT_TYPE VARCHAR(100)
    );

ALTER TABLE ATOM_LINK
        ADD CONSTRAINT FK_ATOM_LINK FOREIGN KEY
                (ENTRY_ID)
        REFERENCES ATOM_ENTRY
                (ENTRY_ID)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
        ENFORCED;
