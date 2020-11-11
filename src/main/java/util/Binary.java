package util;

public class Binary<T, S>
{
    T a;
    S b;

    public Binary(T a, S b)
    {
        this.a = a;
        this.b = b;
    }

    public T getA()
    {
        return a;
    }

    public S getB()
    {
        return b;
    }
}
