package com.sysgears.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInput {
    @NonNull
    private String name;
    @NonNull
    private String email;
    @NonNull
    private String content;
}
