--
-- CzechIdM 9.0 Flyway script
-- BCV solutions s.r.o.
--
-- This SQL script creates the required audit tables by CzechIdM (module vs)

CREATE TABLE vs_account_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime,
	created_m bit,
	creator varchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier varchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator varchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier varchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	connector_key varchar(255),
	connector_key_m bit,
	enable bit,
	enable_m bit,
	system_id binary(255),
	system_id_m bit,
	uid varchar(255),
	uid_m bit,
	CONSTRAINT vs_account_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_vs_acc_a_idm_audit FOREIGN KEY (rev) REFERENCES idm_audit(id)
);



CREATE TABLE vs_account_form_value_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime,
	created_m bit,
	creator varchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier varchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator varchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier varchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	boolean_value bit,
	boolean_value_m bit,
	byte_value varbinary(255),
	byte_value_m bit,
	confidential bit,
	confidential_m bit,
	date_value datetime,
	date_value_m bit,
	double_value numeric(38,4),
	double_value_m bit,
	long_value numeric(19,0),
	long_value_m bit,
	persistent_type varchar(45),
	persistent_type_m bit,
	seq smallint,
	seq_m bit,
	short_text_value varchar(2000),
	short_text_value_m bit,
	string_value text,
	string_value_m bit,
	uuid_value binary(16),
	uuid_value_m bit,
	attribute_id binary(16),
	form_attribute_m bit,
	owner_id binary(16),
	owner_m bit,
	CONSTRAINT vs_account_form_value_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_vs_acc_fvalue_a_audit FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE vs_request_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime,
	created_m bit,
	creator varchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier varchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator varchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier varchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	connector_conf image,
	configuration_m bit,
	connector_key varchar(255),
	connector_key_m bit,
	connector_object image,
	connector_object_m bit,
	duplicate_to_request_id binary(255),
	duplicate_to_request_m bit,
	execute_immediately bit,
	execute_immediately_m bit,
	operation_type varchar(255),
	operation_type_m bit,
	reason varchar(255),
	reason_m bit,
	state varchar(255),
	state_m bit,
	uid varchar(255),
	uid_m bit,
	previous_request_id binary(16),
	previous_request_m bit,
	system_id binary(16),
	system_m bit,
	CONSTRAINT vs_request_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_request_a_idm_audit FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE vs_system_implementer_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime,
	created_m bit,
	creator varchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier varchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator varchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier varchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	identity_id binary(16),
	identity_m bit,
	role_id binary(16),
	role_m bit,
	system_id binary(16),
	system_m bit,
	CONSTRAINT vs_system_implementer_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_ar40qritwofj5acof4vk9l6bq FOREIGN KEY (rev) REFERENCES idm_audit(id)
);