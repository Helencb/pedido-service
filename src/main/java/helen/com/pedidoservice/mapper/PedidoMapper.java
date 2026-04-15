package helen.com.pedidoservice.mapper;

import helen.com.pedidoservice.dto.PedidoCreateDTO;
import helen.com.pedidoservice.dto.PedidoResponseDTO;
import helen.com.pedidoservice.model.ItemPedido;
import helen.com.pedidoservice.model.Pedido;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PedidoMapper {

    public Pedido toEntity(PedidoCreateDTO dto){
        Pedido pedido = new Pedido();
        pedido.setClienteId(dto.clienteId());

        List<ItemPedido> itens = dto.itens().stream().map(i -> {
            ItemPedido item = new ItemPedido();
            item.setProdutoId(i.produtoId());
            item.setNome(i.nome());
            item.setQuantidade(i.quantidade());
            return item;
        }).toList();
        pedido.setItens(itens);
        return pedido;
    }

   public PedidoResponseDTO toDTO(Pedido pedido){
        List<String> itens = pedido.getItens().stream()
                .map(i -> i.getNome() + " x" + i.getQuantidade())
                .toList();
        return new PedidoResponseDTO(pedido.getId(), pedido.getClienteId(), itens);
   }
}
