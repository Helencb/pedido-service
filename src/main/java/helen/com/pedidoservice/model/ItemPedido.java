package helen.com.pedidoservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Setter
@Getter
public class ItemPedido {

    @Id
    @GeneratedValue
    private UUID id;
    private UUID produtoId;
    private String nome;
    private Integer quantidade;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;
}
