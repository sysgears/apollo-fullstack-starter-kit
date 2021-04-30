package com.sysgears.chat.dto;

import com.sysgears.chat.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagePayload {
    @NonNull
    private Integer id;
    private String text;
    private Integer userId;
    private String createdAt;
    private String username;
    private String uuid;
    private Integer quotedId;
    private String filename;
    private String path;
    private QuotedMessage quotedMessage;

    public static MessagePayload from(Message message) {
        QuotedMessage quotedMessage = new QuotedMessage();
        if (message.getQuoted() != null) {
            quotedMessage.setId(message.getQuoted().getId());
            quotedMessage.setText(message.getQuoted().getText());
            if (message.getQuoted().getUser() != null) {
                quotedMessage.setUsername(message.getQuoted().getUser().getUsername());
            }
            if (message.getQuoted().getAttachment() != null) {
                quotedMessage.setFilename(message.getQuoted().getAttachment().getName());
                quotedMessage.setPath(message.getQuoted().getAttachment().getPath());
            }
        }
        return new MessagePayload(
                message.getId(),
                message.getText(),
                message.getUser() != null ? message.getUser().getId() : null,
                message.getCreatedAt().toString(),
                message.getUser() != null ? message.getUser().getUsername() : null,
                message.getUuid().toString(),
                quotedMessage.getId(),
                message.getAttachment() != null ? message.getAttachment().getName() : null,
                message.getAttachment() != null ? message.getAttachment().getPath() : null,
                quotedMessage
        );
    }
}
