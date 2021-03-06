CREATE TABLE ATOM_AUTHOR  (
        ENTRY_ID VARCHAR(100) NOT NULL,
        AUTHOR VARCHAR(100) NOT NULL
    );

ALTER TABLE ATOM_LINK
        ADD CONSTRAINT FK_ATOM_AUTHOR FOREIGN KEY
                (ENTRY_ID)
        REFERENCES ATOM_ENTRY
                (ENTRY_ID)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
        ENFORCED;

