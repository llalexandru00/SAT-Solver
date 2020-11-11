package sat;

import util.Binary;

import java.util.*;

public class Formula
{
    private List<Clause> clauses = new ArrayList<>();
    private List<Set<Clause>> cache = new ArrayList<>(4);
    private Map<Clause, Integer> dirty = new HashMap<>();
    private Map<Integer, Set<Clause>> watchedByVar = new HashMap<>();
    private Map<Integer, Set<Clause>> clauseByVar = new HashMap<>();
    private FreqUpdater freqUpdater;
    private int clauseNumber;
    private int literalNumber;

    public Formula()
    {
        cache.add(new HashSet<>());
        cache.add(new HashSet<>());
        cache.add(new HashSet<>());
        cache.add(new HashSet<>());
    }

    public void attachFreqUpdater(FreqUpdater freqUpdater)
    {
        this.freqUpdater = freqUpdater;
        for (Clause clause : clauses)
        {
            for (Integer literal : clause.getLiterals())
            {
                freqUpdater.increase(literal);
            }
        }
    }

    public void append(Clause clause)
    {
        append(clause, false);
    }

    public void append(Clause clause, boolean isNew)
    {
        this.clauses.add(clause);
        clause.makeFinal();

        Binary<Integer, Integer> refs = clause.getWatched();
        watch(refs.getA(), clause);
        watch(refs.getB(), clause);

        if (!isNew)
        {
            for (Integer literal : clause.getLiterals())
            {
                if (freqUpdater != null)
                {
                    freqUpdater.increase(literal);
                }
            }
        }

        if (clause.getLiterals().size() == 1)
        {
            cache.get(Clause.UNIT).add(clause);
            dirty.put(clause, Clause.UNIT);
        }
        else
        {
            if (isNew)
            {
                cache.get(Clause.UNSATISFIED).add(clause);
                dirty.put(clause, Clause.UNSATISFIED);
                for (Integer literal : clause.getLiterals())
                {
                    clauseByVar.computeIfAbsent(literal < 0 ? -literal : literal, k -> new HashSet<>()).add(clause);
                }
            }
            else
            {
                cache.get(Clause.UNRESOLVED).add(clause);
                dirty.put(clause, Clause.UNRESOLVED);
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Literal number: ").append(literalNumber).append('\n');
        sb.append("Clause number: ").append(clauseNumber).append('\n');
        sb.append("Clauses: ").append('\n');
        clauses.forEach((clause) -> sb.append(clause.toString()).append('\n'));
        return sb.toString();
    }

    public void setClauseNumber(int clauseNumber)
    {
        this.clauseNumber = clauseNumber;
    }

    public void setLiteralNumber(int literalNumber)
    {
        this.literalNumber = literalNumber;
    }

    public int getLiteralNumber()
    {
        return literalNumber;
    }

    public Unit getUnit()
    {
        if (cache.get(Clause.UNIT).isEmpty())
            return null;

        Clause u = cache.get(Clause.UNIT).iterator().next();
        return new Unit(u.getUnit(), u);
    }

    public boolean hasConflict()
    {
        return !cache.get(Clause.UNSATISFIED).isEmpty();
    }

    public void undo(Integer literal, Assignment assignment)
    {
        Set<Clause> clauses = clauseByVar.get(literal < 0 ? -literal : literal);

        if (clauses == null)
        {
            return;
        }

        update(clauses, assignment);
    }

    public void notify(Integer literal, Assignment assignment)
    {
        Set<Clause> watched = watchedByVar.get(literal < 0 ? -literal : literal);

        if (watched == null)
        {
            return;
        }

        update(watched, assignment);
    }

    private void update(Set<Clause> clauses, Assignment assignment)
    {
        new ArrayList<>(clauses).forEach((clause) -> {

            int oldStatus = dirty.get(clause);

            Binary<Integer, Integer> olds = clause.getWatched();
            int status = clause.notify(assignment);
            Binary<Integer, Integer> news = clause.getWatched();

            if (!olds.getA().equals(news.getA()) || !olds.getA().equals(news.getB()))
            {
                unwatch(olds.getA(), clause);
                if (!olds.getA().equals(olds.getB()))
                    unwatch(olds.getB(), clause);

                watch(news.getA(), clause);
                if (!news.getA().equals(news.getB()))
                    watch(news.getB(), clause);
            }

            if (status != oldStatus)
            {
                cache.get(oldStatus).remove(clause);
                cache.get(status).add(clause);
                dirty.put(clause, status);
            }

            if (!isUnitOrUnsatisfied(oldStatus) && isUnitOrUnsatisfied(status))
            {
                for (Integer literal : clause.getLiterals())
                {
                    clauseByVar.computeIfAbsent(literal < 0 ? -literal : literal, k -> new HashSet<>()).add(clause);
                }
            }

            if (isUnitOrUnsatisfied(oldStatus) && !isUnitOrUnsatisfied(status))
            {
                for (Integer literal : clause.getLiterals())
                {
                    clauseByVar.computeIfAbsent(literal < 0 ? -literal : literal, k -> new HashSet<>()).remove(clause);
                }
            }

        });

    }

    private boolean isUnitOrUnsatisfied(int status)
    {
        return status == Clause.UNIT || status == Clause.UNSATISFIED;
    }

    private void watch(int literal, Clause clause)
    {
        watchedByVar.computeIfAbsent(literal < 0 ? -literal : literal, k -> new HashSet<>()).add(clause);
    }

    private void unwatch(int literal, Clause clause)
    {
        watchedByVar.computeIfAbsent(literal < 0 ? -literal : literal, k -> new HashSet<>()).remove(clause);
    }

    public Clause getConflict()
    {
        if (cache.get(Clause.UNSATISFIED).isEmpty())
            return null;

        return cache.get(Clause.UNSATISFIED).iterator().next();
    }

    public boolean check(Assignment assignment)
    {
        if (assignment.size() != literalNumber)
            return false;

        for (Clause clause : clauses)
        {
            if (clause.naiveSolve(assignment) != Clause.SATISFIED)
                return false;
        }

        return true;
    }
}
