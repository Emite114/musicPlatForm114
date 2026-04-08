package com.example.musicplatform.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class SongCommentCreateRequest {
    //userid 从securityContext中解析
    @NotNull(message = "歌曲id不能为空")
    Long songId;

    @NotNull(message = "评论内容不能为空")
    @Size(max=2000,message = "评论不能超过2000字")
    String content;

    Long parentId;
    Long replyToUserId;
}
