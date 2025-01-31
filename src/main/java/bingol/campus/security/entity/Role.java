package bingol.campus.security.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum Role implements GrantedAuthority {
    ADMIN("ADMIN"),
    STUDENT("STUDENT");


    private final String role;
    @Override
    public String getAuthority() {
        return role;
    }
}
