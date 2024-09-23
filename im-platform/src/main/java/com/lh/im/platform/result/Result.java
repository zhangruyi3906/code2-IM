package com.lh.im.platform.result;

import lombok.Data;

@Data
public class Result<T> {

    private int status;

    private String message;

    private T data;

}
