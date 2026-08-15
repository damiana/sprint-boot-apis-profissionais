package br.com.alura.runnercircleapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${runnercircle.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof JacksonJsonHttpMessageConverter jacksonConverter) {
                List<MediaType> tiposSuportados = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                tiposSuportados.add(MediaType.TEXT_PLAIN);
                tiposSuportados.add(MediaType.APPLICATION_OCTET_STREAM);
                jacksonConverter.setSupportedMediaTypes(tiposSuportados);
            }
        }
    }
}
