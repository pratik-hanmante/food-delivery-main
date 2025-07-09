package in.pratikdh.foodiesapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collation = "foods")
public class FoodEntity {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
}
