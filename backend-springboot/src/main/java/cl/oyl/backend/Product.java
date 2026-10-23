package cl.oyl.backend;
import jakarta.persistence.*;
@Entity @Table(name="products") public class Product {
@Id public Integer id;public String name;public String category;public int price;public int stock;
public Product(){} public Product(Integer id,String name,String category,int price,int stock){this.id=id;this.name=name;this.category=category;this.price=price;this.stock=stock;}}
