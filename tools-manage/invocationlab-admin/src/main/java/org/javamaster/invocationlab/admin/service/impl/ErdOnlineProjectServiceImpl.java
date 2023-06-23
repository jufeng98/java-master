package org.javamaster.invocationlab.admin.service.impl;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.consts.ErdConst;
import org.javamaster.invocationlab.admin.enums.ProjectType;
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum;
import org.javamaster.invocationlab.admin.model.PageVo;
import org.javamaster.invocationlab.admin.model.RecordsVo;
import org.javamaster.invocationlab.admin.model.StatisticVo;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.EntitiesBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.GroupGetVo;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.NodesBean;
import org.javamaster.invocationlab.admin.model.erd.PropertiesBean;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.UsersVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService;
import org.javamaster.invocationlab.admin.service.ErdOnlineRoleService;
import org.javamaster.invocationlab.admin.util.DbUtils;
import org.javamaster.invocationlab.admin.util.ErdUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;
import static org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_ROLE_GROUP_USERS;
import static org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_STATISTIC;
import static org.javamaster.invocationlab.admin.consts.ErdConst.USER_PROJECT_ROLE_GROUP;
import static org.javamaster.invocationlab.admin.util.ErdUtils.getPair;

/**
 * @author yudong
 */
@Service
public class ErdOnlineProjectServiceImpl implements ErdOnlineProjectService {
    private static final String REDIS_SAVE_KEY = ERD_PREFIX + "save:modCountRedis";
    private static final String SESSION_SAVE_KEY = "erd:save:modCount";
    private static final String PAGE_JSON_STR = "{\"records\":[],\"total\":0,\"size\":100,\"current\":1,\"orders\":[],\"searchCount\":true,\"pages\":1}";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplateJackson;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ErdOnlineRoleService erdOnlineRoleService;

    @Override
    public StatisticVo statistic() throws Exception {
        Map<Object, Object> map = redisTemplateJackson.opsForHash().entries(PROJECT_STATISTIC);
        int sum = map.values().stream()
                .mapToInt(vo -> {
                    System.out.println(vo);
                    StatisticVo statisticVo = (StatisticVo) vo;
                    return statisticVo.getTotal();
                })
                .sum();
        StatisticVo statisticVo = new StatisticVo();
        statisticVo.setTotal(sum == 0 ? 38 : sum);
        return statisticVo;
    }

    @Override
    public PageVo getProjectList(TokenVo tokenVo, ProjectType projectType, String projectName) throws Exception {
        return getUserPageVo(tokenVo.getUserId(), projectType, projectName);
    }

    @Override
    public void saveRecordsVoToGroupUsers(String projectId, List<UsersVo> reqUsersVos,
                                          RoleGroupEnum roleGroupEnum, TokenVo tokenVo) throws Exception {
        ProjectType projectType = ProjectType.GROUP;
        RecordsVo recordsVo = findRecordsVoByProjectId(projectId, tokenVo);
        for (UsersVo reqUsersVo : reqUsersVos) {
            PageVo pageVo = getUserPageVo(reqUsersVo.getId(), projectType, null);
            Optional<RecordsVo> optional = pageVo.getRecords().stream()
                    .filter(userRecordsVo -> userRecordsVo.getId().equals(recordsVo.getId()))
                    .findAny();
            if (optional.isPresent()) {
                RecordsVo userRecordsVo = optional.get();
                BeanUtils.copyProperties(recordsVo, userRecordsVo);
            } else {
                pageVo.getRecords().add(recordsVo);
            }
            pageVo.setTotal(pageVo.getRecords().size());
            saveUserPageVo(pageVo, reqUsersVo.getId(), projectType);
            redisTemplateJackson.opsForHash().put(USER_PROJECT_ROLE_GROUP + reqUsersVo.getId(),
                    projectId, roleGroupEnum.name());
        }
    }

    @Override
    public void delRecordsVoFromGroupUsers(String projectId, List<UsersVo> reqUsersVos, TokenVo tokenVo) throws Exception {
        ProjectType projectType = ProjectType.GROUP;
        for (UsersVo reqUsersVo : reqUsersVos) {
            PageVo pageVo = getUserPageVo(reqUsersVo.getId(), projectType, null);
            removeProjectFromRecords(pageVo.getRecords(), projectId);
            pageVo.setTotal(pageVo.getRecords().size());
            saveUserPageVo(pageVo, reqUsersVo.getId(), projectType);
            redisTemplateJackson.opsForHash().delete(USER_PROJECT_ROLE_GROUP + reqUsersVo.getId(), projectId);
        }
    }

    private PageVo getUserPageVo(String userId, ProjectType projectType, String projectName) throws Exception {
        Pair<String, String> pair = getPair(projectType, userId);
        String jsonDataStr = (String) stringRedisTemplate.opsForHash().get(pair.getLeft(), pair.getRight());
        if (jsonDataStr == null) {
            jsonDataStr = PAGE_JSON_STR;
        }
        PageVo pageVo = objectMapper.readValue(jsonDataStr, PageVo.class);
        if (StringUtils.isNotBlank(projectName)) {
            pageVo.getRecords().removeIf(recordsVo -> !recordsVo.getProjectName().contains(projectName));
            pageVo.setTotal(pageVo.getRecords().size());
        }
        return pageVo;
    }

    private void saveUserPageVo(PageVo pageVo, String userId, ProjectType projectType) throws Exception {
        Pair<String, String> pair = getPair(projectType, userId);
        stringRedisTemplate.opsForHash().put(pair.getLeft(), pair.getRight(), objectMapper.writeValueAsString(pageVo));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public RecordsVo findRecordsVoByProjectId(String projectId, TokenVo tokenVo) throws Exception {
        List<RecordsVo> records = findRecords(ProjectType.PERSONAL, tokenVo);
        records.addAll(findRecords(ProjectType.GROUP, tokenVo));
        return records.stream()
                .filter(recordsVo -> recordsVo.getId().equals(projectId))
                .findAny()
                .get();
    }

    private List<RecordsVo> findRecords(ProjectType projectType, TokenVo tokenVo) throws Exception {
        PageVo pageVo = getUserPageVo(tokenVo.getUserId(), projectType, null);
        return pageVo.getRecords();
    }

    @Override
    public ErdOnlineModel getProjectDetail(String projectId, TokenVo tokenVo) throws Exception {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpSession session = Objects.requireNonNull(requestAttributes).getRequest().getSession();
        session.setAttribute(SESSION_SAVE_KEY, null);
        String jsonDataStr = stringRedisTemplate.opsForValue().get(ERD_PREFIX + projectId);
        ErdOnlineModel erdOnlineModel = objectMapper.readValue(jsonDataStr, ErdOnlineModel.class);
        eraseSensitiveInfo(erdOnlineModel, tokenVo.getUserId());
        return erdOnlineModel;
    }

    @Override
    public void eraseSensitiveInfo(ErdOnlineModel erdOnlineModel, String userId) {
        RoleGroupEnum userRoleGroup = erdOnlineRoleService.getUserRoleGroup(userId, erdOnlineModel.getId());
        if (userRoleGroup == null) {
            return;
        }
        DbsBean defaultDb;
        try {
            defaultDb = DbUtils.getDefaultDb(erdOnlineModel);
        } catch (Exception e) {
            // 没有默认数据源,忽略
            return;
        }
        redisTemplateJackson.opsForHash().put(ErdConst.PROJECT_DS, erdOnlineModel.getId(), defaultDb);
        if (userRoleGroup != RoleGroupEnum.OWNER) {
            PropertiesBean properties = defaultDb.getProperties();
            properties.setUsername("*****");
            properties.setPassword("*****");
            properties.setUrl("*****");
        }
    }

    @Override
    public void resumeSensitiveInfo(ErdOnlineModel erdOnlineModel, String userId) {
        RoleGroupEnum userRoleGroup = erdOnlineRoleService.getUserRoleGroup(userId, erdOnlineModel.getId());
        if (userRoleGroup == null) {
            return;
        }
        if (userRoleGroup != RoleGroupEnum.OWNER) {
            try {
                DbsBean defaultDb = (DbsBean) redisTemplateJackson.opsForHash().get(ErdConst.PROJECT_DS, erdOnlineModel.getId());
                DbsBean dbsBean = DbUtils.getDefaultDb(erdOnlineModel);
                //noinspection DataFlowIssue,ConstantConditions
                BeanUtils.copyProperties(defaultDb, dbsBean);
            } catch (Exception e) {
                // 没有默认数据源,忽略
            }
        }
    }

    private void saveProjectDetail(ErdOnlineModel erdOnlineModel, TokenVo tokenVo) throws Exception {
        resumeSensitiveInfo(erdOnlineModel, tokenVo.getUserId());
        stringRedisTemplate.opsForValue().set(ERD_PREFIX + erdOnlineModel.getId(), objectMapper.writeValueAsString(erdOnlineModel));
        int sum = erdOnlineModel.getProjectJSON().getModules().stream()
                .mapToInt(module -> module.getEntities().size())
                .sum();
        StatisticVo statisticVo = new StatisticVo();
        statisticVo.setTotal(sum);
        redisTemplateJackson.opsForHash().put(PROJECT_STATISTIC, erdOnlineModel.getId(), statisticVo);
    }

    @Override
    public String createProject(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        ProjectType projectType = ProjectType.getByType(jsonObjectReq.getInteger("type"));
        String projectId = UUID.randomUUID().toString().replace("-", "");
        projectId = projectType.type + "0" + projectId.substring(2);
        RecordsVo recordsVo = new RecordsVo();
        recordsVo.setId(projectId);
        recordsVo.setProjectName(jsonObjectReq.getString("projectName"));
        recordsVo.setDescription(jsonObjectReq.getString("description"));
        recordsVo.setTags(jsonObjectReq.getString("tags"));
        recordsVo.setCreator(tokenVo.getUserId());
        Date now = new Date();
        recordsVo.setCreateTime(now);
        recordsVo.setUpdater(tokenVo.getUserId());
        recordsVo.setUpdateTime(now);
        recordsVo.setType(projectType.type);
        PageVo pageVo = getUserPageVo(tokenVo.getUserId(), projectType, null);
        List<RecordsVo> records = pageVo.getRecords();
        records.add(recordsVo);
        pageVo.setTotal(records.size());
        saveUserPageVo(pageVo, tokenVo.getUserId(), projectType);

        ErdOnlineModel erdOnlineModel = objectMapper.readValue(jsonObjectReq.toJSONString(), ErdOnlineModel.class);
        erdOnlineModel.setId(projectId);
        erdOnlineModel.setProjectName(recordsVo.getProjectName());
        erdOnlineModel.setType(projectType.type + "");
        saveProjectDetail(erdOnlineModel, tokenVo);

        erdOnlineRoleService.saveRolesWhenCreateProject(projectId, tokenVo);
        return "操作成功";
    }

    @Override
    public String deleteProject(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("id");
        ProjectType projectType = ProjectType.getByType(jsonObjectReq.getIntValue("type"));

        PageVo pageVo = getUserPageVo(tokenVo.getUserId(), projectType, null);
        removeProjectFromRecords(pageVo.getRecords(), projectId);
        pageVo.setTotal(pageVo.getRecords().size());
        saveUserPageVo(pageVo, tokenVo.getUserId(), projectType);

        if (projectType == ProjectType.GROUP) {
            delRelateUserRecord(projectId, tokenVo, RoleGroupEnum.ADMIN);
            delRelateUserRecord(projectId, tokenVo, RoleGroupEnum.ORDINARY);
        }

        stringRedisTemplate.delete(ERD_PREFIX + projectId);

        erdOnlineRoleService.delRolesWhenDelProject(projectId, tokenVo);
        return "操作成功";
    }

    private void removeProjectFromRecords(List<RecordsVo> records, String projectId) {
        if (records == null) {
            return;
        }
        records.removeIf(recordsVo -> recordsVo.getId().equals(projectId));
    }

    @Override
    public String updateProjectBasicInfo(RecordsVo recordsVoReq, TokenVo tokenVo) throws Exception {
        ProjectType projectType = ProjectType.getByType(recordsVoReq.getType());
        PageVo pageVo = getUserPageVo(tokenVo.getUserId(), projectType, null);
        List<RecordsVo> records = pageVo.getRecords();
        records.stream()
                .filter(record -> record.getId().equals(recordsVoReq.getId()))
                .forEach(record -> {
                    record.setProjectName(recordsVoReq.getProjectName());
                    record.setDescription(recordsVoReq.getDescription());
                    record.setTags(recordsVoReq.getTags());
                    record.setUpdater(tokenVo.getUserId());
                    record.setUpdateTime(new Date());
                });
        saveUserPageVo(pageVo, tokenVo.getUserId(), projectType);

        ErdOnlineModel erdOnlineModel = getProjectDetail(recordsVoReq.getId(), tokenVo);
        erdOnlineModel.setProjectName(recordsVoReq.getProjectName());
        saveProjectDetail(erdOnlineModel, tokenVo);

        if (projectType == ProjectType.GROUP) {
            updateRelateUserRecord(recordsVoReq.getId(), tokenVo, RoleGroupEnum.ADMIN);
            updateRelateUserRecord(recordsVoReq.getId(), tokenVo, RoleGroupEnum.ORDINARY);
        }
        return "操作成功";
    }

    private void updateRelateUserRecord(String projectId, TokenVo tokenVo, RoleGroupEnum roleGroupEnum) throws Exception {
        @SuppressWarnings("unchecked")
        List<UsersVo> usersVos = (List<UsersVo>) redisTemplateJackson.opsForHash().get(PROJECT_ROLE_GROUP_USERS + projectId,
                roleGroupEnum.name());
        if (CollectionUtils.isEmpty(usersVos)) {
            return;
        }
        saveRecordsVoToGroupUsers(projectId, usersVos, roleGroupEnum, tokenVo);
    }

    private void delRelateUserRecord(String projectId, TokenVo tokenVo, RoleGroupEnum roleGroupEnum) throws Exception {
        @SuppressWarnings("unchecked")
        List<UsersVo> usersVos = (List<UsersVo>) redisTemplateJackson.opsForHash().get(PROJECT_ROLE_GROUP_USERS + projectId,
                roleGroupEnum.name());
        if (CollectionUtils.isEmpty(usersVos)) {
            return;
        }
        delRecordsVoFromGroupUsers(projectId, usersVos, tokenVo);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public synchronized Boolean saveProject(ErdOnlineModel erdOnlineModelReq, TokenVo tokenVo) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpSession session = requestAttributes.getRequest().getSession();
        return stringRedisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Long sessionModCount = 1L;
            byte[] bytes = connection.hashCommands().hGet(REDIS_SAVE_KEY.getBytes(StandardCharsets.UTF_8),
                    erdOnlineModelReq.getId().getBytes(StandardCharsets.UTF_8));
            if (bytes != null) {
                sessionModCount = Long.parseLong(new String(bytes, StandardCharsets.UTF_8));
            }
            Long redisModCount = (Long) session.getAttribute(SESSION_SAVE_KEY);
            if (redisModCount != null) {
                if (!Objects.equals(sessionModCount, redisModCount)) {
                    throw new ErdException(405, "服务器数据已被人修改，所有保存都将失败。为避免数据互相覆盖导致丢失，请刷新页面后再重试！");
                }
            }
            try {
                validateRelation(erdOnlineModelReq);
                sessionModCount = connection.hashCommands().hIncrBy(REDIS_SAVE_KEY.getBytes(StandardCharsets.UTF_8),
                        erdOnlineModelReq.getId().getBytes(StandardCharsets.UTF_8), 1);
                saveProjectDetail(erdOnlineModelReq, tokenVo);
                session.setAttribute(SESSION_SAVE_KEY, sessionModCount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return true;
        });
    }

    private void validateRelation(ErdOnlineModel erdOnlineModelReq) {
        erdOnlineModelReq.getProjectJSON().getModules()
                .forEach(modulesBean -> {
                    Set<String> entityTitles = modulesBean.getEntities().stream()
                            .map(EntitiesBean::getTitle)
                            .collect(Collectors.toSet());
                    Set<String> nodeTitles = modulesBean.getGraphCanvas().getNodes().stream()
                            .map(NodesBean::getTitle)
                            .collect(Collectors.toSet());
                    int entitySize = entityTitles.size();
                    int nodeSize = nodeTitles.size();
                    entityTitles.removeAll(nodeTitles);
                    if (entityTitles.size() + nodeSize != entitySize) {
                        throw new ErdException("检测到关系图数据出现错乱,保存失败,请刷新页面后再重试!");
                    }
                });
    }

    @Override
    public ModulesBean refreshProjectModule(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("id");
        String name = jsonObjectReq.getString("moduleName");
        ErdOnlineModel erdOnlineModel = getProjectDetail(projectId, tokenVo);
        resumeSensitiveInfo(erdOnlineModel, tokenVo.getUserId());
        ModulesBean modulesBean = ErdUtils.refreshModule(erdOnlineModel, name);
        saveProjectDetail(erdOnlineModel, tokenVo);
        return modulesBean;
    }

    @Override
    public GroupGetVo getProjectBasicInfo(String projectId, TokenVo tokenVo) throws Exception {
        RecordsVo recordsVo = findRecordsVoByProjectId(projectId, tokenVo);
        GroupGetVo groupGetVo = new GroupGetVo();
        groupGetVo.setId(recordsVo.getId());
        groupGetVo.setConfigJSON("");
        groupGetVo.setProjectJSON("");
        groupGetVo.setProjectName(recordsVo.getProjectName());
        groupGetVo.setDescription(recordsVo.getDescription());
        groupGetVo.setType(recordsVo.getType() + "");
        groupGetVo.setTags(recordsVo.getTags());
        groupGetVo.setDelFlag("0");
        groupGetVo.setCreator(recordsVo.getCreator());
        groupGetVo.setCreateTime(recordsVo.getCreateTime());
        groupGetVo.setUpdater(recordsVo.getUpdater());
        groupGetVo.setUpdateTime(recordsVo.getUpdateTime());
        return groupGetVo;
    }

}
