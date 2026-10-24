-- Row Level Security: Jede Person sieht nur Daten der Haushalte, in denen sie Mitglied ist.
-- Haushalte und Mitgliedschaften entstehen ausschließlich über die RPC-Funktionen unten.

-- Hilfsfunktionen (security definer, damit die Policies nicht rekursiv werden) ----

create function public.is_household_member(p_household_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from public.household_members
        where household_id = p_household_id and user_id = (select auth.uid())
    );
$$;

create function public.is_member_of(p_household_id uuid, p_user_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from public.household_members
        where household_id = p_household_id and user_id = p_user_id
    );
$$;

create function public.shares_household_with(p_user_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
        from public.household_members mine
        join public.household_members theirs on theirs.household_id = mine.household_id
        where mine.user_id = (select auth.uid()) and theirs.user_id = p_user_id
    );
$$;

-- RLS aktivieren --------------------------------------------------------------

alter table public.profiles enable row level security;
alter table public.households enable row level security;
alter table public.household_members enable row level security;
alter table public.member_incomes enable row level security;
alter table public.household_invites enable row level security;
alter table public.categories enable row level security;
alter table public.category_rules enable row level security;
alter table public.recurring_rules enable row level security;
alter table public.expenses enable row level security;
alter table public.settlements enable row level security;
alter table public.budgets enable row level security;
alter table public.savings_goals enable row level security;
alter table public.goal_contributions enable row level security;

-- Profile
create policy "Profile: eigenes und Partner lesen" on public.profiles
    for select to authenticated
    using (id = (select auth.uid()) or public.shares_household_with(id));

create policy "Profile: eigenes bearbeiten" on public.profiles
    for update to authenticated
    using (id = (select auth.uid()))
    with check (id = (select auth.uid()));

-- Haushalte
create policy "Haushalt: Mitglieder lesen" on public.households
    for select to authenticated
    using (public.is_household_member(id));

create policy "Haushalt: Mitglieder bearbeiten" on public.households
    for update to authenticated
    using (public.is_household_member(id))
    with check (public.is_household_member(id));

-- Mitglieder
create policy "Mitglieder: Mitglieder lesen" on public.household_members
    for select to authenticated
    using (public.is_household_member(household_id));

create policy "Mitglieder: Mitglieder bearbeiten" on public.household_members
    for update to authenticated
    using (public.is_household_member(household_id))
    with check (public.is_household_member(household_id));

create policy "Mitglieder: selbst austreten" on public.household_members
    for delete to authenticated
    using (user_id = (select auth.uid()));

-- Einkommen: beide sehen beide (nötig für die faire Aufteilung), ändern nur das eigene.
create policy "Einkommen: Mitglieder lesen" on public.member_incomes
    for select to authenticated
    using (public.is_household_member(household_id));

create policy "Einkommen: eigenes anlegen" on public.member_incomes
    for insert to authenticated
    with check (user_id = (select auth.uid()) and public.is_household_member(household_id));

create policy "Einkommen: eigenes ändern" on public.member_incomes
    for update to authenticated
    using (user_id = (select auth.uid()))
    with check (user_id = (select auth.uid()) and public.is_household_member(household_id));

create policy "Einkommen: eigenes löschen" on public.member_incomes
    for delete to authenticated
    using (user_id = (select auth.uid()));

-- Einladungen
create policy "Einladungen: Mitglieder lesen" on public.household_invites
    for select to authenticated
    using (public.is_household_member(household_id));

-- Haushaltsdaten: volle Rechte für Mitglieder
do $$
declare
    t text;
begin
    foreach t in array array['categories', 'category_rules', 'recurring_rules', 'budgets', 'savings_goals', 'goal_contributions'] loop
        execute format(
            'create policy "Haushaltsdaten: Mitglieder" on public.%I for all to authenticated '
            'using (public.is_household_member(household_id)) '
            'with check (public.is_household_member(household_id))',
            t
        );
    end loop;
end;
$$;

-- Wiederkehrende Buchungen dürfen nur Mitglieder als Zahler eintragen.
create policy "Daueraufträge: Zahler ist Mitglied" on public.recurring_rules
    as restrictive for all to authenticated
    using (true)
    with check (paid_by is null or public.is_member_of(household_id, paid_by));

-- Ausgaben: private Ausgaben sieht nur, wer sie bezahlt hat.
create policy "Ausgaben: lesen" on public.expenses
    for select to authenticated
    using (
        public.is_household_member(household_id)
        and (not is_private or paid_by = (select auth.uid()))
    );

create policy "Ausgaben: anlegen" on public.expenses
    for insert to authenticated
    with check (
        public.is_household_member(household_id)
        and (paid_by is null or public.is_member_of(household_id, paid_by))
        and (payment_source = 'JOINT_ACCOUNT' or paid_by is not null)
        and (not is_private or paid_by = (select auth.uid()))
    );

create policy "Ausgaben: ändern" on public.expenses
    for update to authenticated
    using (
        public.is_household_member(household_id)
        and (not is_private or paid_by = (select auth.uid()))
    )
    with check (
        public.is_household_member(household_id)
        and (paid_by is null or public.is_member_of(household_id, paid_by))
        and (payment_source = 'JOINT_ACCOUNT' or paid_by is not null)
        and (not is_private or paid_by = (select auth.uid()))
    );

create policy "Ausgaben: löschen" on public.expenses
    for delete to authenticated
    using (
        public.is_household_member(household_id)
        and (not is_private or paid_by = (select auth.uid()))
    );

-- Ausgleichszahlungen: nur zwischen Mitgliedern des Haushalts.
create policy "Ausgleich: lesen" on public.settlements
    for select to authenticated
    using (public.is_household_member(household_id));

create policy "Ausgleich: anlegen und ändern" on public.settlements
    for all to authenticated
    using (public.is_household_member(household_id))
    with check (
        public.is_household_member(household_id)
        and public.is_member_of(household_id, from_user)
        and public.is_member_of(household_id, to_user)
    );

-- RPC: Haushalt anlegen ---------------------------------------------------------

create function public.create_household(
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
        raise exception 'Nicht angemeldet' using errcode = '42501';
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

-- RPC: Partner einladen -------------------------------------------------------

create function public.create_household_invite(p_household_id uuid)
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
        raise exception 'Kein Mitglied dieses Haushalts' using errcode = '42501';
    end if;
    if (select count(*) from public.household_members where household_id = p_household_id) >= 2 then
        raise exception 'Der Haushalt ist bereits vollständig' using errcode = 'P0001';
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

create function public.accept_household_invite(p_code text)
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
        raise exception 'Nicht angemeldet' using errcode = '42501';
    end if;

    select * into v_invite
    from public.household_invites
    where code = upper(trim(p_code))
    for update;

    if not found or v_invite.accepted_at is not null or v_invite.expires_at <= now() then
        raise exception 'Einladungscode ungültig oder abgelaufen' using errcode = 'P0002';
    end if;
    if public.is_member_of(v_invite.household_id, v_user) then
        raise exception 'Du bist bereits Mitglied dieses Haushalts' using errcode = 'P0001';
    end if;

    insert into public.household_members (household_id, user_id, role)
    values (v_invite.household_id, v_user, 'MEMBER');

    update public.household_invites
    set accepted_by = v_user, accepted_at = now()
    where code = v_invite.code;

    return v_invite.household_id;
end;
$$;

-- RPC: Summen privater Ausgaben (Partner sieht nur die Summe, keine Details) ----

create function public.private_spending_totals(p_household_id uuid, p_from date, p_to date)
returns table (user_id uuid, total_cents bigint)
language plpgsql
stable
security definer
set search_path = ''
as $$
begin
    if not public.is_household_member(p_household_id) then
        raise exception 'Kein Mitglied dieses Haushalts' using errcode = '42501';
    end if;

    return query
    select e.paid_by, sum(e.amount_cents)::bigint
    from public.expenses e
    where e.household_id = p_household_id
      and e.is_private
      and e.deleted_at is null
      and e.spent_on between p_from and p_to
    group by e.paid_by;
end;
$$;

-- Ausführungsrechte -----------------------------------------------------------

revoke execute on function
    public.create_household(text, text, public.split_mode),
    public.create_household_invite(uuid),
    public.accept_household_invite(text),
    public.private_spending_totals(uuid, date, date)
from public, anon;

grant execute on function
    public.create_household(text, text, public.split_mode),
    public.create_household_invite(uuid),
    public.accept_household_invite(text),
    public.private_spending_totals(uuid, date, date)
to authenticated;

-- Spaltenrechte: Schlüssel- und Zuordnungsspalten dürfen nicht direkt geändert werden.
revoke update on public.households from anon, authenticated;
grant update (name, currency, default_split_mode, fairness_basis, joint_account_enabled)
    on public.households to authenticated;

revoke update on public.household_members from anon, authenticated;
grant update (fixed_share_bps) on public.household_members to authenticated;

revoke insert, update, delete on public.household_invites from anon, authenticated;
revoke insert, delete on public.profiles, public.households from anon, authenticated;
revoke insert on public.household_members from anon, authenticated;

-- Echtzeit-Updates für den Partner --------------------------------------------

alter publication supabase_realtime add table
    public.households,
    public.household_members,
    public.member_incomes,
    public.categories,
    public.expenses,
    public.settlements,
    public.budgets,
    public.savings_goals,
    public.goal_contributions;
