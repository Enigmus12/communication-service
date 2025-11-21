package co.edu.escuelaing.uplearn.chat.dto;

import lombok.Data;
import java.util.List;

@Data
public class RolesResponse {
    private String id;
    private String email;
    private String name;
    private List<String> roles;
    private boolean hasRoles;
    private String lastUpdated;
}
