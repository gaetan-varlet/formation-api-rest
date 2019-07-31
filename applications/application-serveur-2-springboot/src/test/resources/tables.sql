CREATE SCHEMA formation;

CREATE TABLE formation.VIN (
	id serial PRIMARY KEY,
	chateau VARCHAR(100) NOT NULL,
	appellation VARCHAR(100),
	prix DECIMAL);

CREATE SEQUENCE formation.vin_id_seq start 1 increment 1;


CREATE TABLE formation.TRACE (
	id serial PRIMARY KEY,
	idep VARCHAR(10),
	time TIMESTAMP NOT NULL,
	url_serveur VARCHAR(100) NOT NULL,
	endpoint VARCHAR(200) NOT NULL,
	method VARCHAR(10) NOT NULL,
	request_header_accept VARCHAR(200),
	request_header_user_agent VARCHAR(200),
	response_status CHAR(3),
	response_header_content_type VARCHAR(100),
	response_header_content_length INT,
	time_taken INT NOT NULL
);

CREATE SEQUENCE formation.trace_id_seq start 1 increment 1;