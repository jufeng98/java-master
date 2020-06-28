package org.javamaster.b2c.core;

import org.javamaster.b2c.core.enums.BizExceptionEnum;
import org.javamaster.b2c.core.exception.BizException;
import org.javamaster.b2c.core.exception.BusinessException;
import org.javamaster.b2c.core.model.AuthUser;
import org.javamaster.b2c.core.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yudong
 * @date 2019/6/10
 */
@RestControllerAdvice
public class GlobalHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalHandler.class);

    @ModelAttribute("loginUserInfo")
    public UserDetails modelAttribute() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return AuthUser.ANONYMOUS_USER;
        }
        return (UserDetails) principal;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> exceptionHandler(MethodArgumentNotValidException e) {
        Result<Void> result = new Result<>(BizExceptionEnum.INVALID_REQ_PARAM.getErrorCode(),
                BizExceptionEnum.INVALID_REQ_PARAM.getErrorMsg());
        logger.error("req params error", e);
        return result;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> exceptionHandler(ConstraintViolationException e) {
        Result<Void> result = new Result<>(BizExceptionEnum.INVALID_REQ_PARAM.getErrorCode(),
                BizExceptionEnum.INVALID_REQ_PARAM.getErrorMsg());
        logger.error("req params error", e);
        return result;
    }

    @ExceptionHandler(BizException.class)
    public Result<Void> exceptionHandler(BizException e) {
        BizExceptionEnum exceptionEnum = e.getBizExceptionEnum();
        Result<Void> result = new Result<>(exceptionEnum.getErrorCode(), exceptionEnum.getErrorMsg());
        logger.error("biz error", e);
        return result;
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> exceptionHandler(BusinessException e) {
        Result<Void> result = new Result<>(e.getErrorCode(), e.getMessage());
        logger.error("business error", e);
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> exceptionHandler(Exception e) {
        Result<Void> result = new Result<>(BizExceptionEnum.APPLICATION_ERROR.getErrorCode(),
                BizExceptionEnum.APPLICATION_ERROR.getErrorMsg());
        logger.error("application error", e);
        return result;
    }

}
