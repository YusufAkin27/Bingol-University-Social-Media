package bingol.campus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Tüm endpointlere izin ver
                        .allowedOrigins("http://localhost:5174") // Sadece bu frontend erişebilir
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Desteklenen HTTP metodları
                        .allowedHeaders("*") // Tüm başlıklara izin ver
                        .allowCredentials(true); // Kimlik doğrulama bilgilerini (cookies, authorization headers) kabul et
            }
        };
    }
}
