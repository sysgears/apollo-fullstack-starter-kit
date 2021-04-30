package com.sysgears.chat.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditMessageInput {
    @NonNull
    private Integer id;
    private String text;
    private Integer userId;
}
