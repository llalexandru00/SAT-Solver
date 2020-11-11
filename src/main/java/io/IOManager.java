package io;

import sat.Formula;

public interface IOManager
{
    Formula read();
    void write(String sat);
    void silence(boolean val);
}
