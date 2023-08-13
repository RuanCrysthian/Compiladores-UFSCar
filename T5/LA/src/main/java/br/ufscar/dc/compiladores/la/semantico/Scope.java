package br.ufscar.dc.compiladores.la.semantico;

import java.util.LinkedList;
import java.util.List;

public class Scope {
    private LinkedList<SymbolTable> tablesStack;

    public Scope() {
        tablesStack = new LinkedList<>();
        createNewScope();
    }

    public void createNewScope() {
        tablesStack.push(new SymbolTable());
    }

    public SymbolTable getCurrentScope() {
        return tablesStack.peek();
    }

    public List<SymbolTable> runNestedScopes() {
        return tablesStack;
    }

    public void giveupScope() {
        tablesStack.pop();
    }
}
