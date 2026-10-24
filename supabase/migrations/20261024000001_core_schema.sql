-- Kernschema für Paarfinanz: Haushalte, Mitglieder, Einkommen, Ausgaben, Budgets, Ziele.
-- Alle Beträge in Cent (bigint), Anteile in Basispunkten (10000 = 100 %).

create type public.split_mode as enum ('EQUAL', 'INCOME', 'FIXED', 'PERSONAL');
create type public.fairness_basis as enum ('NET', 'DISPOSABLE');
create type public.payment_source as enum ('PERSON', 'JOINT_ACCOUNT');
create type public.expense_origin as enum ('MANUAL', 'CSV', 'OCR', 'RECURRING');
create type public.recurrence_frequency as enum ('WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY');
create type public.member_role as enum ('OWNER', 'MEMBER');

create function public.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    new.updated_at := now();
    return new;
end;
$$;

-- Profile ---------------------------------------------------------------------

create table public.profiles (
    id           uuid primary key references auth.users (id) on delete cascade,
    display_name text not null default '' check (char_length(display_name) <= 60),
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now()
);

create function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.profiles (id, display_name)
    values (
        new.id,
        left(coalesce(nullif(new.raw_user_meta_data ->> 'display_name', ''), split_part(new.email, '@', 1), ''), 60)
    );
    return new;
end;
$$;

create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- Haushalte -------------------------------------------------------------------

create table public.households (
    id                    uuid primary key default gen_random_uuid(),
    name                  text not null check (char_length(name) between 1 and 60),
    currency              char(3) not null default 'EUR',
    default_split_mode    public.split_mode not null default 'INCOME' check (default_split_mode <> 'PERSONAL'),
    fairness_basis        public.fairness_basis not null default 'NET',
    joint_account_enabled boolean not null default false,
    created_by            uuid references auth.users (id) on delete set null,
    created_at            timestamptz not null default now(),
    updated_at            timestamptz not null default now()
);

create table public.household_members (
    household_id    uuid not null references public.households (id) on delete cascade,
    user_id         uuid not null references public.profiles (id) on delete cascade,
    role            public.member_role not null default 'MEMBER',
    -- Anteil dieser Person bei der Aufteilung "FIXED"; beide Mitglieder ergeben zusammen 10000.
    fixed_share_bps integer not null default 5000 check (fixed_share_bps between 0 and 10000),
    joined_at       timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    primary key (household_id, user_id)
);

create index household_members_user_idx on public.household_members (user_id);

-- Ein Haushalt besteht aus höchstens zwei Personen.
create function public.enforce_max_two_members()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    perform 1 from public.households where id = new.household_id for update;
    if (select count(*) from public.household_members where household_id = new.household_id) >= 2 then
        raise exception 'Ein Haushalt kann höchstens zwei Mitglieder haben' using errcode = 'P0001';
    end if;
    return new;
end;
$$;

create trigger household_members_max_two
    before insert on public.household_members
    for each row execute function public.enforce_max_two_members();

create table public.member_incomes (
    id                        uuid primary key default gen_random_uuid(),
    household_id              uuid not null,
    user_id                   uuid not null,
    net_income_cents          bigint not null check (net_income_cents >= 0),
    private_fixed_costs_cents bigint not null default 0 check (private_fixed_costs_cents >= 0),
    valid_from                date not null,
    created_at                timestamptz not null default now(),
    updated_at                timestamptz not null default now(),
    foreign key (household_id, user_id) references public.household_members (household_id, user_id) on delete cascade,
    unique (household_id, user_id, valid_from)
);

create table public.household_invites (
    code         text primary key,
    household_id uuid not null references public.households (id) on delete cascade,
    created_by   uuid not null references auth.users (id) on delete cascade,
    created_at   timestamptz not null default now(),
    expires_at   timestamptz not null default now() + interval '7 days',
    accepted_by  uuid references auth.users (id) on delete set null,
    accepted_at  timestamptz
);

-- Kategorien ------------------------------------------------------------------

create table public.categories (
    id           uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households (id) on delete cascade,
    name         text not null check (char_length(name) between 1 and 40),
    icon         text not null default 'category',
    color        text not null default '#8A8FA3' check (color ~ '^#[0-9A-Fa-f]{6}$'),
    -- Optional abweichende Aufteilung für alle Ausgaben dieser Kategorie.
    split_mode   public.split_mode,
    sort_order   integer not null default 0,
    archived     boolean not null default false,
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    unique (id, household_id)
);

create index categories_household_idx on public.categories (household_id);

create table public.category_rules (
    id           uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households (id) on delete cascade,
    pattern      text not null check (char_length(pattern) between 1 and 100),
    category_id  uuid not null,
    priority     integer not null default 0,
    created_at   timestamptz not null default now(),
    foreign key (category_id, household_id) references public.categories (id, household_id) on delete cascade
);

-- Wiederkehrende Buchungen ----------------------------------------------------

create table public.recurring_rules (
    id              uuid primary key default gen_random_uuid(),
    household_id    uuid not null references public.households (id) on delete cascade,
    paid_by         uuid references public.profiles (id) on delete set null,
    payment_source  public.payment_source not null default 'PERSON',
    amount_cents    bigint not null check (amount_cents > 0),
    category_id     uuid,
    split_mode      public.split_mode not null,
    -- null = Anteil wird bei jeder Ausführung neu berechnet (z. B. nach aktuellem Einkommen).
    payer_share_bps integer check (payer_share_bps between 0 and 10000),
    note            text not null default '' check (char_length(note) <= 200),
    frequency       public.recurrence_frequency not null default 'MONTHLY',
    interval_count  integer not null default 1 check (interval_count >= 1),
    starts_on       date not null,
    next_run_on     date not null,
    ends_on         date,
    active          boolean not null default true,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    foreign key (category_id, household_id) references public.categories (id, household_id) on delete set null (category_id),
    check (payment_source = 'PERSON' or paid_by is null)
);

-- Ausgaben --------------------------------------------------------------------

create table public.expenses (
    id                uuid primary key default gen_random_uuid(),
    household_id      uuid not null references public.households (id) on delete cascade,
    -- null, wenn vom Gemeinschaftskonto bezahlt.
    paid_by           uuid references public.profiles (id) on delete set null,
    payment_source    public.payment_source not null default 'PERSON',
    amount_cents      bigint not null check (amount_cents > 0),
    category_id       uuid,
    spent_on          date not null default current_date,
    note              text not null default '' check (char_length(note) <= 200),
    split_mode        public.split_mode not null,
    -- Anteil, den die zahlende Person selbst trägt. Wird beim Anlegen festgeschrieben,
    -- damit spätere Gehaltsänderungen alte Buchungen nicht verändern.
    payer_share_bps   integer not null check (payer_share_bps between 0 and 10000),
    is_private        boolean not null default false,
    origin            public.expense_origin not null default 'MANUAL',
    import_hash       text,
    recurring_rule_id uuid references public.recurring_rules (id) on delete set null,
    created_by        uuid references auth.users (id) on delete set null default auth.uid(),
    created_at        timestamptz not null default now(),
    updated_at        timestamptz not null default now(),
    deleted_at        timestamptz,
    foreign key (category_id, household_id) references public.categories (id, household_id) on delete set null (category_id),
    check (payment_source = 'PERSON' or paid_by is null),
    check (payment_source = 'PERSON' or not is_private),
    check (split_mode <> 'PERSONAL' or payer_share_bps = 10000),
    check (not is_private or split_mode = 'PERSONAL')
);

create index expenses_household_date_idx on public.expenses (household_id, spent_on desc);
create unique index expenses_import_hash_idx on public.expenses (household_id, import_hash) where import_hash is not null;

-- Ausgleichszahlungen zwischen den Partnern -----------------------------------

create table public.settlements (
    id           uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households (id) on delete cascade,
    from_user    uuid not null references public.profiles (id) on delete cascade,
    to_user      uuid not null references public.profiles (id) on delete cascade,
    amount_cents bigint not null check (amount_cents > 0),
    settled_on   date not null default current_date,
    note         text not null default '' check (char_length(note) <= 200),
    created_by   uuid references auth.users (id) on delete set null default auth.uid(),
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    deleted_at   timestamptz,
    check (from_user <> to_user)
);

create index settlements_household_idx on public.settlements (household_id, settled_on desc);

-- Budgets ---------------------------------------------------------------------

create table public.budgets (
    id           uuid primary key default gen_random_uuid(),
    household_id uuid not null,
    category_id  uuid not null,
    -- Erster Tag des Monats; null = Standardbudget für jeden Monat.
    month        date check (month is null or extract(day from month) = 1),
    limit_cents  bigint not null check (limit_cents > 0),
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    foreign key (category_id, household_id) references public.categories (id, household_id) on delete cascade,
    unique nulls not distinct (category_id, month)
);

-- Sparziele -------------------------------------------------------------------

create table public.savings_goals (
    id           uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households (id) on delete cascade,
    name         text not null check (char_length(name) between 1 and 60),
    target_cents bigint not null check (target_cents > 0),
    target_date  date,
    icon         text not null default 'savings',
    color        text not null default '#4F5BD5' check (color ~ '^#[0-9A-Fa-f]{6}$'),
    split_mode   public.split_mode not null default 'INCOME' check (split_mode <> 'PERSONAL'),
    archived_at  timestamptz,
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    unique (id, household_id)
);

create table public.goal_contributions (
    id             uuid primary key default gen_random_uuid(),
    goal_id        uuid not null,
    household_id   uuid not null,
    -- null = Einzahlung vom Gemeinschaftskonto.
    user_id        uuid references public.profiles (id) on delete set null,
    -- Negativ = Entnahme.
    amount_cents   bigint not null check (amount_cents <> 0),
    contributed_on date not null default current_date,
    note           text not null default '' check (char_length(note) <= 200),
    created_at     timestamptz not null default now(),
    foreign key (goal_id, household_id) references public.savings_goals (id, household_id) on delete cascade
);

-- updated_at-Trigger ----------------------------------------------------------

do $$
declare
    t text;
begin
    foreach t in array array[
        'profiles', 'households', 'household_members', 'member_incomes', 'categories',
        'recurring_rules', 'expenses', 'settlements', 'budgets', 'savings_goals'
    ] loop
        execute format(
            'create trigger set_updated_at before update on public.%I for each row execute function public.set_updated_at()',
            t
        );
    end loop;
end;
$$;
