grammar la;

NUM_INT:    DIGITO+; //numeros inteiros nao tem virgula

NUM_REAL:   DIGITO+ '.' DIGITO+; //numeros reais tem virgula

fragment
DIGITO: ('0'..'9'); //define que digitos so podem ser compostos por numeros

IDENT:    [a-zA-Z][a-zA-Z0-9_]*; //define os tokens aceitos para um identificador, e define que eles nao podem comecar com numero

CADEIA: ('\'' | '"') (ESC_SEQ | ~('\n'|'\''|'\\'|'"'))* ('\'' | '"'); // cadeias devem ser iniciadas por ' ou ", ter o finalizador (ex: \n),
            // e fechar as aspas simples ou dupla para serem validas

CADEIA_NAO_FECHADA:  ('\'' | '"') (ESC_SEQ | ~('\n'|'\''|'\\'|'"'))* '\n'; // uma cadeia nao fechada eh aquela que abre as aspas
           // simples ou dupla, mas nao as fecha

COMENTARIO: '{' ~('}' | '\n' | '\r')*  '}' { skip(); }; //um comentario deve ser iniciado com {, que nao pode conter \n, \r e } dentro dele,
    // e deve ser fechado com }

COMENTARIO_NAO_FECHADO: '{' ~('}' | '\n' | '\r')* ('\r' | '\n'); //um comentario nao fechado eh aquele que abre uma chave, mas nao a fecha

OP_ARIT	:	'*' | '/' | '+' | '-'; //define os tokens que denotam operadores artimeticos

OP_REL	:	'<' | '<=' | '>=' | '>' | '=' | '<>'; //define os tokens que denotam operadores relacionais

OP_COMPR:  '&'; //define os tokens que denotam operadores logicos

DELIM:	':'; //token que denota o fim de um argumento

ATRIB: '<-';

VIRGULA :   ','; //virgula eh so uma virgula


AP:	'('; //abre parenteses

FP:	')'; //fecha parenteses

AC: '['; //abre colchete
FC: ']'; //fecha colchete


fragment
ESC_SEQ:    '\\\''; //aspas e barra

WS: (' '|'\t'|'\r'|'\n') { skip(); };
    // \n pula para a proxima linha
    // \r move o cursor para o comeco da linha
    // \t insere um tab

ERRO: .;

//  Syntax for T2


programa:
    declaracoes 'algoritmo' corpo 'fim_algoritmo';

declaracoes:
    decl_local_global*;

decl_local_global:
    declaracao_local    |   declaracao_global;

declaracao_local:
    'declare' variavel
    |
    'constante' IDENT ':' tipo_basico '=' valor_constante
    |
    'tipo' IDENT ':' tipo
    ;

variavel:
    identificador (',' identificador)* ':' tipo;

identificador:
    IDENT ('.' IDENT)* dimensao;

dimensao:
    ('[' exp_aritmetica ']')*;

tipo:
    registro    |   tipo_estendido;

tipo_basico:
    'literal'   |   'inteiro'   |   'real'  |   'logico';

tipo_basico_ident:
    tipo_basico |   IDENT;

tipo_estendido:
    ('^')? tipo_basico_ident;

valor_constante:
    CADEIA  |   NUM_INT |   NUM_REAL    |   'verdadeiro'    |   'falso';

registro:
    'registro' (variavel)* 'fim_registro';

declaracao_global:
    'procedimento' IDENT '(' (parametros)? ')' declaracao_local* cmd* 'fim_procedimento'
    |
    'funcao' IDENT '(' (parametros)? ')' ':' tipo_estendido declaracao_local* cmd* 'fim_funcao'
    ;
parametro:
    'var'? identificador (',' identificador)* ':' tipo_estendido;

parametros:
    parametro (',' parametro)*;

corpo:
    declaracao_local* cmd*;

cmd:
    cmdLeia |   cmdEscreva  |   cmdSe   |   cmdCaso |   cmdPara |   cmdEnquanto |   cmdFaca |   cmdAtribuicao   |   cmdChamada  |   cmdRetorne;

cmdLeia:
    'leia' '(' '^'? identificador (',' '^'? identificador)* ')';

cmdEscreva:
    'escreva' '(' expressao (',' expressao)* ')';

cmdSe:
    'se'  expressao 'entao' (cmdIf+=cmd)* ('senao' (cmdElse+=cmd)*)? 'fim_se';

cmdCaso:
    'caso' exp_aritmetica 'seja' selecao ('senao' cmd*)? 'fim_caso';

cmdPara:
    'para' IDENT '<-' exp_aritmetica 'ate' exp_aritmetica 'faca' cmd* 'fim_para';

cmdEnquanto:
    'enquanto' expressao 'faca' cmd* 'fim_enquanto';

cmdFaca:
    'faca' (cmd)* 'ate' expressao;

cmdAtribuicao:
    '^'? identificador '<-' expressao;

cmdChamada:
    IDENT '(' expressao (',' expressao)* ')';

cmdRetorne:
    'retorne' expressao;

selecao:
    item_selecao*;

item_selecao:
    constantes ':' cmd*;

constantes:
    numero_intervalo (',' numero_intervalo)*;

numero_intervalo:
    (op_unarioPrimeiro=op_unario)? numeroPrimeiro=NUM_INT ('..' (op_unariosSegundo=op_unario)? numeroSegundo=NUM_INT)?;

op_unario:
    '-';

exp_aritmetica:
    termo (op1 termo)*;

termo:
    fator (op2 fator)*;

fator:
    parcela (op3 parcela)*;

op1:
    '+' |   '-';

op2:
    '*' |   '/';

op3:
    '%';

parcela:
    op_unario? parcela_unario   |   parcela_nao_unario;

parcela_unario:
    '^'? identificador
    |
    IDENT '(' expressao (',' expressao)* ')'
    |
    NUM_INT
    |
    NUM_REAL
    |
    '(' expressao ')'
    ;

parcela_nao_unario:
    '&' identificador   | CADEIA;

exp_relacional:
    exp_aritmetica (op_relacional exp_aritmetica)?;

op_relacional:
    '=' |   '<>'    |   '>='    |   '<='    |   '>' |   '<';

expressao:
    termo_logico (op_logico_1 termo_logico)*;

termo_logico:
    fator_logico (op_logico_2 fator_logico)*;

fator_logico:
    'nao'? parcela_logica;

parcela_logica:
    ('verdadeiro' | 'falso')    |   exp_relacional;

op_logico_1:
    'ou';

op_logico_2:
    'e';