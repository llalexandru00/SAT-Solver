package exception;


public class FatalException extends RuntimeException
{
    public FatalException(String msg, Exception e)
    {
        super(msg);
        this.addSuppressed(e);
    }

    public FatalException(String msg)
    {
        super(msg);
    }
}
