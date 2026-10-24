-- Härtung nach den Supabase-Advisors.

-- Hilfsfunktionen nur für angemeldete Nutzer -----------------------------------
-- Die RLS-Policies (alle "to authenticated") brauchen EXECUTE für authenticated;
-- anonyme Aufrufe über /rest/v1/rpc sollen gar nicht erst möglich sein.
revoke execute on function
    public.is_household_member(uuid),
    public.is_member_of(uuid, uuid),
    public.shares_household_with(uuid)
from public, anon;

grant execute on function
    public.is_household_member(uuid),
    public.is_member_of(uuid, uuid),
    public.shares_household_with(uuid)
to authenticated;

-- Der Trigger für neue Nutzer läuft nur intern, nie über die API.
revoke execute on function public.handle_new_user() from public, anon, authenticated;

-- Indizes für Fremdschlüssel, über die gefiltert oder kaskadiert wird ------------
create index household_invites_household_idx on public.household_invites (household_id);
create index category_rules_household_idx on public.category_rules (household_id);
create index category_rules_category_idx on public.category_rules (category_id, household_id);
create index recurring_rules_household_idx on public.recurring_rules (household_id);
create index expenses_category_idx on public.expenses (category_id, household_id);
create index expenses_paid_by_idx on public.expenses (paid_by);
create index expenses_recurring_rule_idx on public.expenses (recurring_rule_id);
create index budgets_category_idx on public.budgets (category_id, household_id);
create index savings_goals_household_idx on public.savings_goals (household_id);
create index goal_contributions_goal_idx on public.goal_contributions (goal_id, household_id);
create index settlements_from_user_idx on public.settlements (from_user);
create index settlements_to_user_idx on public.settlements (to_user);
