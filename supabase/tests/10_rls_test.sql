-- Sicherheitstests für Haushalte, Einladungen und Ausgaben.
-- Läuft nach 00_supabase_stub.sql und allen Migrationen (siehe run_tests.sh).
\set ON_ERROR_STOP on
\set anna '11111111-1111-1111-1111-111111111111'
\set ben  '22222222-2222-2222-2222-222222222222'
\set eve  '33333333-3333-3333-3333-333333333333'

create schema tests;
grant usage on schema tests to anon, authenticated;

create function tests.assert(p_ok boolean, p_description text)
returns void language plpgsql as $$
begin
    if p_ok is not true then
        raise exception 'FEHLGESCHLAGEN: %', p_description;
    end if;
    raise notice 'ok: %', p_description;
end;
$$;

create function tests.expect_error(p_sql text, p_description text)
returns void language plpgsql as $$
begin
    begin
        execute p_sql;
    exception when others then
        raise notice 'ok: % (%)', p_description, sqlerrm;
        return;
    end;
    raise exception 'FEHLGESCHLAGEN: erwarteter Fehler blieb aus: %', p_description;
end;
$$;

create function tests.login(p_user uuid)
returns void language sql as $$
    select set_config('request.jwt.claim.sub', p_user::text, false);
$$;

insert into auth.users (id, email, raw_user_meta_data) values
    (:'anna', 'anna@example.com', '{"display_name": "Anna"}'),
    (:'ben',  'ben@example.com',  '{}'),
    (:'eve',  'eve@example.com',  '{"display_name": "Eve"}');

select tests.assert((select display_name from public.profiles where id = :'anna') = 'Anna', 'Profil mit Anzeigename angelegt');
select tests.assert((select display_name from public.profiles where id = :'ben') = 'ben', 'Profil ohne Namen nutzt E-Mail-Präfix');

-- Anna legt einen Haushalt an und lädt Ben ein --------------------------------
select tests.login(:'anna');
set role authenticated;

select id as hh from public.create_household('Anna & Ben') \gset
select tests.assert((select count(*) from public.categories where household_id = :'hh') = 10, 'Standardkategorien angelegt');
select tests.assert((select role from public.household_members where user_id = :'anna') = 'OWNER', 'Anna ist Besitzerin');
select tests.expect_error(
    format('insert into public.households (name) values (%L)', 'Direkt'),
    'Haushalte nur per RPC anlegbar'
);
select public.create_household_invite(:'hh') as code \gset
reset role;

-- Ben vor und nach dem Beitritt -----------------------------------------------
select tests.login(:'ben');
set role authenticated;

select tests.assert(not exists (select 1 from public.households where id = :'hh'), 'Ben sieht den Haushalt vor dem Beitritt nicht');
select tests.assert(public.accept_household_invite(lower(:'code')) = :'hh', 'Ben tritt per Code bei (Groß-/Kleinschreibung egal)');
select tests.assert(exists (select 1 from public.households where id = :'hh'), 'Ben sieht den Haushalt nach dem Beitritt');
select tests.assert((select count(*) from public.profiles) = 2, 'Ben sieht sein Profil und das von Anna');

insert into public.expenses (household_id, paid_by, amount_cents, split_mode, payer_share_bps, note)
values (:'hh', :'ben', 8500, 'INCOME', 4000, 'Wocheneinkauf');
insert into public.expenses (household_id, paid_by, amount_cents, split_mode, payer_share_bps, is_private, note)
values (:'hh', :'ben', 2000, 'PERSONAL', 10000, true, 'Geschenk für Anna');
insert into public.expenses (household_id, payment_source, amount_cents, split_mode, payer_share_bps)
values (:'hh', 'JOINT_ACCOUNT', 120000, 'INCOME', 5000);
reset role;

-- Anna: private Ausgaben von Ben nur als Summe ---------------------------------
select tests.login(:'anna');
set role authenticated;

select tests.assert((select count(*) from public.expenses where household_id = :'hh') = 2, 'Anna sieht Bens private Ausgabe nicht');
select tests.assert(
    (select total_cents from public.private_spending_totals(:'hh', '2000-01-01', '2100-01-01') where user_id = :'ben') = 2000,
    'Anna sieht die Summe von Bens privaten Ausgaben'
);
select tests.expect_error(
    format('insert into public.expenses (household_id, paid_by, amount_cents, split_mode, payer_share_bps, is_private) values (%L, %L, 100, %L, 10000, true)', :'hh', :'ben', 'PERSONAL'),
    'Private Ausgaben nur im eigenen Namen'
);
select tests.expect_error(
    format('insert into public.expenses (household_id, paid_by, amount_cents, split_mode, payer_share_bps) values (%L, %L, 100, %L, 5000)', :'hh', :'eve', 'EQUAL'),
    'Zahler muss Mitglied sein'
);
select tests.expect_error(
    format('insert into public.expenses (household_id, amount_cents, split_mode, payer_share_bps) values (%L, 100, %L, 5000)', :'hh', 'EQUAL'),
    'Persönliche Zahlung braucht einen Zahler'
);
select tests.expect_error(
    format('insert into public.expenses (household_id, paid_by, amount_cents, split_mode, payer_share_bps) values (%L, %L, 100, %L, 5000)', :'hh', :'anna', 'PERSONAL'),
    'PERSONAL heißt 100 % für den Zahler'
);

insert into public.member_incomes (household_id, user_id, net_income_cents, valid_from)
values (:'hh', :'anna', 300000, '2026-01-01');
select tests.expect_error(
    format('insert into public.member_incomes (household_id, user_id, net_income_cents, valid_from) values (%L, %L, 1, %L)', :'hh', :'ben', '2026-01-01'),
    'Einkommen nur für sich selbst eintragen'
);

update public.household_members set fixed_share_bps = 6000 where household_id = :'hh' and user_id = :'anna';
select tests.assert((select fixed_share_bps from public.household_members where user_id = :'anna') = 6000, 'Fester Anteil änderbar');
select tests.expect_error(
    format('update public.household_members set user_id = %L where user_id = %L', :'eve', :'ben'),
    'Mitgliedschaften können nicht umgeschrieben werden'
);
select tests.expect_error(
    format('update public.households set created_by = %L where id = %L', :'eve', :'hh'),
    'Haushalts-Besitz nicht änderbar'
);
reset role;

-- Eve ist fremd ---------------------------------------------------------------
select tests.login(:'eve');
set role authenticated;

select tests.assert((select count(*) from public.households) = 0, 'Eve sieht keine fremden Haushalte');
select tests.assert((select count(*) from public.expenses) = 0, 'Eve sieht keine fremden Ausgaben');
select tests.assert((select count(*) from public.categories) = 0, 'Eve sieht keine fremden Kategorien');
select tests.assert((select count(*) from public.member_incomes) = 0, 'Eve sieht keine fremden Einkommen');
select tests.assert((select count(*) from public.household_invites) = 0, 'Eve sieht keine fremden Einladungen');
select tests.assert((select count(*) from public.profiles) = 1, 'Eve sieht nur ihr eigenes Profil');
select tests.expect_error(format('select public.accept_household_invite(%L)', :'code'), 'Benutzter Code ist ungültig');
select tests.expect_error(format('select public.create_household_invite(%L)', :'hh'), 'Fremde können nicht einladen');
select tests.expect_error(format('select public.private_spending_totals(%L, %L, %L)', :'hh', '2000-01-01', '2100-01-01'), 'Fremde sehen keine Summen');
select tests.expect_error(
    format('insert into public.expenses (household_id, paid_by, amount_cents, split_mode, payer_share_bps) values (%L, %L, 100, %L, 5000)', :'hh', :'eve', 'EQUAL'),
    'Fremde können keine Ausgaben anlegen'
);

with updated as (update public.expenses set amount_cents = 1 where household_id = :'hh' returning 1)
select tests.assert((select count(*) from updated) = 0, 'Fremde können keine Ausgaben ändern');

select id as eve_hh from public.create_household('Eves WG') \gset
select tests.assert((select count(*) from public.households) = 1, 'Eve sieht nur ihren eigenen Haushalt');
reset role;

-- Haushalt ist voll -----------------------------------------------------------
select tests.login(:'anna');
set role authenticated;
select tests.expect_error(format('select public.create_household_invite(%L)', :'hh'), 'Kein dritter Partner möglich');
reset role;

-- Nicht angemeldet ------------------------------------------------------------
select set_config('request.jwt.claim.sub', '', false);
set role anon;
select tests.assert((select count(*) from public.households) = 0, 'Anonym sieht keine Haushalte');
select tests.assert((select count(*) from public.expenses) = 0, 'Anonym sieht keine Ausgaben');
select tests.expect_error(format('select public.create_household(%L)', 'Anonym'), 'Anonym kann keinen Haushalt anlegen');
reset role;

\echo 'Alle Datenbanktests bestanden.'
