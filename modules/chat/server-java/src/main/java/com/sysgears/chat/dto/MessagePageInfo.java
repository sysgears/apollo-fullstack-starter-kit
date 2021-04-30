package com.sysgears.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePageInfo {
    private Integer endCursor;
    private Boolean hasNextPage;
}
