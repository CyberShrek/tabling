package org.vniizt.tabling.config;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Alexander Ilyin
 */

@ControllerAdvice
public class ExceptionHandling {
    @ExceptionHandler
    public void common(HttpServletResponse response, Exception exception) throws IOException {
        System.out.println(Arrays.toString(exception.getStackTrace()));
        response.setStatus(500);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.getWriter().write(exception.getMessage());
        response.getWriter().flush();
    }
}