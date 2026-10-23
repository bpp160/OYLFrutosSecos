package cl.oyl.backend;
import org.springframework.boot.*;import org.springframework.boot.autoconfigure.*;import org.springframework.context.annotation.*;
@SpringBootApplication public class BackendApplication {public static void main(String[] a){SpringApplication.run(BackendApplication.class,a);}
@Bean CommandLineRunner seed(ProductRepository repo){return args->{if(repo.count()==0){repo.save(new Product(1,"Almendras","Frutos secos",2990,25));repo.save(new Product(2,"Nueces","Frutos secos",3490,18));repo.save(new Product(3,"Mix Premium","Frutos secos",3990,40));repo.save(new Product(4,"Pasas","Deshidratados",2490,36));repo.save(new Product(5,"Semillas de chía","Semillas",2990,22));repo.save(new Product(6,"Cranberries","Deshidratados",2890,16));}};}}
