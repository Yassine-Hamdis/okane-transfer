package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.MessageIntent;
import com.okanetransfer.entity.enums.MessageSender;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageResponse {
    private String        content;
    private MessageSender sender;
    private MessageIntent intent;
    private boolean       escalated;
}