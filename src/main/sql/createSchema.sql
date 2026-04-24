drop table if exists Rental;
drop table if exists Court;
drop table if exists Club;
drop table if exists User_Table;



create table User_Table
(
    uid   serial primary key,
    name  varchar(80)  not null,
    email varchar(80)  not null unique check (email like '%@%'),
    password varchar(255) not null,
    token varchar(255) not null unique
);

create table Club
(
    cid   serial primary key,
    name  varchar(80) not null,
    owner int         not null,
    FOREIGN KEY (owner) REFERENCES User_Table (uid)
);

create table Court
(
    crid serial primary key,
    name varchar(80) not null,
    cid  int         not null,
    FOREIGN KEY (cid) REFERENCES Club (cid)
);
CREATE TABLE Rental
(
    rid            SERIAL PRIMARY KEY,
    crid           INT       NOT NULL,
    uid            INT       NOT NULL,
    rental_date    TIMESTAMP NOT NULL,
    rental_enddate TIMESTAMP NOT NULL,
    duration_hours INT       NOT NULL CHECK (duration_hours > 0 AND duration_hours < 24),
    FOREIGN KEY (crid) REFERENCES Court (crid),
    FOREIGN KEY (uid) REFERENCES User_Table (uid)
);