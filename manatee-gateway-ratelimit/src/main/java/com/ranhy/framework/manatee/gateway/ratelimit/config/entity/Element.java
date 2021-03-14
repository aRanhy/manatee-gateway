package com.ranhy.framework.manatee.gateway.ratelimit.config.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@Validated
public class Element<T> {
    /**
     * 最近使用的时间戳
     */
    @NotNull
    private  long  refreshTime;

    /**
     * 元素有效期
     */
    @NotNull
    @Min(1)
    private  long  expireTime;


    @NotNull
    private  T element;


}
