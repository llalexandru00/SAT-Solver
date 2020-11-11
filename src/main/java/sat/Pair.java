package sat;

public class Pair
{
    private int key;
    private boolean value;

    public Pair(int key, boolean value)
    {
        if (key < 0)
        {
            key *= -1;
            value = !value;
        }

        this.key = key;
        this.value = value;
    }

    public int getKey()
    {
        return key;
    }

    public boolean getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "Key, value: " + key + " "  + value;
    }
}
