package picker;

import sat.Assignment;
import sat.Formula;
import sat.FreqUpdater;
import sat.Pair;

public class FrequencyPicker implements BranchingVariablePicker
{

    FreqUpdater freqUpdater;

    public FrequencyPicker(FreqUpdater freqUpdater)
    {
        this.freqUpdater = freqUpdater;
    }

    @Override
    public Pair pick(Formula formula, Assignment assignment)
    {
        return new Pair(freqUpdater.getLast(), true);
    }

}
