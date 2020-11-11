package sat;

import java.util.*;

public class FreqUpdater
{
    private Map<Integer, Integer> freqs = new HashMap<>(); // literal -> frecventa
    private TreeMap<Integer, Set<Integer>> invFreqs = new TreeMap<>(); // freq -> multime de literali
    private Map<Integer, Integer> defaults = new HashMap<>();  // literal -> de cate ori apare in formula
    private Random rand = new Random();

    public void increase(int lit)
    {
        defaults.put(lit, defaults.getOrDefault(lit, 0) + 1);
    }

    public int getLast()
    {
        if (rand.nextInt(10) < 2)
            return (int) freqs.keySet().toArray()[rand.nextInt(freqs.size())];
        return invFreqs.lastEntry().getValue().iterator().next();
    }

    public void remove(int key)
    {
        removeLiteral(key);
        removeLiteral(-key);
    }

    private void removeLiteral(int key)
    {
        int freqA = freqs.get(key);

        Set<Integer> lits = invFreqs.get(freqA);
        if (lits != null)
        {
            lits.remove(key);
            if (lits.isEmpty())
                invFreqs.remove(freqA);
        }

        freqs.remove(key);
    }

    public void add(int key)
    {
        addLiteral(key);
        addLiteral(-key);
    }

    private void addLiteral(int key)
    {
        int newFreq = defaults.get(key);
        freqs.put(key, newFreq);
        invFreqs.computeIfAbsent(newFreq, k -> new HashSet<>()).add(key);
    }
}
