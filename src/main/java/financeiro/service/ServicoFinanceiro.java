package financeiro.service;

import financeiro.model.TipoTransacao;
import financeiro.model.Transacao;
import financeiro.repository.RepositorioTransacoes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class ServicoFinanceiro {

    private final RepositorioTransacoes repositorio;
    private final ArrayList<Transacao> transacoes;

    public ServicoFinanceiro(RepositorioTransacoes repositorio) {
        this.repositorio = repositorio;
        this.transacoes = new ArrayList<>(repositorio.carregar());
    }

    public Transacao adicionar(TipoTransacao tipo, String descricao, String categoria, BigDecimal valor, LocalDate data) {
        int proximoId = transacoes.stream().mapToInt(Transacao::id).max().orElse(0) + 1;
        Transacao nova = new Transacao(proximoId, tipo, descricao.trim(),
                padronizarCategoria(categoria), valor, data);
        transacoes.add(nova);
        repositorio.salvar(transacoes);
        return nova;
    }

    public boolean remover(int id) {
        boolean removeu = transacoes.removeIf(t -> t.id() == id);
        if (removeu) {
            repositorio.salvar(transacoes);
        }
        return removeu;
    }

    public List<Transacao> listarDoMes(YearMonth mes) {
        return transacoes.stream()
                .filter(t -> YearMonth.from(t.data()).equals(mes))
                .sorted(Comparator.comparing(Transacao::data))
                .toList();
    }

    public BigDecimal total(List<Transacao> lista, TipoTransacao tipo) {
        return lista.stream()
                .filter(t -> t.tipo() == tipo)
                .map(Transacao::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal saldoGeral() {
        return transacoes.stream()
                .map(Transacao::valorComSinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    public Map<String, BigDecimal> despesasPorCategoria(YearMonth mes) {
        Map<String, BigDecimal> resultado = new TreeMap<>();
        for (Transacao t : listarDoMes(mes)) {
            if (t.tipo() == TipoTransacao.DESPESA) {
                BigDecimal somaAtual = resultado.getOrDefault(t.categoria(), BigDecimal.ZERO);
                resultado.put(t.categoria(), somaAtual.add(t.valor()));
            }
        }
        return resultado;
    }


    private String padronizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) return "Outros";
        String c = categoria.trim().toLowerCase();
        return Character.toUpperCase(c.charAt(0)) + c.substring(1);
    }

}

