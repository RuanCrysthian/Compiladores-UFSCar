package br.ufscar.dc.compiladores.la.semantico;

import br.ufscar.dc.compiladores.la.semantico.SymbolTable.TypeLAVariable;

public class LaSemanticAnalyzer extends laBaseVisitor<Void> {
    Scope nestedScopes = new Scope();
    SymbolTable symbolTable;

    @Override
    public Void visitDeclaracao_local(laParser.Declaracao_localContext ctx) {
        // Logic for the rule "declaracao_local"
        for (var ctxIdentVariable : ctx.variavel().identificador()) {
            StringBuilder variableIdentifier = new StringBuilder();
            for (var ident : ctxIdentVariable.IDENT())
                variableIdentifier.append(ident.getText());

            var currentScope = nestedScopes.getCurrentScope();

            // Verifies if the variable identifier has been declared before.
            if (currentScope.exists(variableIdentifier.toString())) {
                LaUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                        "identificador " + variableIdentifier + " ja declarado anteriormente\n");
            } else {
                var variableType = ctx.variavel().tipo().getText();
                // Switch-case to handle different variable types.
                switch (variableType) {
                    case "inteiro":
                        currentScope.put(variableIdentifier.toString(),
                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.INTEIRO);
                        break;
                    case "literal":
                        currentScope.put(variableIdentifier.toString(),
                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LITERAL);
                        break;
                    case "real":
                        currentScope.put(variableIdentifier.toString(),
                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.REAL);
                        break;
                    case "logico":
                        currentScope.put(variableIdentifier.toString(),
                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LOGICO);
                        break;
                    default:
                        // If the type is not a basic type, check if it's a valid user-defined type.
                        if (currentScope.exists(variableType) && currentScope
                                .check(variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                            if (currentScope.exists(variableIdentifier.toString())) {
                                LaUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                                        "identificador " + variableIdentifier + " ja declarado anteriormente\n");
                            }
                        }

                        // If the type is not declared, add a semantic error indicating that the type is
                        // undeclared.
                        if (!currentScope.exists(variableType)) {
                            LaUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                                    "tipo " + variableType + " nao declarado\n");
                            // Add the variable to the current scope with an INVALIDO type.
                            currentScope.put(variableIdentifier.toString(),
                                    SymbolTable.TypeLAIdentifier.VARIAVEL,
                                    SymbolTable.TypeLAVariable.INVALIDO);
                        }
                        break;
                }
            }
        }

        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Void visitCmd(laParser.CmdContext ctx) {
        // Logic for the rule "cmd" (command), i.e., handling actions when a command is
        // found during code analysis.
        if (ctx.cmdLeia() != null) {
            var currentScope = nestedScopes.getCurrentScope();

            // Iterate over the identifiers in the command and perform semantic type
            // verification.
            for (var ident : ctx.cmdLeia().identificador()) {
                LaUtils.verifyType(currentScope, ident);
            }
        }

        if (ctx.cmdAtribuicao() != null) {
            var currentScope = nestedScopes.getCurrentScope();
            var leftValue = LaUtils.verifyType(currentScope, ctx.cmdAtribuicao().identificador());
            var rightValue = LaUtils.verifyType(currentScope, ctx.cmdAtribuicao().expressao());

            // Check for assignment to pointers.
            var assignment = ctx.cmdAtribuicao().getText().split("<-");
            if (!LaUtils.verifyType(leftValue, rightValue) && !assignment[0].contains("^")) {
                // Add a semantic error if the assignment is not compatible with the identifier
                // present in the assignment.
                LaUtils.addSemanticError(ctx.cmdAtribuicao().identificador().IDENT(0).getSymbol(),
                        "atribuicao nao compativel para " + ctx.cmdAtribuicao().identificador().getText() + "\n");
            }
        }

        // Allow the visit to continue to the child nodes of the "cmd" rule.
        return super.visitCmd(ctx);
    }

    @Override
    public Void visitExp_aritmetica(laParser.Exp_aritmeticaContext ctx) {
        // Logic for the rule "exp_aritmetica"
        var currentScope = nestedScopes.getCurrentScope();
        LaUtils.verifyType(currentScope, ctx);
        return super.visitExp_aritmetica(ctx);
    }
}
