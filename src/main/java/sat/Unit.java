package sat;

public class Unit
{
    private Pair pair;
    private Clause clause;

    public Unit(Pair pair, Clause clause)
    {
        this.pair = pair;
        this.clause = clause;
    }

    public Pair getPair()
    {
        return pair;
    }

    public Clause getClause()
    {
        return clause;
    }

    @Override
    public String toString()
    {
        return "Unit: " + pair.toString() + " " + clause.toString();
    }
}
