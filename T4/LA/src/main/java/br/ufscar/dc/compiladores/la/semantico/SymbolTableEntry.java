package br.ufscar.dc.compiladores.la.semantico;

public class SymbolTableEntry {
    public String name;
    public SymbolTable.TypeLAIdentifier identifierType;
    public SymbolTable.TypeLAVariable variableType;
    public SymbolTable argsRegFunc;
    public String functionType;
}
