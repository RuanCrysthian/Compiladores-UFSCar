package br.ufscar.dc.compiladores.la.semantico;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    public enum TypeLAVariable {
        LITERAL,
        INTEIRO,
        REAL,
        LOGICO,
        NAO_DECLARADO,
        INVALIDO,
        PONT_INTE,
        PONT_REAL,
        PONT_LOGI,
        PONT_LITE,
        ENDERECO,
        REGISTRO
    }

    public enum TypeLAIdentifier {
        VARIAVEL,
        CONSTANTE,
        TIPO,
        PROCEDIMENTO,
        FUNCAO,
        REGISTRO
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

    public void put(String name, TypeLAIdentifier identifierType, TypeLAVariable variableType,
            SymbolTable argsRegFunc) {
        SymbolTableEntry st = new SymbolTableEntry();
        st.name = name;
        st.identifierType = identifierType;
        st.variableType = variableType;
        st.argsRegFunc = argsRegFunc;
        symbolTable.put(name, st);
    }

    public void put(String name, TypeLAIdentifier identifierType, TypeLAVariable variableType, SymbolTable argsRegFunc,
            String funcType) {
        SymbolTableEntry st = new SymbolTableEntry();
        st.name = name;
        st.identifierType = identifierType;
        st.variableType = variableType;
        st.argsRegFunc = argsRegFunc;
        st.functionType = funcType;
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

    public boolean validType(ArrayList<SymbolTable.TypeLAVariable> types) {
        int counter = 0;

        if (symbolTable.size() != types.size())
            return false;
        for (SymbolTableEntry entry : symbolTable.values()) {
            if (types.get(counter) != entry.variableType) {
                return false;
            }
            counter++;
        }

        return true;
    }
}
