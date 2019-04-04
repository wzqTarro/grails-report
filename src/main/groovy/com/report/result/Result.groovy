package com.report.result

import com.report.enst.ResultEnum

class Result {
    Integer code = ResultEnum.SUCCESS.getCode()
    String message = ResultEnum.SUCCESS.getMessage()
    String errorCode
    Integer errcode

    static Result success() {
        Result result = new Result();
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setMessage(ResultEnum.SUCCESS.getMessage());
        return result;
    }

    static Result error() {
        Result result = new Result();
        result.setCode(ResultEnum.PARAM_ERROR.getCode());
        result.setMessage(ResultEnum.PARAM_ERROR.getMessage());
        return result;
    }

    static Result error(String msg) {
        Result result = new Result();
        result.setCode(ResultEnum.OTHER_ERROR.getCode());
        result.setMessage(msg);
        return result;
    }

    void setError(String msg) {
        this.code = ResultEnum.OTHER_ERROR.getCode()
        this.message = msg;
    }
}
