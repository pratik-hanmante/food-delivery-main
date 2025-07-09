package in.pratikdh.foodiesapi.repository;

import in.pratikdh.foodiesapi.entity.FoodEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FoodRepository extends MongoRepository<FoodEntity, String> {
}
