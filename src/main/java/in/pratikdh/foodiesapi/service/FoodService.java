package in.pratikdh.foodiesapi.service;

import org.springframework.web.multipart.MultipartFile;

public interface FoodService {
    public String uploadFile(MultipartFile file);
}
