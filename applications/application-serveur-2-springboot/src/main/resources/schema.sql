CREATE SCHEMA formation;

CREATE TABLE formation.VIN (
	id serial PRIMARY KEY,
	chateau VARCHAR(100) NOT NULL,
	appellation VARCHAR(100),
	prix DECIMAL);

CREATE SEQUENCE formation.vin_id_seq start 1 increment 1;