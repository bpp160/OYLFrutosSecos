package cl.oyl.backend;
import org.springframework.web.bind.annotation.*;import org.springframework.http.*;import org.springframework.transaction.annotation.Transactional;import java.util.*;
@RestController @RequestMapping("/api") public class ApiController {
 private final ProductRepository products;private final OrderRepository orders;
 public ApiController(ProductRepository products,OrderRepository orders){this.products=products;this.orders=orders;}
 public record OrderItemDto(int productId,int quantity){} public record CreateOrderDto(long clientId,String delivery,List<OrderItemDto> items){}
 public record OrderDto(long id,String status,int total){} public record StatusDto(String status){} public record StockDto(int delta){}
 public record ServerOrderDto(long id,long clientId,String delivery,String status,int total){}
 @GetMapping("/orders") public List<ServerOrderDto> listOrders(){return orders.findAll().stream().map(o->new ServerOrderDto(o.id,o.clientId,o.delivery,o.status,o.total)).toList();}
 @GetMapping("/products") public List<Product> products(){return products.findAll();}
 @PostMapping("/products") @Transactional public Product saveProduct(@RequestBody Product req){
  if(req.id==null||req.id<=0||req.name==null||req.name.isBlank()||req.price<0||req.stock<0)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Producto inválido");
  var product=products.findById(req.id).orElseGet(Product::new);
  product.id=req.id;product.name=req.name;product.category=req.category;product.price=req.price;product.stock=req.stock;
  return products.save(product);
 }
 @GetMapping("/products/{id}") public Product product(@PathVariable int id){return products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Producto no encontrado"));}
 @PostMapping("/orders") @Transactional public OrderDto place(@RequestBody CreateOrderDto req){
  if(req.items()==null||req.items().isEmpty()||req.clientId()<=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Pedido inválido");
  var quantities=new TreeMap<Integer,Integer>();for(var item:req.items()){
   if(item.quantity()<=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cantidad inválida");
   quantities.merge(item.productId(),item.quantity(),(a,b)->Math.addExact(a,b));}
  long total=0;var locked=new ArrayList<Product>();
  for(var item:quantities.entrySet()){var product=products.locked(item.getKey()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Producto no encontrado"));
   if(product.stock<item.getValue())throw new ResponseStatusException(HttpStatus.CONFLICT,"Stock insuficiente: "+product.name);
   total+= (long)product.price*item.getValue();locked.add(product);}
  if(total>Integer.MAX_VALUE)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Monto excesivo");
  for(var product:locked)product.stock-=quantities.get(product.id);
  var saved=orders.save(new OrderEntity(req.clientId(),req.delivery(),"Pendiente de validación",(int)total));
  return new OrderDto(saved.id,saved.status,saved.total);
 }
 @PatchMapping("/products/{id}/stock") @Transactional public Product stock(@PathVariable int id,@RequestBody StockDto dto){
  var product=products.locked(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  long next=(long)product.stock+dto.delta();if(next<0||next>Integer.MAX_VALUE)throw new ResponseStatusException(HttpStatus.CONFLICT,"Stock inválido");
  product.stock=(int)next;return product;
 }
 @PatchMapping("/orders/{id}/status") @Transactional public OrderDto status(@PathVariable long id,@RequestBody StatusDto dto){
  var order=orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!List.of("Pendiente de validación","Confirmado","Preparado","Entregado").contains(dto.status()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Estado inválido");
  order.status=dto.status();return new OrderDto(order.id,order.status,order.total);
 }
}
