package jsp.springboot.dto;


import lombok.Data;

import java.time.Instant;

@Data
public class Tutorial {

    private Long id;

    private String title;

    private String description;

    private Boolean published;

    private Instant createdAt;

    private Instant updatedAt;
}