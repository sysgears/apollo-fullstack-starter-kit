package com.sysgears.chat.dto.input;

import lombok.*;

import javax.servlet.http.Part;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMessageInput {
    private String text;
    private Integer userId;
    private String uuid;
    private Integer quotedId;
    private Part attachment;
}
