package jsp.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class Product {

    @JsonProperty("ID")
    private Long id;

    @JsonProperty("CreatedAt")
    private Instant createdAt;

    @JsonProperty("UpdatedAt")
    private Instant updatedAt;

    @JsonProperty("DeletedAt")
    private Instant deletedAt;

    private String name;

    private BigDecimal price;
}