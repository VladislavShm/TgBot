package com.tgbot.converter.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.entity.LocationAttribute;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Converter
public class LocationAttributeMapConverter implements AttributeConverter<Map<LocationAttribute, String>, byte[]> {

    @Override
    @SneakyThrows
    public byte[] convertToDatabaseColumn(Map<LocationAttribute, String> customerInfo) {
        if (customerInfo == null) {
            return new byte[0];
        }

        return new ObjectMapper().writeValueAsString(customerInfo).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @SneakyThrows
    public Map<LocationAttribute, String> convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length == 0) {
            return new HashMap<>();
        }

        return ((Map<String, String>) new ObjectMapper().readValue(new String(dbData), HashMap.class))
                .entrySet().stream().collect(Collectors.toMap(
                        entry -> Enum.valueOf(LocationAttribute.class, entry.getKey()),
                        Map.Entry::getValue
                ));
    }

}