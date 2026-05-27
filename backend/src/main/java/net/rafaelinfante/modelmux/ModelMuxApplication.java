package net.rafaelinfante.modelmux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ModelMuxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelMuxApplication.class, args);
    }
}
