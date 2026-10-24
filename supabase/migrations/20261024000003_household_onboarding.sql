-- Onboarding: ein Haushalt pro Person, tolerante Einladungscodes, Vorschau vor dem Beitritt.
--
-- Fehler der RPCs tragen einen maschinenlesbaren Hinweis (HINT), den die App auswertet:
--   invite_invalid     Code unbekannt, benutzt oder abgelaufen
--   already_member     Person gehört schon zu einem Haushalt
--   household_full     Haushalt hat bereits zwei Mitglieder
--   not_member         Person gehört nicht zum Haushalt
--   not_authenticated  nicht angemeldet

-- Ein Haushalt pro Person ---------------------------------------------------------

drop index public.household_members_user_idx;
alter table public.household_members
    add constraint household_members_one_household_per_user unique (user_id);

create or replace function public.enforce_max_two_members()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    perform 1 from public.households where id = new.household_id for update;
    if (select count(*) from public.household_members where household_id = new.household_id) >= 2 then
        raise exception 'Ein Haushalt kann höchstens zwei Mitglieder haben'
            using errcode = 'P0001', hint = 'household_full';
    end if;
    return new;
end;
$$;

-- Einladungscodes: Groß-/Kleinschreibung, Bindestriche und Leerzeichen egal ---------

create function public.normalize_invite_code(p_code text)
returns text
language sql
immutable
set search_path = ''
as $$
    select regexp_replace(upper(coalesce(p_code, '')), '[^A-Z0-9]', '', 'g');
$$;

-- RPCs neu fassen ---------------------------------------------------------------

create or replace function public.create_household(
    p_name text,
    p_currency text default 'EUR',
    p_split_mode public.split_mode default 'INCOME'
)
returns public.households
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user uuid := auth.uid();
    v_household public.households;
begin
    if v_user is null then
        raise exception 'Nicht angemeldet' using errcode = '42501', hint = 'not_authenticated';
    end if;
    if exists (select 1 from public.household_members where user_id = v_user) then
        raise exception 'Du gehörst bereits zu einem Haushalt' using errcode = 'P0001', hint = 'already_member';
    end if;

    insert into public.households (name, currency, default_split_mode, created_by)
    values (trim(p_name), upper(p_currency), p_split_mode, v_user)
    returning * into v_household;

    insert into public.household_members (household_id, user_id, role)
    values (v_household.id, v_user, 'OWNER');

    insert into public.categories (household_id, name, icon, color, sort_order)
    values
        (v_household.id, 'Lebensmittel', 'shopping_cart',  '#2A9D8F', 10),
        (v_household.id, 'Wohnen',       'home',           '#4F5BD5', 20),
        (v_household.id, 'Haushalt',     'cleaning',       '#8AB17D', 30),
        (v_household.id, 'Mobilität',    'directions_car', '#E9C46A', 40),
        (v_household.id, 'Restaurant',   'restaurant',     '#F4A261', 50),
        (v_household.id, 'Freizeit',     'celebration',    '#E76F51', 60),
        (v_household.id, 'Abos',         'subscriptions',  '#9B5DE5', 70),
        (v_household.id, 'Gesundheit',   'favorite',       '#F15BB5', 80),
        (v_household.id, 'Urlaub',       'flight',         '#00BBF9', 90),
        (v_household.id, 'Sonstiges',    'category',       '#8A8FA3', 100);

    return v_household;
end;
$$;

create or replace function public.create_household_invite(p_household_id uuid)
returns text
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user uuid := auth.uid();
    v_code text;
begin
    if not public.is_household_member(p_household_id) then
        raise exception 'Kein Mitglied dieses Haushalts' using errcode = '42501', hint = 'not_member';
    end if;
    if (select count(*) from public.household_members where household_id = p_household_id) >= 2 then
        raise exception 'Der Haushalt ist bereits vollständig' using errcode = 'P0001', hint = 'household_full';
    end if;

    -- Offene Einladungen ungültig machen, damit immer nur ein Code aktiv ist.
    update public.household_invites
    set expires_at = now()
    where household_id = p_household_id and accepted_at is null and expires_at > now();

    v_code := upper(substr(replace(gen_random_uuid()::text, '-', ''), 1, 8));

    insert into public.household_invites (code, household_id, created_by)
    values (v_code, p_household_id, v_user);

    return v_code;
end;
$$;

create or replace function public.accept_household_invite(p_code text)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user uuid := auth.uid();
    v_invite public.household_invites;
begin
    if v_user is null then
        raise exception 'Nicht angemeldet' using errcode = '42501', hint = 'not_authenticated';
    end if;

    select * into v_invite
    from public.household_invites
    where code = public.normalize_invite_code(p_code)
    for update;

    if not found or v_invite.accepted_at is not null or v_invite.expires_at <= now() then
        raise exception 'Einladungscode ungültig oder abgelaufen' using errcode = 'P0002', hint = 'invite_invalid';
    end if;
    if exists (select 1 from public.household_members where user_id = v_user) then
        raise exception 'Du gehörst bereits zu einem Haushalt' using errcode = 'P0001', hint = 'already_member';
    end if;

    insert into public.household_members (household_id, user_id, role)
    values (v_invite.household_id, v_user, 'MEMBER');

    -- Fester Anteil startet bei 50/50, egal was vorher eingestellt war.
    update public.household_members
    set fixed_share_bps = 5000
    where household_id = v_invite.household_id;

    update public.household_invites
    set accepted_by = v_user, accepted_at = now()
    where code = v_invite.code;

    return v_invite.household_id;
end;
$$;

-- Vorschau: "Anna lädt dich zu 'Anna & Ben' ein" --------------------------------

create function public.preview_household_invite(p_code text)
returns table (household_name text, inviter_name text)
language plpgsql
stable
security definer
set search_path = ''
as $$
begin
    if auth.uid() is null then
        raise exception 'Nicht angemeldet' using errcode = '42501', hint = 'not_authenticated';
    end if;

    return query
    select h.name, coalesce(p.display_name, '')
    from public.household_invites i
    join public.households h on h.id = i.household_id
    left join public.profiles p on p.id = i.created_by
    where i.code = public.normalize_invite_code(p_code)
      and i.accepted_at is null
      and i.expires_at > now();

    if not found then
        raise exception 'Einladungscode ungültig oder abgelaufen' using errcode = 'P0002', hint = 'invite_invalid';
    end if;
end;
$$;

revoke execute on function public.preview_household_invite(text) from public, anon;
grant execute on function public.preview_household_invite(text) to authenticated;
