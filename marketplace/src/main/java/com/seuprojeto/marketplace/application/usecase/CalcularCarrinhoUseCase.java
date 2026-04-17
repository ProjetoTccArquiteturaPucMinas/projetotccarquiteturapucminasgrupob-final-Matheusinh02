package com.seuprojeto.marketplace.application.usecase;

import com.seuprojeto.marketplace.application.dto.SelecaoCarrinho;
import com.seuprojeto.marketplace.domain.model.CategoriaProduto;
import com.seuprojeto.marketplace.domain.model.Produto;
import com.seuprojeto.marketplace.domain.model.ResumoCarrinho;
import com.seuprojeto.marketplace.domain.repository.ProdutoRepositorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalcularCarrinhoUseCase {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal DESCONTO_MAXIMO_PERCENTUAL = new BigDecimal("25");

    private final ProdutoRepositorio produtoRepositorio;

    public CalcularCarrinhoUseCase(ProdutoRepositorio produtoRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
    }

    public ResumoCarrinho executar(List<SelecaoCarrinho> selecaoCarrinhos) {
        if (selecaoCarrinhos == null || selecaoCarrinhos.isEmpty()) {
            return new ResumoCarrinho(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItens = 0;
        BigDecimal descontoCategoriaPercentual = BigDecimal.ZERO;

        for (SelecaoCarrinho selecao : selecaoCarrinhos) {
            if (selecao == null || selecao.getIdProduto() == null || selecao.getQuantidade() == null || selecao.getQuantidade() <= 0) {
                continue;
            }

            Produto produto = produtoRepositorio.findById(selecao.getIdProduto()).orElse(null);
            if (produto == null) {
                continue;
            }

            int quantidade = selecao.getQuantidade();
            totalItens += quantidade;

            subtotal = subtotal.add(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));
            descontoCategoriaPercentual = descontoCategoriaPercentual
                    .add(descontoCategoria(produto.getCategoriaProduto()).multiply(BigDecimal.valueOf(quantidade)));
        }

        BigDecimal descontoQuantidadePercentual = descontoQuantidade(totalItens);
        BigDecimal descontoPercentual = descontoQuantidadePercentual.add(descontoCategoriaPercentual);

        if (descontoPercentual.compareTo(DESCONTO_MAXIMO_PERCENTUAL) > 0) {
            descontoPercentual = DESCONTO_MAXIMO_PERCENTUAL;
        }

        BigDecimal desconto = subtotal
                .multiply(descontoPercentual)
                .divide(CEM, 2, RoundingMode.HALF_UP);

        return new ResumoCarrinho(
                subtotal,
                desconto
        );
    }

    private BigDecimal descontoQuantidade(int totalItens) {
        if (totalItens >= 4) {
            return new BigDecimal("10");
        }
        if (totalItens == 3) {
            return new BigDecimal("7");
        }
        if (totalItens == 2) {
            return new BigDecimal("5");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal descontoCategoria(CategoriaProduto categoriaProduto) {
        if (categoriaProduto == null) {
            return BigDecimal.ZERO;
        }

        return switch (categoriaProduto) {
            case CAPINHA, FONE -> new BigDecimal("3");
            case CARREGADOR -> new BigDecimal("5");
            case PELICULA, SUPORTE -> new BigDecimal("2");
        };
    }
}