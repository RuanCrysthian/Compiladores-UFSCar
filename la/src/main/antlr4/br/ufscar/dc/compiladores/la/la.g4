lexer grammar la;

PALAVRA_CHAVE:
    'algoritmo' | 'fim_algoritmo' | 'declare' | 'inteiro' | 'leia' | 'escreva' | 'literal'
    | 'real' | 'logico' | 'se' | 'entao' | 'senao' | 'fim_se' | 'caso'
    | 'seja' | 'fim_caso' | 'para' | 'ate' | 'faca' | 'fim_para' | 'enquanto' | 'fim_enquanto'
    | 'registro' | 'fim_registro' | 'tipo' | 'procedimento' | 'var' | 'fim_procedimento'
    | 'funcao' | 'retorne' | 'fim_funcao' | 'constante' | 'falso' | 'verdadeiro'
    | 'e' | 'ou' | 'nao' | '&' | '<-' | '..' | '%' | '^' | '.';

IDENT:    [a-zA-Z][a-zA-Z0-9_]*;

OPERADOR_ARITMETICO: '+' | '-' | '*' | '/';

OPERADOR_RELACIONAL: '<' | '<=' | '>=' | '>' | '=' | '<>';

OPERADOR_BOOLEANO: 'e' | 'ou' | 'nao' | '&';

DELIMITADOR: ':';

ABRE_PARENTESE: '(';

FECHA_PARENTESE: ')';

VIRGULA: ',';

ABRE_COLCHETE: '[';
FECHA_COLCHETE: ']';

fragment
NUMERO: ('0'..'9');

NUM_INT: NUMERO+;

NUM_REAL: NUMERO+ '.' NUMERO+;

CADEIA: ('\'' | '"') (ESC_SEQ | ~('\n'|'\''|'\\'|'"'))* ('\'' | '"');

fragment
ESC_SEQ:    '\\\'';

COMENTARIO: '{' ~('}' | '\n' | '\r')*  '}' { skip(); }; /

WS: (' '|'\t'|'\r'|'\n') { skip(); };

// Para gerar mensagem de erro personalizada
CADEIA_NAO_FECHADA:  ('\'' | '"') (ESC_SEQ | ~('\n'|'\''|'\\'|'"'))* '\n';
COMENTARIO_NAO_FECHADO: '{' ~('}' | '\n' | '\r')* ('\r' | '\n');
ERRO: .;
