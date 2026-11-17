package financeiro.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transacao(int id, TipoTransacao tipo, String descricao,
                        String categoria, BigDecimal valor, LocalDate data) {
    public Transacao {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor precisa ser maior que zero.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição não pode ficar vazia.");
        }
        if (categoria == null || categoria.isBlank()) {
            categoria = "Outros";
        }
    }

    public BigDecimal valorComSinal() {
        return tipo == TipoTransacao.RECEITA ? valor : valor.negate();
    }


}
