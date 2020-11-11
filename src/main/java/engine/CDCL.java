package engine;

import exception.FatalException;
import io.IOManager;
import picker.BranchingVariablePicker;
import picker.FrequencyPicker;
import sat.*;

import java.util.*;

public class CDCL implements Runnable
{
    private IOManager io;
    private Formula formula;
    private Assignment assignment;
    private FreqUpdater freqUpdater = new FreqUpdater();
    private BranchingVariablePicker bvp = new FrequencyPicker(freqUpdater);
    private int level;
    private int steps;

    private int restartThreshold = 8;

    private Map<Integer, Integer> literalToLevel = new HashMap<>();
    private Map<Integer, Set<Integer>> levelLiterals = new HashMap<>();
    private Map<Integer, Clause> antecedent = new HashMap<>();


    public CDCL(Formula formula, IOManager io)
    {
        this.formula = formula;
        this.io = io;
        assignment = new Assignment(formula.getLiteralNumber());
        this.formula.attachFreqUpdater(freqUpdater);
        assignment.attachFreqUpdater(freqUpdater);
        assignment.clear();
    }

    private void assign(Pair pair)
    {
        // update assignment
        assignment.add(pair);

        // update internal maps
        int literal = pair.getKey();

        literalToLevel.put(literal, level);
        Set<Integer> list = levelLiterals.get(level);
        if (list == null)
        {
            list = new HashSet<>();
        }
        list.add(literal);
        levelLiterals.put(level, list);

        // update formula and clauses internal maps
        formula.notify(literal, assignment);
    }

    private boolean unitPropagation()
    {
        Unit unit = formula.getUnit();
        while (unit != null && !formula.hasConflict())
        {
            io.write(unit.toString());
            assign(unit.getPair());
            antecedent.put(unit.getPair().getKey(), unit.getClause());
            unit = formula.getUnit();
        }

        return hasConflict();
    }

    private boolean hasConflict()
    {
        return formula.hasConflict();
    }

    private boolean allVariablesAssigned()
    {
        if (assignment.size() > maxProgress)
        {
            maxProgress = assignment.size();
            System.out.println("Progress: " + maxProgress + "/" + formula.getLiteralNumber());
        }
        return assignment.size() == formula.getLiteralNumber();
    }

    private Pair pickBranchingVariable()
    {
        return bvp.pick(formula, assignment);
    }

    private Clause findAntecedent(Clause clause)
    {
        for (Integer literal : clause.getLiterals())
        {
            int normalizedLiteral = literal < 0 ? -1 * literal : literal;
            if (antecedent.get(normalizedLiteral) != null)
                return antecedent.get(normalizedLiteral);
        }
        return null;
    }

    private Clause generateNewClause()
    {
        Clause conflict = formula.getConflict();
        if (conflict == null)
        {
            throw new FatalException("Conflict was not found even if detected by unit propagation!");
        }

        Clause newClause = new Clause();
        for (Integer literal : conflict.getLiterals())
        {
            newClause.add(literal);
        }

        Clause prev = findAntecedent(newClause);
        while (prev != null)
        {
            newClause = newClause.resolve(prev);
            prev = findAntecedent(newClause);
        }

        if (newClause.getLiterals().isEmpty())
        {
            throw new FatalException("Resolut empty clause!");
        }

        return newClause;
    }

    private int conflictAnalyze()
    {
        Clause newClause = generateNewClause();
        io.write("Learned new clause: " + newClause);
        formula.append(newClause, true);
        for (Integer literal : newClause.getLiterals())
        {
            formula.notify(literal, assignment);
        }

        int maxim = -1;
        boolean first = true;

        for (Integer literal : newClause.getLiterals())
        {
            int normalizedLiteral = literal < 0 ? literal * -1 : literal;
            if (antecedent.get(normalizedLiteral) != null)
            {
                throw new FatalException("The new learned claused is has antecedents.");
            }

            if (assignment.getStatus(normalizedLiteral) == Assignment.Status.UNKNOWN)
            {
                throw new FatalException("All literals should have been assigned.");
            }

            int litLevel = literalToLevel.get(normalizedLiteral);
            if (litLevel > maxim || first)
            {
                maxim = litLevel;
                first = false;
            }
        }

        return maxim - 1;
    }

    private void backtrack(int beta)
    {
        for (int i = level; i > beta; i--)
        {
            Set<Integer> assigned = levelLiterals.get(i);

            if (assigned == null || assigned.isEmpty())
            {
                throw new FatalException("Can't have empty decision levels.");
            }

            for (Integer literal : assigned)
            {
                literal = literal < 0 ? -literal : literal;

                assignment.remove(literal);
                literalToLevel.remove(literal);
                antecedent.remove(literal);
                formula.undo(literal, assignment);
            }

            levelLiterals.remove(i);
        }
        level = beta;
    }

    private int maxProgress = 0;

    private boolean execute()
    {
        if (unitPropagation())
        {
            return false; // UNSAT
        }

        level = 0;
        while (!allVariablesAssigned())
        {
            if (steps == restartThreshold)
            {
                steps = 0;
                restartThreshold *= 2;

                backtrack(0);
                return execute();
            }

            Pair pair = pickBranchingVariable();
            io.write(pair.toString());
            level++;
            steps++;
            assign(pair);
            // variabilele de decizie nu au antecedent
            antecedent.put(pair.getKey(), null);

            while (unitPropagation())
            {
                if (level == 0)
                    return false;

                int beta = conflictAnalyze();
                backtrack(beta);
            }
        }

        return true; // SAT
    }

    @Override
    public void run()
    {
        io.silence(true);

        long start = System.nanoTime();
        boolean result = execute();
        long finish = System.nanoTime();

        io.silence(false);

        if (result)
        {
            io.write("SAT");
            io.write(assignment.toString());

            if (!formula.check(assignment))
            {
                throw new FatalException("Answer is not correct");
            }
        }
        else
        {
            io.write("UNSAT");
        }

        io.write("Solution computed in: " + (finish - start) / 1e9 + " seconds.");
    }
}
