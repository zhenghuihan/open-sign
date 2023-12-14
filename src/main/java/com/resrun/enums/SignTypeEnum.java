package com.resrun.enums;

/**
 * @Description: 签署方式枚举
 * @Package: com.resrun.enums
 * @ClassName: SignTypeEnum
 * @copyright 北京资源律动科技有限公司
 */
public enum SignTypeEnum {


    POSITION(1,"位置签署"),
    KEYWORD(2,"关键字签署"),

    ;

    private String msg;
    private Integer code;


    SignTypeEnum(Integer code,String msg){
        this.msg = msg;
        this.code = code;
    }

    public Integer getCode(){
        return code;
    }
    public String getMsg(){
        return msg;
    }


}