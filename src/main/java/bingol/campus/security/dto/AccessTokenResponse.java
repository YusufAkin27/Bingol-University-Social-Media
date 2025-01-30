package bingol.campus.security.dto;

import lombok.Data;

@Data
public class AccessTokenResponse {
    private String accessToken;
    public AccessTokenResponse (String accessToken){
        this.accessToken = accessToken;
    }
}
