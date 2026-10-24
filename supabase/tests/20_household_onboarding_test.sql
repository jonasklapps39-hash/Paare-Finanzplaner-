-- Tests für das Onboarding: ein Haushalt pro Person, Codes, Vorschau.
-- Nutzt die Hilfsfunktionen aus 10_rls_test.sql.
\set ON_ERROR_STOP on
\set dora '44444444-4444-4444-4444-444444444444'
\set finn '55555555-5555-5555-5555-555555555555'
\set gina '66666666-6666-6666-6666-666666666666'
\set hugo '77777777-7777-7777-7777-777777777777'

-- Prüft zusätzlich den maschinenlesbaren Hinweis, den die App auswertet.
create function tests.expect_hint(p_sql text, p_hint text, p_description text)
returns void language plpgsql as $$
declare
    v_hint text;
begin
    begin
        execute p_sql;
    exception when others then
        get stacked diagnostics v_hint = pg_exception_hint;
        if v_hint is distinct from p_hint then
            raise exception 'FEHLGESCHLAGEN: % – Hinweis war "%" statt "%" (%)', p_description, v_hint, p_hint, sqlerrm;
        end if;
        raise notice 'ok: % (%)', p_description, p_hint;
        return;
    end;
    raise exception 'FEHLGESCHLAGEN: erwarteter Fehler blieb aus: %', p_description;
end;
$$;

insert into auth.users (id, email, raw_user_meta_data) values
    (:'dora', 'dora@example.com', '{"display_name": "Dora"}'),
    (:'finn', 'finn@example.com', '{"display_name": "Finn"}'),
    (:'gina', 'gina@example.com', '{"display_name": "Gina"}'),
    (:'hugo', 'hugo@example.com', '{"display_name": "Hugo"}');

select tests.assert(public.normalize_invite_code(' ab12-cd34 ') = 'AB12CD34', 'Code wird normalisiert');

-- Dora legt einen Haushalt an ------------------------------------------------------
select tests.login(:'dora');
set role authenticated;

select id as hh from public.create_household('Dora & Finn') \gset
select tests.expect_hint(format('select public.create_household(%L)', 'Zweiter Haushalt'), 'already_member', 'Nur ein Haushalt pro Person');
update public.household_members set fixed_share_bps = 7000 where user_id = :'dora';
select public.create_household_invite(:'hh') as code \gset
reset role;

select lower(substr(:'code', 1, 4) || '-' || substr(:'code', 5)) as fancy_code \gset

-- Finn sieht die Vorschau und tritt bei ----------------------------------------------
select tests.login(:'finn');
set role authenticated;

select tests.assert(
    (select household_name = 'Dora & Finn' and inviter_name = 'Dora' from public.preview_household_invite(:'fancy_code')),
    'Vorschau zeigt Haushalt und einladende Person'
);
select tests.assert(public.accept_household_invite(:'fancy_code') = :'hh', 'Beitritt mit Bindestrich und Kleinschreibung');
select tests.assert(
    (select bool_and(fixed_share_bps = 5000) from public.household_members where household_id = :'hh'),
    'Fester Anteil startet nach dem Beitritt bei 50/50'
);
select tests.expect_hint(format('select public.create_household_invite(%L)', :'hh'), 'household_full', 'Voller Haushalt kann nicht einladen');
reset role;

-- Gina: benutzte und abgelaufene Codes -----------------------------------------------
select tests.login(:'gina');
set role authenticated;

select tests.expect_hint(format('select * from public.preview_household_invite(%L)', :'code'), 'invite_invalid', 'Benutzter Code zeigt keine Vorschau');
select tests.expect_hint(format('select public.accept_household_invite(%L)', 'XXXX-0000'), 'invite_invalid', 'Unbekannter Code');
select tests.expect_hint(format('select public.create_household_invite(%L)', :'hh'), 'not_member', 'Fremde können nicht einladen');

select id as gina_hh from public.create_household('Ginas Haushalt') \gset
select public.create_household_invite(:'gina_hh') as old_code \gset
select public.create_household_invite(:'gina_hh') as gina_code \gset
reset role;

select tests.login(:'hugo');
set role authenticated;
select tests.expect_hint(format('select * from public.preview_household_invite(%L)', :'old_code'), 'invite_invalid', 'Neuer Code macht alten ungültig');
select tests.assert(
    (select inviter_name = 'Gina' from public.preview_household_invite(:'gina_code')),
    'Aktueller Code funktioniert'
);
reset role;

update public.household_invites set expires_at = now() - interval '1 minute' where code = :'gina_code';

select tests.login(:'hugo');
set role authenticated;
select tests.expect_hint(format('select * from public.preview_household_invite(%L)', :'gina_code'), 'invite_invalid', 'Abgelaufener Code zeigt keine Vorschau');
select tests.expect_hint(format('select public.accept_household_invite(%L)', :'gina_code'), 'invite_invalid', 'Abgelaufener Code funktioniert nicht');
reset role;

-- Wer schon einen Haushalt hat, kann nicht woanders beitreten ------------------------
select tests.login(:'gina');
set role authenticated;
select public.create_household_invite(:'gina_hh') as fresh_code \gset
reset role;

select tests.login(:'dora');
set role authenticated;
select tests.expect_hint(format('select public.accept_household_invite(%L)', :'fresh_code'), 'already_member', 'Kein Beitritt zu einem zweiten Haushalt');
reset role;

-- Nicht angemeldet --------------------------------------------------------------
select set_config('request.jwt.claim.sub', '', false);
set role anon;
select tests.expect_error(format('select * from public.preview_household_invite(%L)', :'fresh_code'), 'Anonym keine Vorschau');
reset role;

\echo 'Onboarding-Tests bestanden.'
