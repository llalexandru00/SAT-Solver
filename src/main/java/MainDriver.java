import engine.CDCL;
import exception.FatalException;
import io.DimacsCNF;
import io.IOManager;
import sat.Formula;

public class MainDriver
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            throw new FatalException("Incorrect number of arguments.");
        }

        IOManager io = new DimacsCNF(args[0]);
        Formula formula = io.read();

        new CDCL(formula, io).run();
    }
}
