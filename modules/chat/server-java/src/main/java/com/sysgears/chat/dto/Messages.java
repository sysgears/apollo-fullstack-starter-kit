package com.sysgears.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Messages {
    private Long totalCount;
    private List<MessageEdges> edges;
    private MessagePageInfo pageInfo;
}
