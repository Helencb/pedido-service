package helen.com.pedidoservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Setter
@Getter
public class ItemPedido {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "produto_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID produtoId;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;
}
