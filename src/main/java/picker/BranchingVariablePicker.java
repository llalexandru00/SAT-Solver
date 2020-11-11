package picker;

import sat.Assignment;
import sat.Formula;
import sat.Pair;

public interface BranchingVariablePicker
{
    Pair pick(Formula formula, Assignment assignment);
}
