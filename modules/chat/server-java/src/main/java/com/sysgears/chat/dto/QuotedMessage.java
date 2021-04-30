package com.sysgears.chat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuotedMessage {
    private Integer id;
    private String text;
    private String username;
    private String filename;
    private String path;
}
