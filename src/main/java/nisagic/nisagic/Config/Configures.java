package nisagic.nisagic.Config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Configures implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")      //("192.168.0.173:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS");
//                .allowCredentials(true);
    }
}
