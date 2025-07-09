package in.pratikdh.foodiesapi.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodResponse {
    private String id;
    private String imageUrl;
    private String name;
    private String description;
    private double price;
    private String category;
}
