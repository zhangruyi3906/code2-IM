package com.lh.im.platform.interceptor;

import cn.hutool.core.util.StrUtil;
import com.lh.im.common.enums.IMTerminalType;
import com.lh.im.platform.config.JwtProperties;
import com.lh.im.platform.session.UserSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@AllArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

//    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/im")) {
            // 存放session
            UserSession userSession = new UserSession();
            String userAccount = request.getHeader("userAccount");
            if (StringUtils.isBlank(userAccount)) {
                throw new RuntimeException("need userAccount. Please check.");
            }
            userSession.setUserAccount(userAccount);
            String terminalStr = request.getHeader("terminal");
            int terminal = Integer.parseInt(terminalStr);
            if (IMTerminalType.fromCode(terminal) == null) {
                throw new RuntimeException("need terminal. Please check.");
            }
            userSession.setTerminal(terminal);
            request.setAttribute("session", userSession);
            return true;
        }
        return true;
    }
}
