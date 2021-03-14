
package com.ranhy.example.manatee.gateway.nginx.error;


import com.ranhy.framework.manatee.gateway.common.constants.RespCodeEnum;
import com.ranhy.framework.manatee.gateway.common.exception.CatfishGatewayException;
import com.ranhy.framework.manatee.gateway.common.protocol.Response;
import com.ranhy.framework.manatee.gateway.common.util.JsonUtils;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
@Slf4j
public class CustomErrorController implements ErrorController {

    @Value("${error.path:/error}")
    private String errorPath;

    @Autowired
    private DefaultErrorAttributes defaultErrorAttributes;

    @Override
    public String getErrorPath() {
        return errorPath;
    }

    @RequestMapping
    public void error(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body;
        try {
            RequestAttributes requestAttributes = new ServletRequestAttributes(request);

            RequestContext ctx = RequestContext.getCurrentContext();

            Throwable e= ctx.getThrowable();

            Map<String, Object> errorAttributes = defaultErrorAttributes.getErrorAttributes(requestAttributes, false);

            if(e != null && e.getCause() instanceof CatfishGatewayException){
                CatfishGatewayException gatewayException= (CatfishGatewayException) e.getCause();
                response.setStatus( gatewayException.getHttpStatus());
                errorAttributes.put("status",gatewayException.getHttpStatus());
                errorAttributes.put("error",gatewayException.getMessage());
            }else {
                response.setStatus(getStatus(request).value());
            }

            body= JsonUtils.beanToJson(errorAttributes);
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().write(body);

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

            body = JsonUtils.beanToJson(Response.of(RespCodeEnum.SYS_FAIL.getDescription(), RespCodeEnum.SYS_FAIL.getRespCode()));
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write(body);
        }


    }

    HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }


}
