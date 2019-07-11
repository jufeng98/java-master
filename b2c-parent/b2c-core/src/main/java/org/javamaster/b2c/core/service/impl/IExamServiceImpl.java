package org.javamaster.b2c.core.service.impl;

import cn.com.bluemoon.handypoi.excel.enums.ExcelType;
import cn.com.bluemoon.handypoi.excel.resolve.ExcelWriter;
import cn.com.bluemoon.handypoi.excel.resolve.SheetInfo;
import static java.util.stream.Collectors.toList;
import org.javamaster.b2c.core.entity.MicrowebsiteExam;
import org.javamaster.b2c.core.entity.MicrowebsiteExamExample;
import org.javamaster.b2c.core.enums.BizExceptionEnum;
import org.javamaster.b2c.core.enums.ExamStatusEnum;
import org.javamaster.b2c.core.enums.ExamTypeEnum;
import org.javamaster.b2c.core.exception.BizException;
import org.javamaster.b2c.core.mapper.ManualMicrowebsiteMapper;
import org.javamaster.b2c.core.mapper.MicrowebsiteExamMapper;
import org.javamaster.b2c.core.model.vo.ExportExamResVo;
import org.javamaster.b2c.core.model.vo.GetExamListReqVo;
import org.javamaster.b2c.core.model.vo.GetExamListResVo;
import org.javamaster.b2c.core.service.IExamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author yudong
 * @date 2019/6/10
 */
@Service
public class IExamServiceImpl implements IExamService {
    @Autowired
    private ManualMicrowebsiteMapper microwebsiteMapper;
    @Autowired
    private MicrowebsiteExamMapper examMapper;

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

    @Override
    public List<GetExamListResVo> getExamListByOpInfo(Date examOpDate, UserDetails userDetails) {
        List<MicrowebsiteExam> examEntities = microwebsiteMapper.selectExamListByOpInfo(examOpDate, userDetails.getUsername());
        List<GetExamListResVo> resVos = examEntities.stream().map(examEntity -> {
            GetExamListResVo resVo = new GetExamListResVo();
            BeanUtils.copyProperties(examEntity, resVo);
            return resVo;
        }).collect(toList());
        return resVos;
    }

    @Override
    public byte[] exportExam() {
        List<MicrowebsiteExam> exams = examMapper.selectByExample(new MicrowebsiteExamExample());
        List<ExportExamResVo> exportExamResVos = exams.stream()
                .map(exam -> {
                    ExportExamResVo exportExamResVo = new ExportExamResVo();
                    exportExamResVo.setExamCode(exam.getExamCode());
                    exportExamResVo.setExamName(exam.getExamName());
                    exportExamResVo.setExamTypeName(ExamTypeEnum.getEnumByCode(exam.getExamType().intValue()).getMsg());
                    exportExamResVo.setExamStatusName(ExamStatusEnum.getEnumByCode(exam.getExamStatus().intValue()).getMsg());
                    return exportExamResVo;
                })
                .collect(toList());
        ExcelWriter<ExportExamResVo> excelWriter = new ExcelWriter<>(ExcelType.XLSX);
        SheetInfo<ExportExamResVo> sheetInfo = new SheetInfo<>(exportExamResVos, ExportExamResVo.class, "考试信息", 1);
        excelWriter.addSheetInfo(sheetInfo);
        return excelWriter.addSheetFinish().getBytes();
    }
}
