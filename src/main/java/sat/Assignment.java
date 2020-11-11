package sat;

import exception.FatalException;

import java.util.*;

public class Assignment
{

    private Map<Integer, Boolean> map = new HashMap<>();
    private Set<Integer> unassigned = new HashSet<>();
    private int literalNumber;
    private FreqUpdater freqUpdater;

    public Assignment(int literalNumber)
    {
        this.literalNumber = literalNumber;
        clear();
    }

    public void add(Pair pair)
    {
        int key = pair.getKey();
        boolean value = pair.getValue();

        if (map.containsKey(key))
        {
            throw new FatalException("Shouldn't update the same value twice.");
        }

        map.put(key, value);
        unassigned.remove(key);

        if (freqUpdater != null)
        {
            freqUpdater.remove(key);
        }
    }

    public int size()
    {
        return map.size();
    }

    public Set<Integer> getUnassigned()
    {
        return unassigned;
    }

    public void remove(int literal)
    {
        literal = literal < 0 ? -literal : literal;
        map.remove(literal);
        unassigned.add(literal);
        if (freqUpdater != null)
        {
            freqUpdater.add(literal);
        }
    }

    public Status getStatus(int literal)
    {
        boolean negated = literal < 0;
        literal = literal < 0 ? -literal : literal;

        if (map.containsKey(literal))
        {
            Status value = map.get(literal) ? Status.TRUE : Status.FALSE;
            return negated ? reversed(value) : value;
        }

        return Status.UNKNOWN;
    }

    private Status reversed(Status status)
    {
        return status.equals(Status.TRUE) ? Status.FALSE : Status.TRUE;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> sb.append(key).append(" : ").append(value).append('\n'));
        return sb.toString();
    }

    public void clear()
    {
        map.clear();
        for (int i = 1; i <= literalNumber; i++)
        {
            unassigned.add(i);
            if (freqUpdater != null)
            {
                freqUpdater.add(i);
            }
        }
    }

    public void attachFreqUpdater(FreqUpdater freqUpdater)
    {
        this.freqUpdater = freqUpdater;
    }

    public enum Status { TRUE, UNKNOWN, FALSE}
}
