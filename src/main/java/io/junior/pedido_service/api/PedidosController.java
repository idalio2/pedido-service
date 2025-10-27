package io.junior.pedido_service.api;

import io.junior.pedido_service.infra.NotificacaoClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/pedidos")
public class PedidosController {

    public record NovoPedido(
            @NotBlank String descricao,
            @Positive double valor,
            @NotBlank @Email String emailCliente
    ) {}

    public record PedidoCriado(
            String idPedido,
            String descricao,
            double valor,
            Instant criadoEm,
            String idNotificacao
    ) {}

    private final Map<String, PedidoCriado> banco = new ConcurrentHashMap<>();
    private final NotificacaoClient notificacoes;

    public PedidosController(NotificacaoClient notificacoes) {
        this.notificacoes = notificacoes;
    }

    @PostMapping
    public ResponseEntity<PedidoCriado> criar(@Valid @RequestBody NovoPedido dto) {
        // 1) Cria o pedido localmente (em memória para fins didáticos)
        var idPedido = UUID.randomUUID().toString();

        // 2) Dispara uma notificação no outro serviço (consumo de saída do notificacao-service)
        var mensagem = "Seu pedido \"" + dto.descricao() + "\" no valor de " + dto.valor() + " foi recebido.";
        var notif = notificacoes.criar(new NotificacaoClient.PedidoNotificacao(dto.emailCliente(), mensagem));

        // 3) Persiste localmente já amarrando o id da notificação retornado pelo outro serviço
        var criado = new PedidoCriado(idPedido, dto.descricao(), dto.valor(), Instant.now(), notif.id());
        banco.put(idPedido, criado);

        return ResponseEntity.created(URI.create("/api/pedidos/" + idPedido)).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoCriado> byId(@PathVariable String id) {
        var p = banco.get(id);
        return (p == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
    }

    @GetMapping("/health")
    public String health() { return "OK"; }
}
