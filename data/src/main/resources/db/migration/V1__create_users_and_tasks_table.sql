SET TIME ZONE 'UTC';

CREATE TABLE users (
    id uuid NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp NOT NULL,
    name varchar(255) NOT NULL UNIQUE CHECK (name <> '')
);

CREATE TABLE tasks (
    id uuid NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    title varchar(255) NOT NULL,
    description text NOT NULL,
    status varchar(255) NOT NULL,
    assignee_id uuid REFERENCES users(id) ON DELETE SET NULL
);
