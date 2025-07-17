package in.pratikdh.foodiesapi.service;

import in.pratikdh.foodiesapi.entity.FoodEntity;
import in.pratikdh.foodiesapi.io.FoodRequest;
import in.pratikdh.foodiesapi.io.FoodResponse;
import in.pratikdh.foodiesapi.repository.FoodRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FoodServiceImpl implements FoodService {

    // AWS S3 client used for interacting with the S3 bucket
    private final S3Client s3Client;

    // Repository to interact with the database
    private final FoodRepository foodRepository;

    // Fetching the S3 bucket name from application.properties
    @Value("${aws.s3.bucketName}")
    private String bucketName;

    // Constructor injection (Redundant due to @AllArgsConstructor, but provided explicitly)
    public FoodServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Uploads a file to AWS S3 and returns the file URL
     * @param file The multipart file to upload
     * @return The public URL of the uploaded file
     */
    @Override
    public String uploadFile(MultipartFile file) {
        // Extract the original filename
        String originalFilename = file.getOriginalFilename();

        // Validate the file name to ensure it has an extension
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // Extract the file extension (e.g., jpg, png)
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);

        // Generate a unique key for the file using UUID
        String key = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Create a PutObjectRequest with bucket name, key, and content type
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            // Upload the file to S3 with the request and file bytes
            PutObjectResponse response = s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );

            // Check if the upload was successful
            if (response.sdkHttpResponse().isSuccessful()) {
                // Return the public URL of the uploaded file
                return "https://" + bucketName + ".s3.amazonaws.com/" + key;
            } else {
                // Throw error if upload failed
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed"
                );
            }

        } catch (IOException ex) {
            // Handle error if reading file bytes fails
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while uploading the file"
            );
        }
    }

    /**
     * Adds a new food item to the database with an image
     * @param request The food data (name, description, category, price)
     * @param file The image file to upload
     * @return The created food item (not implemented in current code)
     */
    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
        // Convert incoming request to an entity
        FoodEntity newFoodEntity = convertToEntity(request);

        // Upload the image and set its URL in the entity
        String imageurl = uploadFile(file);
        newFoodEntity.setImageUrl(imageurl);

        // Save the food item to the database
        foodRepository.save(newFoodEntity);

        // ❌ Missing return statement – should return a FoodResponse object
        return null; // You should implement and return FoodResponse here
    }

    /**
     * Converts a FoodRequest DTO to a FoodEntity for persistence
     * @param request The input food request
     * @return A FoodEntity ready for database insertion
     */
    private FoodEntity convertToEntity(FoodRequest request) {
        return FoodEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
    }
}
