-- Drop table

-- DROP TABLE oauth.scopes;

CREATE TABLE oauth.scopes (
	id bigserial NOT NULL,
	"name" varchar(255) NULL,
	description varchar(255) NULL,
	CONSTRAINT scopes_pkey PRIMARY KEY (id)
);


-- Drop table

-- DROP TABLE oauth.clients;

CREATE TABLE oauth.clients (
	client_id varchar(255) NOT NULL,
	public_key text NULL,
	client_secret varchar(255) NULL,
	client_name varchar(255) NULL,
	status varchar(255) NULL,
	"scope" varchar(255) NULL,
	created_at timestamp NULL,
	updated_at timestamp NULL,
	CONSTRAINT clients_pkey PRIMARY KEY (client_id)
);


-- Drop table

-- DROP TABLE oauth.users;

CREATE TABLE oauth.users (
	user_id varchar(255) NOT NULL,
	user_name varchar(255) NULL,
	password_hash varchar(255) NULL,
	email varchar(255) NULL,
	status varchar(255) NULL,
	failed_attempts int4 NULL,
	locked_until timestamp NULL,
	last_login timestamp NULL,
	created_at timestamp NULL,
	updated_at timestamp NULL,
	CONSTRAINT users_pkey PRIMARY KEY (user_id)
);


-- Drop table

-- DROP TABLE oauth.user_client_scopes;

CREATE TABLE oauth.user_client_scopes (
	id bigserial NOT NULL,
	user_id varchar(255) NOT NULL,
	client_id varchar(255) NOT NULL,
	scope_id int8 NOT NULL,
	granted_at timestamp NULL,
	CONSTRAINT ukgfuani3ppf0sh2qppn83tmt7w UNIQUE (user_id, client_id, scope_id),
	CONSTRAINT user_client_scopes_pkey PRIMARY KEY (id),
	CONSTRAINT fk_ucs_client FOREIGN KEY (client_id) REFERENCES oauth.clients(client_id),
	CONSTRAINT fk_ucs_scope FOREIGN KEY (scope_id) REFERENCES oauth.scopes(id),
	CONSTRAINT fk_ucs_user FOREIGN KEY (user_id) REFERENCES oauth.users(user_id)
);
CREATE INDEX idx_ucs_client ON oauth.user_client_scopes USING btree (client_id);
CREATE INDEX idx_ucs_scope ON oauth.user_client_scopes USING btree (scope_id);
CREATE INDEX idx_ucs_user ON oauth.user_client_scopes USING btree (user_id);


-- Drop table

-- DROP TABLE oauth.authorization_codes;

CREATE TABLE oauth.authorization_codes (
	code varchar(100) NOT NULL,
	"scope" varchar(255) NULL,
	client_id varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	redirect_uri varchar(255) NOT NULL,
	created_at timestamp NOT NULL,
	expires_at timestamp NOT NULL,
	used bool NOT NULL,
	CONSTRAINT authorization_codes_pkey PRIMARY KEY (code),
	CONSTRAINT fk_auth_code_client FOREIGN KEY (client_id) REFERENCES oauth.clients(client_id),
	CONSTRAINT fk_auth_code_user FOREIGN KEY (user_id) REFERENCES oauth.users(user_id)
);
CREATE INDEX idx_auth_code_client ON oauth.authorization_codes USING btree (client_id);
CREATE INDEX idx_auth_code_user ON oauth.authorization_codes USING btree (user_id);



-- Drop table

-- DROP TABLE oauth.refresh_tokens;

CREATE TABLE oauth.refresh_tokens (
	id bigserial NOT NULL,
	token_hash varchar(128) NOT NULL,
	user_id varchar(255) NOT NULL,
	client_id varchar(100) NOT NULL,
	expiry_date timestamp NOT NULL,
	"scope" varchar(255) NOT NULL,
	CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id),
	CONSTRAINT refresh_tokens_token_hash_key UNIQUE (token_hash),
	CONSTRAINT fk_refresh_client FOREIGN KEY (client_id) REFERENCES oauth.clients(client_id),
	CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES oauth.users(user_id)
);
CREATE INDEX idx_refresh_client ON oauth.refresh_tokens USING btree (client_id);
CREATE INDEX idx_refresh_user ON oauth.refresh_tokens USING btree (user_id);
