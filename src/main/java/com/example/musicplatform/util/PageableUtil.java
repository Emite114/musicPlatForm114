package com.example.musicplatform.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableUtil {
    //歌曲的playcount 约等于帖子的viewcount
    public static Pageable initializePageable(int page, int size, String sort) {
        Pageable pageable;
        if ("like".equals(sort) || sort == null) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("likeCount"),
                    Sort.Order.desc("createTime")));
        } else if ("time".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("createTime"),
                    Sort.Order.desc("likeCount")
            ));
        }else if("favourite".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("favouriteCount"),
                    Sort.Order.desc("createTime")));
        }else if("comment".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("commentCount"),
                    Sort.Order.desc("createTime")));
        }else if("hotScore".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("hotScore"),
                    Sort.Order.desc("createTime")));
        }else if("viewCount".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("createTime")));
        }
        else {
            throw new RuntimeException("未知的排序");
        }
        return pageable;
    }
    public static Pageable initializeSongPageable(int page, int size, String sort) {
        Pageable pageable;
        if ( sort == null) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("favouriteCount"),
                    Sort.Order.desc("createTime")));
        } else if ("time".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("createTime"),
                    Sort.Order.desc("favouriteCount")
            ));
        }else if("favourite".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("favouriteCount"),
                    Sort.Order.desc("createTime")));
        }else if("comment".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("commentCount"),
                    Sort.Order.desc("createTime")));
        }else if("hotScore".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc("hotScore"),
                    Sort.Order.desc("createTime")));

        }
        else {
            throw new RuntimeException("未知的排序");
        }
        return pageable;
    }
}
