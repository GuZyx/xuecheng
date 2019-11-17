package com.xuecheng.framework.domain.learning.response;

import com.xuecheng.framework.model.response.ResultCode;

public enum  LearningCode implements ResultCode {
    LEARNING_GETMEDIA_ERROR(false,25000,"获取课程学习媒资信息失败！"),
    CHOOSECOURSE_COURSEISNULL(false,25001,"获取课程信息为空！"),
    CHOOSECOURSE_USERISNULL(false,25002,"获取用户信息为空！"),
    CHOOSECOURSE_TASKISNULL(false,25003,"获取任务信息为空！");

    boolean success;

    int code;

    String message;

    private LearningCode(boolean success, int code, String message){
        this.success = success;
        this.code = code;
        this.message = message;
    }

    @Override
    public boolean success() {
        return false;
    }

    @Override
    public int code() {
        return 0;
    }

    @Override
    public String message() {
        return null;
    }
}
