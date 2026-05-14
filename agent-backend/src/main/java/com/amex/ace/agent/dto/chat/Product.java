package com.amex.ace.agent.dto.chat;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Product(String name, String price, String image,
                      String sku, String description) {}
