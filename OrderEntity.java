package cl.oyl.backend;
import jakarta.persistence.*;
@Entity @Table(name="orders") public class OrderEntity {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;public long clientId;public String delivery;public String status;public int total;
public OrderEntity(){}public OrderEntity(long clientId,String delivery,String status,int total){this.clientId=clientId;this.delivery=delivery;this.status=status;this.total=total;}}
