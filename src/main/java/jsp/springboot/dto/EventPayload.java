package jsp.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventPayload {

    private String eventId;
    private String eventType;
    private Integer recordId;
    private Object data;
}