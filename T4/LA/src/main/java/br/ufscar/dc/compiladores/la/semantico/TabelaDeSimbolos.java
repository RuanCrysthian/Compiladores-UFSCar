package br.ufscar.dc.compiladores.la.semantico;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabelaDeSimbolos {

	public enum TipoVariavel {
		LITERAL,
		INTEIRO,
		REAL,
		LOGICO,
		INVALIDO,
		PONTEIRO_INTEIRO,
		PONTEIRO_LITERAL,
		PONTEIRO_REAL,
		PONTEIRO_LOGICO,
		REGISTRO,
		TIPO
	}

	public enum TipoEntradaTds {
		TIPO,
		FUNCAO,
		PROCEDIMENTO,
		VARIAVEL,
		REGISTRO
	}

	public final Map<String, EntradaTabelaDeSimbolos> tabela;

	public TabelaDeSimbolos() {
		this.tabela = new HashMap<>();
	}

	public void adicionar(String nome, TipoEntradaTds tipoEntrada, TipoVariavel tipoObjeto) {
		tabela.put(nome, new EntradaTabelaDeSimbolos(nome, tipoEntrada, tipoObjeto));
	}

	public void adicionar(String nome, TipoEntradaTds tipoEntrada, TipoVariavel tipoObjeto,
			List<TabelaDeSimbolos.TipoVariavel> tiposFuncao) {
		tabela.put(nome, new EntradaTabelaDeSimbolos(nome, tipoEntrada, tipoObjeto, tiposFuncao));
	}

	public void adicionar(String nome, EntradaTabelaDeSimbolos etd) {
		tabela.put(nome, etd);
	}

	public boolean existe(String nome) {
		return tabela.containsKey(nome);
	}

	public EntradaTabelaDeSimbolos verificar(String nome) {
		return tabela.get(nome);
	}

}
