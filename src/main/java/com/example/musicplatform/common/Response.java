package com.example.musicplatform.common;

import lombok.Data;

@Data
public class Response<T> {
    Integer code;
    T data;
    String msg;

    public Response(Integer code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static <A> Response<A> of(Integer code,A data,String msg){
        return new Response<>(code,data,msg);
    }

    public static <A> Response<A> success(A data,String msg){
        return Response.of(0,data,msg);
    }


    public static <A> Response<?> error(A data,String msg){
        return Response.of(-1, data,msg);
    }
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
