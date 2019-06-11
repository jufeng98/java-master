package org.javamaster.b2c.core.service;

import org.javamaster.b2c.core.model.vo.GetExamListReqVo;
import org.javamaster.b2c.core.model.vo.GetExamListResVo;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

/**
 * @author yudong
 * @date 2019/6/10
 */
public interface IExamService {
    List<GetExamListResVo> getExamList(GetExamListReqVo reqVo, UserDetails userDetails) throws IOException;
}
