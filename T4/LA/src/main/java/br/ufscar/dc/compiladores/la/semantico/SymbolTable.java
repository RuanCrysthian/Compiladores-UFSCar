package br.ufscar.dc.compiladores.la.semantico;

import java.util.HashMap;

public class SymbolTable {
    public enum TypeLAVariable {
        LITERAL,
        INTEIRO,
        REAL,
        LOGICO,
        NAO_DECLARADO,
        INVALIDO
    }

    public enum TypeLAIdentifier {
        VARIAVEL,
        CONSTANTE,
        TIPO,
        PROCEDIMENTO,
        FUNCAO
    }

    private HashMap<String, SymbolTableEntry> symbolTable;
    private SymbolTable global;

    public SymbolTable() {
        this.symbolTable = new HashMap<>();
        this.global = null;
    }

    void setGlobal(SymbolTable global) {
        this.global = global;
    }

    public void put(String name, TypeLAIdentifier identifierType, TypeLAVariable variableType) {
        SymbolTableEntry st = new SymbolTableEntry();
        st.name = name;
        st.identifierType = identifierType;
        st.variableType = variableType;
        symbolTable.put(name, st);
    }

    public boolean exists(String name) {
        if (global == null) {
            return symbolTable.containsKey(name);
        } else {
            return symbolTable.containsKey(name) || global.exists(name);
        }
    }

    public SymbolTableEntry check(String name) {
        if (global == null)
            return symbolTable.get(name);
        else {
            if (symbolTable.containsKey(name))
                return symbolTable.get(name);
            else
                return global.check(name);
        }
    }
}
