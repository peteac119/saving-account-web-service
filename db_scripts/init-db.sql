-- Script to generate tables.
create table users (
	ID bigint GENERATED ALWAYS AS identity PRIMARY KEY,
	THAI_NAME  VARCHAR(100) not NULL,
	ENGLISH_NAME VARCHAR(100) not NULL,
	EMAIL  VARCHAR(100) unique not null,
	password VARCHAR(100) not null,
	CITIZEN_ID VARCHAR(13) unique not null,
	PIN_NUM  VARCHAR(100),
	role    VARCHAR(50) not null,
	CREATION_DATE  TIMESTAMP not null,
	LAST_UPDATE_DATE TIMESTAMP not null
);

create table saving_accounts (
	ID bigint GENERATED ALWAYS AS identity PRIMARY KEY,
	ACCOUNT_NUMBER	VARCHAR(7) unique not null,
	BALANCE	numeric(18,2) not null,
	CREATION_DATE  TIMESTAMP not null,
	LAST_UPDATE_DATE TIMESTAMP not null

	USER_ID  bigint references users (id)
);

create table transaction_audit_log (
	ID bigint GENERATED ALWAYS AS identity PRIMARY KEY,
	TRANSACTION_DATE  	DATE not null,
	TRANSACTION_TIME  	TIME not null,
	CODE 			  	VARCHAR(2) not null,
	CHANNEL 		  	VARCHAR(3) not null,
	TRANSACTION_AMOUNT	numeric(18,2) not null,
	BALANCE				numeric(18,2) not null,
	REMARKS  		  	VARCHAR(1000),
	ACCOUNT_ID	bigint 	references saving_accounts (id)
);

CREATE SEQUENCE account_num_seq
START WITH 1000000
INCREMENT BY 1
MAXVALUE 9999999;

-- Create teller for testing
INSERT INTO users
(thai_name, english_name, email, "password", citizen_id, pin_num, "role", creation_date, created_by, last_update_date, updated_by)
VALUES('เทลเลอร์ ยินดีให้บริการ', 'Teller Ishere', 'teller.ishere@somemail.com', '$2a$12$hgJrlTqGI/.52RSrQw7a8.lBd6BslxE36keA0bdRF74q578NQXyvO', '1112225534587', '$2a$12$qKF6AELU.dA0Tk4UYpfYi.zZJtZInjBhoi9raUOxZM6.upE8xIzCe', '[TELLER]', now(), NULL, now(), NULL);

commit;