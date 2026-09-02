create table parsed_resume (
    id uuid primary key default gen_random_uuid(),
    profile_id uuid not null references profile(id) on delete cascade,
    raw_text text not null,
    skills text[] not null default '{}',
    years_experience numeric(4, 1),
    tech_stack text[] not null default '{}',
    past_titles text[] not null default '{}',
    updated_at timestamptz not null default now(),
    constraint parsed_resume_profile_unique unique (profile_id)
);
