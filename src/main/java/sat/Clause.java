package sat;

import exception.FatalException;
import util.Binary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Clause
{
    private List<Integer> literals = new ArrayList<>();
    private int refA;
    private int refB;
    private boolean bulk;
    private int hash;
    private final int MAX_LITERAL = (int) 1e3;
    private final int MOD_LITERAL = (int) 1e9;
    private boolean _final = false;

    static final int SATISFIED = 0,
                     UNSATISFIED = 1,
                     UNIT = 2,
                     UNRESOLVED = 3;

    public void add(Integer literal)
    {
        if (_final)
            throw new FatalException("Clause is final");

        this.literals.add(literal);

        if (!bulk)
        {
            resetReferences();
        }
    }

    private void resetReferences()
    {
        if (_final)
            throw new FatalException("Clause is final");

        refA = 0;
        refB = literals.size() - 1;

        hash = 0;
        for (Integer literal : literals)
        {
            hash = hash * MAX_LITERAL + literal;
            hash %= MOD_LITERAL;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        literals.forEach((literal) -> sb.append(literal).append(' '));
        return sb.toString();
    }

    public void removeLast()
    {
        if (_final)
            throw new FatalException("Clause is final");

        literals.remove(literals.size() - 1);
        resetReferences();
    }

    public Pair getUnit()
    {
        return new Pair(literals.get(refA), true);
    }

    public int notify(Assignment assignment)
    {
        refA = calibrate(refA, assignment, refB);
        refB = calibrate(refB, assignment, refA);

        return getStatus(assignment);
    }

    private int getStatus(Assignment assignment)
    {
        Assignment.Status statusA = assignment.getStatus(literals.get(refA));
        Assignment.Status statusB = assignment.getStatus(literals.get(refB));

        if (statusA.equals(Assignment.Status.TRUE) || statusB.equals(Assignment.Status.TRUE))
            return SATISFIED;

        if (statusA.equals(Assignment.Status.FALSE) && refA == refB) // all are 0
            return UNSATISFIED;

        if (statusA.equals(Assignment.Status.UNKNOWN) && refA == refB) // all are 0 excepting the current one
            return UNIT;

        return UNRESOLVED;
    }

    private int calibrate(int pointer, Assignment assignment, int other)
    {
        Assignment.Status status = assignment.getStatus(literals.get(pointer));
        if (!status.equals(Assignment.Status.FALSE) && pointer != other) // not pointing to false is ok
            return pointer;

        int start = pointer;
        pointer = (pointer + 1) % literals.size();
        status = assignment.getStatus(literals.get(pointer));
        while ((status.equals(Assignment.Status.FALSE) || pointer == other) && pointer != start)
        {
            pointer = (pointer + 1) % literals.size();
            status = assignment.getStatus(literals.get(pointer));
        }

        return pointer == start ? other : pointer;
    }

    public List<Integer> getLiterals()
    {
        return literals;
    }

    public Clause resolve(Clause prev)
    {
        Clause newClause = new Clause();

        Set<Integer> Total = new HashSet<>();
        Total.addAll(literals);
        Total.addAll(prev.literals);
        newClause.startBulk();
        for (Integer literal : Total)
        {
            if (!Total.contains(-literal))
            {
                newClause.add(literal);
            }
        }
        newClause.finishBulk();
        return newClause;
    }

    private void startBulk()
    {
        bulk = true;
    }

    private void finishBulk()
    {
        bulk = false;
        resetReferences();
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public boolean equals(Object clause)
    {
        return this == clause;
    }

    public Binary<Integer, Integer> getWatched()
    {
        return new Binary<>(literals.get(refA), literals.get(refB));
    }

    public void makeFinal()
    {
        this._final = true;
    }

    public int naiveSolve(Assignment assignment)
    {
        for (Integer literal : literals)
        {
            Assignment.Status value = assignment.getStatus(literal);
            if (value.equals(Assignment.Status.UNKNOWN))
                return UNRESOLVED;
            if (value.equals(Assignment.Status.TRUE))
                return SATISFIED;
        }

        return UNSATISFIED;
    }
}
