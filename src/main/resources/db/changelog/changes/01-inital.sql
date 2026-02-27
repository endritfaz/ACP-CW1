--liquibase formatted sql

--changeset ilp:001
create SCHEMA IF NOT EXISTS ilp;

CREATE TABLE IF NOT EXISTS ilp.drone_service_points (
                                                        id int primary key,
                                                        name varchar(100) not null,
    latitude decimal(9,6) not null,
    longitude decimal(9,6) not null,
    altitude integer not null
    );


CREATE TABLE IF NOT EXISTS ilp.drones (
                                          id varchar(50) primary key,
    name varchar(100) not null,
    cooling boolean default false not null,
    heating boolean default false not null,
    capacity numeric(10,2) not null,
    maxMoves integer not null,
    costPerMove numeric(10,2) not null,
    costInitial numeric(10,2) not null,
    costFinal numeric(10,2) not null
    costPer100Moves numeric(10, 2) not null
    );

CREATE TABLE IF NOT EXISTS ilp.drone_service_point_availability (
                                                                    id uuid primary key,
                                                                    drone_id varchar(50) not null,
    service_point_id int not null,
    day_of_week integer not null,
    start_time time not null,
    end_time time not null,
    foreign key (drone_id) references ilp.drones(id),
    foreign key (service_point_id) references ilp.drone_service_points(id)
    );