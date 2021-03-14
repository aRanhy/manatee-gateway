
package com.ranhy.framework.manatee.gateway.ratelimit.config.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rate {

    private String key;
    private Long remaining;
    private Long reset;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private Date expiration;
}
 