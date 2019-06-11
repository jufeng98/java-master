package org.javamaster.b2c.core.service.impl;

import static java.util.stream.Collectors.toList;
import org.javamaster.b2c.core.entity.MicrowebsiteExam;
import org.javamaster.b2c.core.enums.BizExceptionEnum;
import org.javamaster.b2c.core.exception.BizException;
import org.javamaster.b2c.core.mapper.ManualMicrowebsiteMapper;
import org.javamaster.b2c.core.model.vo.GetExamListReqVo;
import org.javamaster.b2c.core.model.vo.GetExamListResVo;
import org.javamaster.b2c.core.service.IExamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author yudong
 * @date 2019/6/10
 */
@Service
public class IExamServiceImpl implements IExamService {
    @Autowired
    private ManualMicrowebsiteMapper microwebsiteMapper;

    @Override
    public List<GetExamListResVo> getExamList(GetExamListReqVo reqVo, UserDetails userDetails) throws IOException {
        List<MicrowebsiteExam> examEntities = microwebsiteMapper.select(reqVo.getExamType(), userDetails.getUsername());
        // 按照业务的定义要求,此处考试列表必须不为空,一旦为空,则说明后台配置有误或其它未知原因,这种情况视为一种业务异常
        if (examEntities.isEmpty()) {
            // 未查到考试信息,抛出相应的业务异常
            throw new BizException(BizExceptionEnum.EXAM_NOT_FOUND);
        }
        // 此处代码还有其它各类异常抛出......
        List<GetExamListResVo> resVos = examEntities.stream().map(examEntity -> {
            GetExamListResVo resVo = new GetExamListResVo();
            BeanUtils.copyProperties(examEntity, resVo);
            return resVo;
        }).collect(toList());
        return resVos;
    }
}
