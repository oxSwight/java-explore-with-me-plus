package ru.practicum.explore.global.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeService {
    public static HttpHeaders exchange(Object obj) throws IOException {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.findAndRegisterModules();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = objMapper.getFactory().createGenerator(byteArrayOutputStream);
        objMapper.writeValue(jsonGenerator, obj);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(byteArrayOutputStream.size()));
        return httpHeaders;
    }
}
