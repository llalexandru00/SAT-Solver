package io;

import exception.FatalException;
import sat.Clause;
import sat.Formula;

import java.io.*;
import java.util.Scanner;

public class DimacsCNF implements IOManager
{
    private String url;
    private boolean quiet;

    public DimacsCNF(String url)
    {
        this.url = url;
    }

    @Override
    public Formula read()
    {
        Formula formula = new Formula();
        File myObj = new File(url);
        int literalNumber = -1, clauseNumber = -1;
        try (Scanner reader = new Scanner(myObj);)
        {
            while (reader.hasNextLine())
            {
                String line = reader.nextLine();

                String[] tokens = line.split(" ");
                if (tokens.length == 0 || tokens[0].equals("c"))
                {
                    continue;
                }

                if (tokens[0].equals("p"))
                {
                    if (literalNumber != -1)
                    {
                        throw new FatalException("Multiple definitions of the parameters.");
                    }

                    if (tokens.length != 4)
                    {
                        throw new FatalException("Malformed input.");
                    }

                    String form = tokens[1];
                    if (!form.equals("cnf"))
                    {
                        throw new FatalException("Can't solve non-cnf formula.");
                    }

                    literalNumber = Integer.parseInt(tokens[2]);
                    clauseNumber = Integer.parseInt(tokens[3]);

                    if (literalNumber < 0 || clauseNumber < 0)
                    {
                        throw new FatalException("Invalid parameters.");
                    }

                    continue;
                }

                Clause clause = new Clause();
                for (String literal : tokens)
                {
                    clause.add(Integer.parseInt(literal));
                }

                clause.removeLast();

                formula.append(clause);
            }
        }
        catch (FileNotFoundException e)
        {
            throw new FatalException("Input file not found.", e);
        }
        catch (NumberFormatException e)
        {
            throw new FatalException("Can't parse numbers.", e);
        }

        if (clauseNumber == -1)
        {
            throw new FatalException("Invalid parameters");
        }
        formula.setClauseNumber(clauseNumber);
        formula.setLiteralNumber(literalNumber);
        return formula;
    }

    @Override
    public void write(String sat)
    {
        if (!quiet)
        {
            System.out.println(sat);
        }
    }

    @Override
    public void silence(boolean val) {
        this.quiet = val;
    }
}
