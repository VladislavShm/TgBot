package com.tgbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NftMetadata {
    private String name;
    private String description;
    private String image;
    private List<Attribute> attributes;

    @Data
    public static class Attribute {
        @JsonProperty("trait_type")
        private String traitType;
        private String value;
    }

}
