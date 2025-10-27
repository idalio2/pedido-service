package io.junior.pedido_service.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@FeignClient(
        name = "notificacaoClient",
        url = "${notificacao.base-url}",
        configuration = FeignConfig.class
)
public interface NotificacaoClient {

    record PedidoNotificacao(@NotBlank @Email String email,
                             @NotBlank @Size(max = 500) String mensagem) {}

    record NotificacaoCriada(String id, String email, String estado, Instant criadoEm) {}

    @PostMapping("/api/notificacoes")
    NotificacaoCriada criar(@RequestBody PedidoNotificacao pedido);
}
