package com.cosmos.diseaseforecasterpro.interceptors;

import com.cosmos.diseaseforecasterpro.common.ErrorCode;
import com.cosmos.diseaseforecasterpro.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import static com.cosmos.diseaseforecasterpro.service.impl.UserServiceImpl.USER_LOGIN_STATE;


@Slf4j
public class AuthInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            log.error("未登录");
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
