package application.infrastructure.models;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class ApplicationConfiguration {

    @Value("${specificationsFolder}")
    public String specificationsFolder;
}
