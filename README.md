database table

create table ftl_user
(
    username varchar(50)   not null
        primary key,
    password varchar(255)  not null,
    score    int default 0 not null
);
