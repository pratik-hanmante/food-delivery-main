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

    // Injecting the AWS S3 client
    private final S3Client s3Client;

    public FoodServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private final FoodRepository foodRepository;


    // Reading the S3 bucket name from application.properties
    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {
        // Extract the original filename
        String originalFilename = file.getOriginalFilename();

        // Validate file name
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // Extract the file extension from the original filename
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);

        // Generate a unique key for the file (UUID + extension)
        String key = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Build the PutObjectRequest with bucket, key, and content type
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            // Upload the file to S3
            PutObjectResponse response = s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes()) // Read file content as bytes
            );

            // If the upload is successful, return the S3 file URL
            if (response.sdkHttpResponse().isSuccessful()) {
                return "https://" + bucketName + ".s3.amazonaws.com/" + key;
            } else {
                // If upload fails, throw 500 error
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed"
                );
            }

        } catch (IOException ex) {
            // Handle IOException (e.g., if reading file bytes fails)
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while uploading the file"
            );
        }
    }

    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
FoodEntity newFoodEntity = convertToEntity(request);
String imageurl = uploadFile(file);
newFoodEntity.setImageUrl(imageurl);
foodRepository.save(newFoodEntity);
    }

    private FoodEntity convertToEntity(FoodRequest request) {
        return FoodEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
    }

}
