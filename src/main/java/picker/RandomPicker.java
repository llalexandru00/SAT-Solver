package picker;

import sat.Assignment;
import sat.Formula;
import sat.Pair;

import java.util.*;

public class RandomPicker implements BranchingVariablePicker
{
    @Override
    public Pair pick(Formula formula, Assignment assignment)
    {
        Set<Integer> unassigned = assignment.getUnassigned();

        List<Integer> list = new ArrayList<>(unassigned);
        Random rnd = new Random(1);
        Collections.shuffle(list, rnd);
        int random = rnd.nextInt(10);
        return new Pair(list.get(0), random % 2 == 0);
    }
}
