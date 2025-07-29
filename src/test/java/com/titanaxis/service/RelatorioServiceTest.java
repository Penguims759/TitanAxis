package com.titanaxis.service;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.*;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.util.I18n;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RelatorioServiceTest {

    @Test
    void gerarRelatorioInventario_builds_csv() throws PersistenciaException {
        ProdutoRepository prodRepo = mock(ProdutoRepository.class);
        VendaRepository vendaRepo = mock(VendaRepository.class);
        AuditoriaRepository auditRepo = mock(AuditoriaRepository.class);
        TransactionService txService = mock(TransactionService.class);
        RelatorioService service = new RelatorioService(prodRepo, vendaRepo, auditRepo, txService);

        EntityManager em = mock(EntityManager.class);
        Categoria cat = new Categoria(1, "Eletronicos");
        Produto p = new Produto();
        p.setId(1);
        p.setNome("Notebook");
        p.setPreco(10.0);
        p.setCategoria(cat);
        Lote l = new Lote();
        l.setQuantidade(2);
        p.setLotes(List.of(l));
        List<Produto> produtos = List.of(p);

        when(txService.executeInTransactionWithResult(any())).thenAnswer(inv -> {
            Function<EntityManager, List<Produto>> func = inv.getArgument(0);
            return func.apply(em);
        });
        when(prodRepo.findAllIncludingInactive(em)).thenReturn(produtos);

        String csv = service.gerarRelatorioInventario();

        String header = I18n.getString("report.inventory.header.id") + ";" +
                I18n.getString("report.inventory.header.name") + ";" +
                I18n.getString("report.inventory.header.category") + ";" +
                I18n.getString("report.inventory.header.totalQty") + ";" +
                I18n.getString("report.inventory.header.unitPrice") + ";" +
                I18n.getString("report.inventory.header.totalValue") + "\n";
        assertEquals(header + "1;\"Notebook\";\"Eletronicos\";2;10.00;20.00\n", csv);
        verify(prodRepo).findAllIncludingInactive(em);
    }

    @Test
    void gerarRelatorioVendas_builds_csv() throws PersistenciaException {
        ProdutoRepository prodRepo = mock(ProdutoRepository.class);
        VendaRepository vendaRepo = mock(VendaRepository.class);
        AuditoriaRepository auditRepo = mock(AuditoriaRepository.class);
        TransactionService txService = mock(TransactionService.class);
        RelatorioService service = new RelatorioService(prodRepo, vendaRepo, auditRepo, txService);

        EntityManager em = mock(EntityManager.class);
        Usuario user = new Usuario();
        user.setNomeUsuario("seller");
        Cliente cliente = new Cliente();
        cliente.setNome("John");
        Venda venda = new Venda();
        venda.setId(1);
        venda.setUsuario(user);
        venda.setCliente(cliente);
        venda.setDataVenda(LocalDateTime.of(2024,1,1,10,0));
        venda.setValorTotal(30.0);
        List<Venda> vendas = List.of(venda);

        when(txService.executeInTransactionWithResult(any())).thenAnswer(inv -> {
            Function<EntityManager, List<Venda>> func = inv.getArgument(0);
            return func.apply(em);
        });
        when(vendaRepo.findAll(em)).thenReturn(vendas);

        String csv = service.gerarRelatorioVendas();

        String header = I18n.getString("report.sales.header.id") + ";" +
                I18n.getString("report.sales.header.date") + ";" +
                I18n.getString("report.sales.header.client") + ";" +
                I18n.getString("report.sales.header.user") + ";" +
                I18n.getString("report.sales.header.totalValue") + "\n";
        assertTrue(csv.startsWith(header));
        assertTrue(csv.contains("\"John\""));
        assertTrue(csv.contains("\"seller\""));
        verify(vendaRepo).findAll(em);
    }

    @Test
    void gerarRelatorioAuditoriaCsv_quotes_values() {
        RelatorioService service = new RelatorioService(null, null, null, null);

        Vector<Object> row = new Vector<>();
        row.add("He said \"Hello\"");
        row.add(5);
        String[] headers = {"h1","h2"};

        String csv = service.gerarRelatorioAuditoriaCsv(List.of(row), headers);

        assertEquals("h1;h2\n\"He said \"\"Hello\"\"\";\"5\"\n", csv);
    }
}
