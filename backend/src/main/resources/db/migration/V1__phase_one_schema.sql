create extension if not exists pgcrypto;

create table profile (
    id uuid primary key default gen_random_uuid(),
    name varchar(200) not null,
    email varchar(320) not null,
    target_roles text[] not null default '{}',
    locations text[] not null default '{}',
    remote_pref varchar(50),
    salary_min numeric(12, 2),
    salary_max numeric(12, 2),
    seniority varchar(80)
);

create table jobs (
    id uuid primary key default gen_random_uuid(),
    source varchar(20) not null check (source in ('adzuna', 'remoteok')),
    external_id varchar(255) not null,
    title varchar(300) not null,
    company varchar(300) not null,
    description text,
    url text not null,
    location varchar(300),
    salary_range varchar(200),
    posted_at timestamptz,
    fetched_at timestamptz not null default now(),
    constraint jobs_source_external_id_unique unique (source, external_id)
);

create index jobs_posted_at_idx on jobs (posted_at desc);

create table applications (
    id uuid primary key default gen_random_uuid(),
    job_id uuid not null references jobs(id) on delete cascade,
    status varchar(20) not null default 'saved'
        check (status in ('saved', 'applied', 'interview', 'offer', 'rejected')),
    applied_at timestamptz,
    notes text,
    cover_letter_text text,
    resume_version varchar(255),
    constraint applications_job_unique unique (job_id)
);

create index applications_status_idx on applications (status);
