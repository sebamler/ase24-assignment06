CREATE TABLE events (
    id uuid NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    type varchar(255) not null,
    entity varchar(255) not null,
    entity_version bigint not null,
    created_by uuid default null,
    created_at timestamp not null,
    body jsonb not null
);

CREATE INDEX ON events ((body ->> 'id'));

-- TASK insert function
create or replace function fn_task_insert(body jsonb) returns void
    security definer --  indicates that the function is to be executed with the privileges of the user that calls it
    language plpgsql
as
$$
begin
    insert into tasks(id, title, description, status, assignee_id, created_at, updated_at)
    values (cast(body ->> 'id' as uuid),
            cast(body ->> 'title' as varchar(255)),
            cast(body ->> 'description' as text),
            cast(body ->> 'status' as varchar(255)),
            cast(body ->> 'assigneeId' as uuid),
            cast(body ->> 'createdAt' as timestamp),
            cast(body ->> 'updatedAt' as timestamp));
end;
$$;

-- TASK insert trigger
create or replace function fn_task_insert_trigger() returns trigger
    security definer
    language plpgsql
as
$$
begin
    perform fn_task_insert(new.body);
    return new;
end;
$$;

-- register TASK insert trigger
create trigger events_insert_task
    after insert
    on events
    for each row
    when (new.type = 'INSERT' and new.entity = 'Task')
execute procedure fn_task_insert_trigger();

-- TASK update function
create or replace function fn_task_update(body jsonb) returns void
    security definer
    language plpgsql
as
$$
begin
    update tasks
    set title = cast(body ->> 'title' as varchar(255)),
        description = cast(body ->> 'description' as text),
        status = cast(body ->> 'status' as varchar(255)),
        assignee_id = cast(body ->> 'assigneeId' as uuid),
        updated_at = cast(body ->> 'updatedAt' as timestamp)
    where id = cast(body ->> 'id' as uuid);
end;
$$;

-- TASK update trigger
create or replace function fn_task_update_trigger() returns trigger
    security definer
    language plpgsql
as
$$
begin
    perform fn_task_update(new.body);
    return new;
end;
$$;

-- register TASK update trigger
create trigger events_update_task
    after insert
    on events
    for each row
    when (new.type = 'UPDATE' and new.entity = 'Task')
execute procedure fn_task_update_trigger();

-- TASK delete function
create or replace function fn_task_delete(body jsonb) returns void
    security definer
    language plpgsql
as
$$
begin
    if body ->> 'id' is not null then
        delete from tasks where id = cast(body ->> 'id' as uuid);
    end if;
end;
$$;

-- TASK delete trigger
create or replace function fn_task_delete_trigger() returns trigger
    security definer
    language plpgsql
as
$$
begin
    perform fn_task_delete(new.body);
    return new;
end;
$$;

-- register TASK delete trigger
create trigger events_delete_task
    after insert
    on events
    for each row
    when (new.type = 'DELETE' and new.entity = 'Task')
execute procedure fn_task_delete_trigger();

-- USER insert function
create or replace function fn_user_insert(body jsonb) returns void
    security definer
    language plpgsql
as
$$
begin
    insert into users(id, created_at, name)
    values (cast(body ->> 'id' as uuid),
            cast(body ->> 'createdAt' as timestamp),
            cast(body ->> 'name' as varchar(255)));
end;
$$;

-- USER insert trigger
create or replace function fn_user_insert_trigger() returns trigger
    security definer
    language plpgsql
as
$$
begin
    perform fn_user_insert(new.body);
    return new;
end;
$$;

-- register USER insert trigger
create trigger events_insert_user
    after insert
    on events
    for each row
    when (new.type = 'INSERT' and new.entity = 'User')
execute procedure fn_user_insert_trigger();

-- USER update function
create or replace function fn_user_update(body jsonb) returns void
    security definer
    language plpgsql
as
$$
begin
    update users
    set name = cast(body ->> 'name' as text)
    where id = cast(body ->> 'id' as uuid);
end;
$$;

-- USER update trigger
create or replace function fn_user_update_trigger() returns trigger
    security definer
    language plpgsql
as
$$
begin
    perform fn_user_update(new.body);
    return new;
end;
$$;

-- register USER update trigger
create trigger events_update_user
    after insert
    on events
    for each row
    when (new.type = 'UPDATE' and new.entity = 'User')
execute procedure fn_user_update_trigger();

-- USER delete function
create or replace function fn_user_delete(body jsonb) returns void
    security definer
    language plpgsql
as
$$
begin
    if body ->> 'id' is not null then
        delete from users where id = cast(body ->> 'id' as uuid);
    end if;
end;
$$;

-- USER delete trigger
create or replace function fn_user_delete_trigger() returns trigger
    security definer
    language plpgsql
as
$$
begin
    perform fn_user_delete(new.body);
    return new;
end;
$$;

-- register USER delete trigger
create trigger events_delete_user
    after insert
    on events
    for each row
    when (new.type = 'DELETE' and new.entity = 'User')
execute procedure fn_user_delete_trigger();
