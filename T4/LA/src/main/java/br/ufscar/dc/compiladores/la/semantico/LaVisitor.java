package br.ufscar.dc.compiladores.la.semantico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import br.ufscar.dc.compiladores.la.semantico.SymbolTable.TypeLAVariable;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdAtribuicaoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdChamadaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdLeiaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CorpoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Decl_local_globalContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Declaracao_globalContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.DimensaoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Exp_aritmeticaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.ExpressaoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.IdentificadorContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.TipoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.VariavelContext;

public class LaVisitor extends laBaseVisitor<Void> {
    Scope nestedScopes = new Scope();
    SymbolTable symbolTable;

    Boolean defineTypeAndAddtoScope(String variableIdentifier, String variableType, SymbolTable symbolTable) {
        Map<String, TypeLAVariable> typeMappings = new HashMap<>();
        typeMappings.put("inteiro", TypeLAVariable.INTEIRO);
        typeMappings.put("literal", TypeLAVariable.LITERAL);
        typeMappings.put("real", TypeLAVariable.REAL);
        typeMappings.put("logico", TypeLAVariable.LOGICO);
        typeMappings.put("^logico", TypeLAVariable.PONT_LOGI);
        typeMappings.put("^real", TypeLAVariable.PONT_REAL);
        typeMappings.put("^literal", TypeLAVariable.PONT_LITE);
        typeMappings.put("^inteiro", TypeLAVariable.PONT_INTE);

        TypeLAVariable mappedType = typeMappings.get(variableType);
        if (mappedType != null) {
            symbolTable.put(variableIdentifier, SymbolTable.TypeLAIdentifier.VARIAVEL, mappedType);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Void visitDeclaracao_local(laParser.Declaracao_localContext ctx) {
        if (ctx.IDENT() != null) {
            handleIdentifierDeclaration(ctx);
        } else {
            handleVariableDeclaration(ctx);
        }
        return super.visitDeclaracao_local(ctx);
    }

    private void handleIdentifierDeclaration(laParser.Declaracao_localContext ctx) {
        String identifier = ctx.IDENT().getText();
        SymbolTable currentScope = nestedScopes.getCurrentScope();

        if (ctx.tipo_basico() != null) {
            handleConstantDeclaration(ctx, identifier, currentScope);
        } else {
            handleTypeDeclaration(ctx, identifier, currentScope);
        }
    }

    private void handleConstantDeclaration(
            laParser.Declaracao_localContext ctx,
            String identifier,
            SymbolTable currentScope) {
        if (currentScope.exists(identifier)) {
            emitIdentifierAlreadyDeclaredError(ctx, identifier);
        } else {
            String constantType = ctx.tipo_basico().getText();
            TypeLAVariable variableType = getTypeFromTypeName(constantType);

            if (variableType != null) {
                currentScope.put(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE, variableType);
            } else {
                emitIdentifierAlreadyDeclaredError(ctx, constantType);
            }
        }
    }

    private TypeLAVariable getTypeFromTypeName(String typeName) {
        switch (typeName) {
            case "inteiro":
                return TypeLAVariable.INTEIRO;
            case "literal":
                return TypeLAVariable.LITERAL;
            case "real":
                return TypeLAVariable.REAL;
            case "logico":
                return TypeLAVariable.LOGICO;
            case "^logico":
                return TypeLAVariable.PONT_LOGI;
            case "^real":
                return TypeLAVariable.PONT_REAL;
            case "^literal":
                return TypeLAVariable.PONT_LITE;
            case "^inteiro":
                return TypeLAVariable.PONT_INTE;
            default:
                return TypeLAVariable.REGISTRO; // Handle the case of an unknown type name
        }
    }

    private void handleTypeDeclaration(
            laParser.Declaracao_localContext ctx,
            String identifier,
            SymbolTable currentScope) {
        if (currentScope.exists(identifier)) {
            emitIdentifierAlreadyDeclaredError(ctx, identifier);
        } else {
            SymbolTable fieldsTypes = new SymbolTable();
            currentScope.put(identifier, SymbolTable.TypeLAIdentifier.TIPO, null, fieldsTypes);

            for (VariavelContext variable : ctx.tipo().registro().variavel()) {
                for (IdentificadorContext ctxIdentVariable : variable.identificador()) {
                    String variableIdentifier = ctxIdentVariable.getText();
                    String variableType = variable.tipo().getText();
                    handleNestedTypeDeclaration(variableIdentifier, variableType, fieldsTypes, ctxIdentVariable);
                }
            }
        }
    }

    private void handleNestedTypeDeclaration(
            String variableIdentifier,
            String variableType,
            SymbolTable fieldsTypes,
            IdentificadorContext ctxIdentVariable) {
        if (fieldsTypes.exists(variableIdentifier)) {
            emitIdentifierAlreadyDeclaredError(ctxIdentVariable, variableIdentifier);
        } else {
            defineTypeAndAddtoScope(variableIdentifier, variableType, fieldsTypes);
        }
    }

    private void handleVariableDeclaration(laParser.Declaracao_localContext ctx) {
        VariavelContext varContext = ctx.variavel();
        TipoContext typeContext = varContext.tipo();
        SymbolTable currentScope = nestedScopes.getCurrentScope();

        if (typeContext.registro() == null) {
            handleNonRegisterVariableDeclaration(varContext, currentScope);
        } else {
            handleRegisterVariableDeclaration(varContext, currentScope);
        }
    }

    private void handleNonRegisterVariableDeclaration(
            VariavelContext varContext,
            SymbolTable currentScope) {
        TipoContext typeContext = varContext.tipo();

        for (IdentificadorContext ctxIdentVariable : varContext.identificador()) {
            String variableIdentifier = buildVariableIdentifier(ctxIdentVariable);

            if (ctxIdentVariable.dimensao() != null) {
                handleVariableDimension(ctxIdentVariable.dimensao(), currentScope);
            }

            if (currentScope.exists(variableIdentifier)) {
                emitIdentifierAlreadyDeclaredError(ctxIdentVariable, variableIdentifier);
            } else {
                String variableType = typeContext.getText();
                handleVariableTypeDeclaration(variableIdentifier, variableType, currentScope, ctxIdentVariable);
            }
        }
    }

    private String buildVariableIdentifier(IdentificadorContext ctxIdentVariable) {
        return ctxIdentVariable.IDENT().stream().map(ParseTree::getText).collect(Collectors.joining());
    }

    private void handleVariableDimension(DimensaoContext dimensaoContext, SymbolTable currentScope) {
        for (Exp_aritmeticaContext expDim : dimensaoContext.exp_aritmetica()) {
            LaUtils.verifyType(currentScope, expDim);
        }
    }

    private void handleVariableTypeDeclaration(
            String variableIdentifier,
            String variableType,
            SymbolTable currentScope,
            IdentificadorContext ctxIdentVariable) {
        if (!defineTypeAndAddtoScope(variableIdentifier, variableType, currentScope)) {
            if (currentScope.exists(variableType)
                    && currentScope.check(variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                SymbolTableEntry entry = currentScope.check(variableType);
                SymbolTable fieldsType = entry.argsRegFunc;
                currentScope.put(variableIdentifier, SymbolTable.TypeLAIdentifier.REGISTRO, null, fieldsType);
            } else if (!currentScope.exists(variableType)) {
                emitIdentifierAlreadyDeclaredError(ctxIdentVariable, variableType);
                currentScope.put(variableIdentifier, SymbolTable.TypeLAIdentifier.VARIAVEL,
                        SymbolTable.TypeLAVariable.INVALIDO);
            }
        }
    }

    private void handleRegisterVariableDeclaration(
            VariavelContext varContext,
            SymbolTable currentScope) {
        ArrayList<String> registerIdentifiers = new ArrayList<>();

        for (IdentificadorContext ctxIdentReg : varContext.identificador()) {
            String identifierName = ctxIdentReg.getText();

            if (currentScope.exists(identifierName)) {
                emitIdentifierAlreadyDeclaredError(ctxIdentReg, identifierName);
            } else {
                SymbolTable fields = new SymbolTable();
                currentScope.put(identifierName, SymbolTable.TypeLAIdentifier.REGISTRO, null, fields);
                registerIdentifiers.add(identifierName);
            }
        }

        for (VariavelContext ctxVariableRegister : varContext.tipo().registro().variavel()) {
            for (IdentificadorContext ctxVariableRegisterIdent : ctxVariableRegister.identificador()) {
                String registerFieldName = ctxVariableRegisterIdent.getText();

                for (String registerIdentifier : registerIdentifiers) {
                    SymbolTableEntry entry = currentScope.check(registerIdentifier);
                    SymbolTable registerFields = entry.argsRegFunc;

                    if (registerFields.exists(registerFieldName)) {
                        emitIdentifierAlreadyDeclaredError(ctxVariableRegisterIdent, registerFieldName);
                    } else {
                        String variableType = ctxVariableRegister.tipo().getText();
                        handleNestedTypeDeclaration(registerFieldName, variableType, registerFields,
                                ctxVariableRegisterIdent);
                    }
                }
            }
        }
    }

    private void emitIdentifierAlreadyDeclaredError(ParserRuleContext ctx, String identifier) {
        LaUtils.addSemanticError(ctx.getStart(), "identificador " + identifier + " ja declarado anteriormente\n");
    }

    @Override
    public Void visitDeclaracao_global(Declaracao_globalContext ctx) {
        String identifier = ctx.IDENT().getText();

        // recebe escopos
        List<SymbolTable> scopes = nestedScopes.runNestedScopes();
        if (scopes.size() > 1) {
            nestedScopes.giveupScope();
        }
        SymbolTable globalScope = nestedScopes.getCurrentScope();

        if (ctx.tipo_estendido() != null) {
            // tem um tipo e retorna, eh função
            nestedScopes.createNewScope();
            SymbolTable functionScope = nestedScopes.getCurrentScope();
            functionScope.setGlobal(globalScope); // Add global scope reference to symbolTable

            if (globalScope.exists(identifier)) {
                LaUtils.addSemanticError(ctx.IDENT().getSymbol(),
                        "identificador " + identifier + " ja declarado anteriormente\n");
            } else {
                SymbolTable funcParameters = new SymbolTable();
                globalScope.put(identifier, SymbolTable.TypeLAIdentifier.FUNCAO, null, funcParameters,
                        ctx.tipo_estendido().getText());

                for (laParser.ParametroContext declaredParameter : ctx.parametros().parametro()) {
                    String variableType = declaredParameter.tipo_estendido().getText();

                    for (laParser.IdentificadorContext ident : declaredParameter.identificador()) {
                        // dps de declarar o tipo de um parametro, pode declarar varios parametros do
                        // msm tipo
                        String parameterIdentifier = ident.getText();

                        if (functionScope.exists(parameterIdentifier)) {
                            // outro parametro ja identificado com msm nome
                            LaUtils.addSemanticError(ctx.IDENT().getSymbol(),
                                    "identificador " + parameterIdentifier + " ja declarado anteriormente\n");
                        } else {
                            if (defineTypeAndAddtoScope(parameterIdentifier, variableType, functionScope)) {
                                // Caso consiga definir os tipos para o escopo da função, reproduz para os
                                // parametros
                                defineTypeAndAddtoScope(parameterIdentifier, variableType, funcParameters);
                            } else {
                                // Caso não seja um dos tipo_estendido
                                if (globalScope.exists(variableType) && globalScope.check(
                                        variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                                    if (functionScope.exists(parameterIdentifier)) {
                                        LaUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                "identificador " + parameterIdentifier
                                                        + " ja declarado anteriormente\n");
                                    } else {
                                        SymbolTableEntry fields = globalScope.check(variableType);
                                        SymbolTable nestedTableType = fields.argsRegFunc;

                                        functionScope.put(parameterIdentifier,
                                                SymbolTable.TypeLAIdentifier.REGISTRO,
                                                SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                variableType);
                                        funcParameters.put(parameterIdentifier,
                                                SymbolTable.TypeLAIdentifier.REGISTRO,
                                                SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                variableType);
                                    }
                                }
                                if (!globalScope.exists(variableType)) {
                                    LaUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                            "tipo " + variableType + " nao declarado\n");
                                    functionScope.put(parameterIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INVALIDO);
                                    funcParameters.put(parameterIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INVALIDO);
                                }
                            }
                        }

                    }
                }
            }

        } else {
            // é um procedure
            nestedScopes.createNewScope();
            SymbolTable procScope = nestedScopes.getCurrentScope();
            procScope.setGlobal(globalScope); // Add referencia do escopo global na symbolTable

            if (globalScope.exists(identifier)) {
                LaUtils.addSemanticError(ctx.IDENT().getSymbol(),
                        "identificador " + identifier + " ja declarado anteriormente\n");
            } else {
                SymbolTable procParameters = new SymbolTable();
                globalScope.put(identifier, SymbolTable.TypeLAIdentifier.PROCEDIMENTO, null, procParameters);

                for (laParser.ParametroContext declaredParameter : ctx.parametros().parametro()) {
                    String variableType = declaredParameter.tipo_estendido().getText();

                    for (laParser.IdentificadorContext ident : declaredParameter.identificador()) {
                        // dps de declarar o tipo de um parametro, pode declarar varios parametros do
                        // msm tipo
                        String parameterIdentifier = ident.getText();

                        if (procScope.exists(parameterIdentifier)) {
                            // outro parametro ja identificado com msm nome
                            LaUtils.addSemanticError(ctx.IDENT().getSymbol(),
                                    "identificador " + parameterIdentifier + " ja declarado anteriormente\n");
                        } else {
                            if (defineTypeAndAddtoScope(parameterIdentifier, variableType, procScope)) {
                                defineTypeAndAddtoScope(parameterIdentifier, variableType, procParameters);
                            } else {
                                if (globalScope.exists(variableType) && globalScope.check(
                                        variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                                    if (procScope.exists(parameterIdentifier)) {
                                        LaUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                "identificador " + parameterIdentifier
                                                        + " ja declarado anteriormente\n");
                                    } else {
                                        SymbolTableEntry fields = globalScope.check(variableType);
                                        SymbolTable nestedTableType = fields.argsRegFunc;

                                        procScope.put(parameterIdentifier,
                                                SymbolTable.TypeLAIdentifier.REGISTRO,
                                                SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                variableType);
                                        procParameters.put(parameterIdentifier,
                                                SymbolTable.TypeLAIdentifier.REGISTRO,
                                                SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                variableType);
                                    }
                                }

                                if (!globalScope.exists(variableType)) {
                                    LaUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                            "tipo " + variableType + " nao declarado\n");
                                    procScope.put(parameterIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INVALIDO);
                                    procParameters.put(parameterIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INVALIDO);
                                }
                            }
                        }
                    }
                }
            }

        }

        return super.visitDeclaracao_global(ctx);
    }

    @Override
    public Void visitCmdChamada(CmdChamadaContext ctx) {
        SymbolTable currentScope = nestedScopes.getCurrentScope();
        String identifier = ctx.IDENT().getText();

        if (!currentScope.exists(identifier)) {
            LaUtils.addSemanticError(ctx.IDENT().getSymbol(),
                    "identificador " + identifier + " nao declarado\n");
        } else {
            ArrayList<SymbolTable.TypeLAVariable> parameterTypes = gatherParameterTypes(ctx, currentScope);
            for (ExpressaoContext exp : ctx.expressao()) {
                parameterTypes.add(LaUtils.verifyType(currentScope, exp));
            }
        }

        return super.visitCmdChamada(ctx);
    }

    private ArrayList<SymbolTable.TypeLAVariable> gatherParameterTypes(CmdChamadaContext ctx,
            SymbolTable currentScope) {
        ArrayList<SymbolTable.TypeLAVariable> parameterTypes = new ArrayList<>();

        for (ExpressaoContext exp : ctx.expressao()) {
            SymbolTable.TypeLAVariable type = LaUtils.verifyType(currentScope, exp);
            parameterTypes.add(type);
        }

        return parameterTypes;
    }

    @Override
    public Void visitCmdAtribuicao(CmdAtribuicaoContext ctx) {
        SymbolTable currentScope = nestedScopes.getCurrentScope();
        TypeLAVariable leftValue = LaUtils.verifyType(currentScope, ctx.identificador());
        TypeLAVariable rightValue = LaUtils.verifyType(currentScope, ctx.expressao());

        String[] assignmentParts = ctx.getText().split("<-");
        String leftIdentifier = assignmentParts[0].trim();

        if (!isValidAssignment(leftValue, rightValue, assignmentParts)) {
            LaUtils.addSemanticError(ctx.identificador().IDENT(0).getSymbol(),
                    "atribuicao nao compativel para " + leftIdentifier + "\n");
        }

        return super.visitCmdAtribuicao(ctx);
    }

    private boolean isValidAssignment(TypeLAVariable leftValue, TypeLAVariable rightValue, String[] assignmentParts) {
        boolean isPointerAssignment = assignmentParts[0].contains("^");

        if (isPointerAssignment) {
            return isValidPointerAssignment(leftValue, rightValue, assignmentParts[0]);
        } else {
            return LaUtils.verifyType(leftValue, rightValue);
        }
    }

    private boolean isValidPointerAssignment(TypeLAVariable leftValue, TypeLAVariable rightValue,
            String assignmentPart) {
        if (leftValue == SymbolTable.TypeLAVariable.PONT_INTE && rightValue != SymbolTable.TypeLAVariable.INTEIRO)
            return false;
        if (leftValue == SymbolTable.TypeLAVariable.PONT_LOGI && rightValue != SymbolTable.TypeLAVariable.LOGICO)
            return false;
        if (leftValue == SymbolTable.TypeLAVariable.PONT_REAL && rightValue != SymbolTable.TypeLAVariable.REAL)
            return false;
        if (leftValue == SymbolTable.TypeLAVariable.PONT_LITE && rightValue != SymbolTable.TypeLAVariable.LITERAL)
            return false;

        return true;
    }

    @Override
    public Void visitCmdLeia(CmdLeiaContext ctx) {
        SymbolTable currentScope = nestedScopes.getCurrentScope();
        for (IdentificadorContext ident : ctx.identificador()) {
            LaUtils.verifyType(currentScope, ident);
        }
        return super.visitCmdLeia(ctx);
    }

    @Override
    public Void visitExp_aritmetica(laParser.Exp_aritmeticaContext ctx) {
        SymbolTable currentScope = nestedScopes.getCurrentScope();
        LaUtils.verifyType(currentScope, ctx);
        return super.visitExp_aritmetica(ctx);
    }

    @Override
    public Void visitPrograma(laParser.ProgramaContext ctx) {
        checkReturnCommandsInBody(ctx.corpo());

        for (Decl_local_globalContext ctxDec : ctx.declaracoes().decl_local_global()) {
            checkReturnCommandsInGlobalDeclaration(ctxDec);
        }

        return super.visitPrograma(ctx);
    }

    private void checkReturnCommandsInBody(CorpoContext corpo) {
        for (CmdContext ctxCmd : corpo.cmd()) {
            if (ctxCmd.cmdRetorne() != null) {
                LaUtils.addSemanticError(ctxCmd.cmdRetorne().getStart(),
                        "comando retorne nao permitido nesse escopo\n");
            }
        }
    }

    private void checkReturnCommandsInGlobalDeclaration(Decl_local_globalContext ctxDec) {
        Declaracao_globalContext declGlobal = ctxDec.declaracao_global();

        if (declGlobal != null && declGlobal.tipo_estendido() == null) {
            for (CmdContext ctxCmd : declGlobal.cmd()) {
                if (ctxCmd.cmdRetorne() != null) {
                    LaUtils.addSemanticError(ctxCmd.cmdRetorne().getStart(),
                            "comando retorne nao permitido nesse escopo\n");
                }
            }
        }
    }

    @Override
    // cria e gerencia escopos pra cada bloco de codigo
    public Void visitCorpo(laParser.CorpoContext ctx) {
        List<SymbolTable> scopes = nestedScopes.runNestedScopes();
        if (scopes.size() > 1)
            nestedScopes.giveupScope();
        return super.visitCorpo(ctx);
    }

}
