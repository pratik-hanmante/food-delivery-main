package in.pratikdh.foodiesapi.service;

import in.pratikdh.foodiesapi.io.FoodRequest;
import in.pratikdh.foodiesapi.io.FoodResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FoodService {
    String uploadFile(MultipartFile file);

   FoodResponse addFood(FoodRequest request, MultipartFile file);

}
