package org.javamaster.b2c.core.controller;

import org.javamaster.b2c.core.model.Result;
import org.javamaster.b2c.core.model.vo.GetExamListReqVo;
import org.javamaster.b2c.core.model.vo.GetExamListResVo;
import org.javamaster.b2c.core.service.IExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author yudong
 * @date 2019/6/10
 */
@RestController
@RequestMapping("/json/exam")
@Validated
public class ExamController {
    @Autowired
    private IExamService examService;

    @PostMapping("/getExamList")
    public Result<List<GetExamListResVo>> getExamList(@Validated @RequestBody GetExamListReqVo reqVo,
                                                      @AuthenticationPrincipal UserDetails userDetails)
            throws IOException {
        List<GetExamListResVo> resVos = examService.getExamList(reqVo, userDetails);
        Result<List<GetExamListResVo>> result = new Result(resVos);
        return result;
    }

    @PostMapping("/getExamListByOpInfo")
    public Result<List<GetExamListResVo>> getExamListByOpInfo( @NotNull Date examOpDate,
                                                              @ModelAttribute("loginUserInfo") UserDetails userDetails) {
        List<GetExamListResVo> resVos = examService.getExamListByOpInfo(examOpDate, userDetails);
        Result<List<GetExamListResVo>> result = new Result(resVos);
        return result;
    }

}
