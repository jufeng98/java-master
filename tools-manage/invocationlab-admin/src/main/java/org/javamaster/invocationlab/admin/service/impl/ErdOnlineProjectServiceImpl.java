package org.javamaster.invocationlab.admin.service.impl;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.consts.ErdConst;
import org.javamaster.invocationlab.admin.enums.ProjectType;
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum;
import org.javamaster.invocationlab.admin.handler.WebSocketHandler;
import org.javamaster.invocationlab.admin.model.erd.DatabaseBean;
import org.javamaster.invocationlab.admin.model.erd.DatatypeBean;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.EntitiesBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.FieldsBean;
import org.javamaster.invocationlab.admin.model.erd.GroupGetVo;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.NodesBean;
import org.javamaster.invocationlab.admin.model.erd.PageVo;
import org.javamaster.invocationlab.admin.model.erd.PropertiesBean;
import org.javamaster.invocationlab.admin.model.erd.RecordsVo;
import org.javamaster.invocationlab.admin.model.erd.SaveProjectVo;
import org.javamaster.invocationlab.admin.model.erd.SortModuleReqVo;
import org.javamaster.invocationlab.admin.model.erd.SortModuleVo;
import org.javamaster.invocationlab.admin.model.erd.StatisticVo;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.UsersVo;
import org.javamaster.invocationlab.admin.pdf.ImageTagProcessor;
import org.javamaster.invocationlab.admin.pdf.MyFontProvider;
import org.javamaster.invocationlab.admin.service.DbService;
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService;
import org.javamaster.invocationlab.admin.service.ErdOnlineRoleService;
import org.javamaster.invocationlab.admin.util.DbUtils;
import org.javamaster.invocationlab.admin.util.SessionUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFilesImpl;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.HTML;
import com.itextpdf.tool.xml.html.TagProcessorFactory;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
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
@SuppressWarnings("VulnerableCodeUsages")
@Service
public class ErdOnlineProjectServiceImpl implements ErdOnlineProjectService {
    static {
        initVelocity();
    }

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
    @Autowired
    private WebSocketHandler webSocketHandler;
    @Autowired
    private Map<String, DbService> dbServiceMap;

    @Override
    public StatisticVo statistic() throws Exception {
        Map<Object, Object> map = redisTemplateJackson.opsForHash().entries(PROJECT_STATISTIC);
        int sum = map.values().stream()
                .mapToInt(vo -> {
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
        SessionUtils.saveToSession(SESSION_SAVE_KEY + ":" + projectId, getRedisModCount(projectId));

        String jsonDataStr = stringRedisTemplate.opsForValue().get(ERD_PREFIX + projectId);
        ErdOnlineModel erdOnlineModel = objectMapper.readValue(jsonDataStr, ErdOnlineModel.class);

        eraseSensitiveInfo(erdOnlineModel, tokenVo.getUserId());

        return erdOnlineModel;
    }

    @Override
    public void eraseSensitiveInfo(ErdOnlineModel erdOnlineModel, String userId) {
        DbsBean defaultDb;
        try {
            defaultDb = DbUtils.getDefaultDb(erdOnlineModel);
        } catch (Exception e) {
            // 没有默认数据源,忽略
            return;
        }
        redisTemplateJackson.opsForHash().put(ErdConst.PROJECT_DS, erdOnlineModel.getId(), defaultDb);

        RoleGroupEnum userRoleGroup = erdOnlineRoleService.getUserRoleGroup(userId, erdOnlineModel.getId());
        if (userRoleGroup != RoleGroupEnum.OWNER) {
            PropertiesBean properties = defaultDb.getProperties();
            properties.setUsername("*****");
            properties.setPassword("*****");
            properties.setUrl("*****");
            properties.setCipherType("*****");
            properties.setCipherKey("*****");
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

    @Override
    public DbsBean getDefaultDb(String projectId, TokenVo tokenVo) throws Exception {
        DbsBean defaultDb = (DbsBean) redisTemplateJackson.opsForHash().get(ErdConst.PROJECT_DS, projectId);
        if (defaultDb != null) {
            return defaultDb;
        }
        ErdOnlineModel erdOnlineModel = getProjectDetail(projectId, tokenVo);
        return DbUtils.getDefaultDb(erdOnlineModel);
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

    private Long getRedisModCount(String projectId) {
        return stringRedisTemplate.execute((RedisCallback<Long>) connection -> {
            long redisModCount = 1L;
            byte[] bytes = connection.hashCommands().hGet(REDIS_SAVE_KEY.getBytes(StandardCharsets.UTF_8),
                    projectId.getBytes(StandardCharsets.UTF_8));
            if (bytes != null) {
                redisModCount = Long.parseLong(new String(bytes, StandardCharsets.UTF_8));
            }
            return redisModCount;
        });
    }

    private void checkDataChanged(String projectId) {
        long redisModCount = getRedisModCount(projectId);
        Long sessionModCount = SessionUtils.getFromSession(SESSION_SAVE_KEY + ":" + projectId);
        if (!Objects.equals(redisModCount, sessionModCount)) {
            throw new ErdException(405, "服务器数据已发生变化，为避免互相覆盖导致数据丢失，所有保存都将失败。请刷新页面后再重试！");
        }
    }

    private void increaseModCount(String projectId, RedisConnection connection) {
        Long newSessionModCount = connection.hashCommands().hIncrBy(REDIS_SAVE_KEY.getBytes(StandardCharsets.UTF_8),
                projectId.getBytes(StandardCharsets.UTF_8), 1);
        SessionUtils.saveToSession(SESSION_SAVE_KEY + ":" + projectId, newSessionModCount);
    }

    @Override
    public synchronized Boolean saveProject(SaveProjectVo saveProjectVo, TokenVo tokenVo) {
        ErdOnlineModel erdOnlineModelReq = saveProjectVo.getErdOnlineModel();
        return stringRedisTemplate.execute((RedisCallback<Boolean>) connection -> {
            checkDataChanged(erdOnlineModelReq.getId());
            try {
                validateRelation(erdOnlineModelReq, tokenVo);

                increaseModCount(erdOnlineModelReq.getId(), connection);

                saveProjectDetail(erdOnlineModelReq, tokenVo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return true;
        });
    }

    @Override
    public String sortModule(SortModuleReqVo reqVo, TokenVo tokenVo) throws Exception {
        Map<String, Integer> sortMap = Maps.newHashMap();
        List<SortModuleVo> sortModuleVos = reqVo.getSortModuleVos();
        for (int i = 0; i < sortModuleVos.size(); i++) {
            sortMap.put(sortModuleVos.get(i).getName(), i);
        }
        ErdOnlineModel erdOnlineModel = getProjectDetail(reqVo.getProjectId(), tokenVo);
        List<ModulesBean> modules = erdOnlineModel.getProjectJSON().getModules();
        for (ModulesBean module : modules) {
            Integer sort = sortMap.get(module.getName());
            module.setSort(sort);
        }
        modules.sort(Comparator.comparingInt(ModulesBean::getSort));

        return stringRedisTemplate.execute((RedisCallback<String>) connection -> {
            checkDataChanged(erdOnlineModel.getId());

            increaseModCount(erdOnlineModel.getId(), connection);

            try {
                saveProjectDetail(erdOnlineModel, tokenVo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return "调整顺序成功,即将重新载入页面!";
        });
    }

    private void validateRelation(ErdOnlineModel erdOnlineModelReq, TokenVo tokenVo) {
        erdOnlineModelReq.getProjectJSON().getModules()
                .forEach(modulesBean -> {
                    Set<String> entityTitles = modulesBean.getEntities().stream()
                            .map(EntitiesBean::getTitle)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    Set<String> nodeTitles = modulesBean.getGraphCanvas().getNodes().stream()
                            .map(NodesBean::getTitle)
                            .collect(Collectors.toSet());

                    int entitySize = entityTitles.size();
                    int nodeSize = nodeTitles.size();
                    HashSet<String> tmp = Sets.newHashSet(entityTitles);
                    tmp.removeAll(nodeTitles);
                    if (tmp.size() + nodeSize == entitySize) {
                        return;
                    }

                    try {
                        if (!isModifyTableName(erdOnlineModelReq.getId(), modulesBean, tokenVo, entityTitles, tmp)) {
                            throw new ErdException("检测到关系图数据出现错乱,保存失败,请刷新页面后再重试!");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @SneakyThrows
    private boolean isModifyTableName(String projectId, ModulesBean modulesBean, TokenVo tokenVo,
                                      Set<String> entityTitles, Set<String> needCheckTitles) {
        List<String> tableNames = Lists.newArrayList(entityTitles);
        List<ModulesBean> dbModules = getProjectDetail(projectId, tokenVo).getProjectJSON()
                .getModules();
        for (ModulesBean dbModulesBean : dbModules) {
            if (!dbModulesBean.getName().equals(modulesBean.getName())) {
                continue;
            }
            outer:
            for (String needCheckTitle : needCheckTitles) {
                for (int i = 0; i < tableNames.size(); i++) {
                    if (!tableNames.get(i).equals(needCheckTitle)) {
                        continue;
                    }
                    EntitiesBean dbEntitiesBean = dbModulesBean.getEntities().get(i);
                    Set<String> dbFieldNames = dbEntitiesBean.getFields().stream()
                            .map(FieldsBean::getName)
                            .collect(Collectors.toSet());

                    EntitiesBean entitiesBean = modulesBean.getEntities().get(i);
                    Set<String> fieldNames = entitiesBean.getFields().stream()
                            .map(FieldsBean::getName)
                            .collect(Collectors.toSet());

                    if (!org.apache.commons.collections.CollectionUtils.isEqualCollection(dbFieldNames, fieldNames)) {
                        return false;
                    }

                    continue outer;
                }
            }
        }
        return true;
    }

    @Override
    public ModulesBean refreshProjectModule(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("id");
        String moduleName = jsonObjectReq.getString("moduleName");

        ErdOnlineModel erdOnlineModel = getProjectDetail(projectId, tokenVo);
        resumeSensitiveInfo(erdOnlineModel, tokenVo.getUserId());

        DbsBean dbsBean = getDefaultDb(projectId, tokenVo);
        DbService dbService = dbServiceMap.get(dbsBean.getSelect());

        ModulesBean modulesBean = dbService.refreshModule(erdOnlineModel, moduleName);

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

    public Pair<String, byte[]> exportErd(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        ErdOnlineModel erdOnlineModel = getProjectDetail(projectId, tokenVo);
        @SuppressWarnings("unchecked")
        Map<String, String> imageMap = (Map<String, String>) jsonObjectReq.get("imgs");
        imageMap.replaceAll((k, v) -> "data:image/png;base64," + v);
        VelocityContext context = initVelocityContext(erdOnlineModel, imageMap);
        Template tpl = Velocity.getTemplate("erd.vm", "UTF-8");
        StringWriter stringWriter = new StringWriter();
        tpl.merge(context, stringWriter);
        byte[] bytes = convertHtmlToPdf(stringWriter.toString());
        return Pair.of(erdOnlineModel.getProjectName(), bytes);
    }

    public static byte[] convertHtmlToPdf(String html) throws Exception {
        @Cleanup
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        PdfWriter writer = PdfWriter.getInstance(document, out);

        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", false);
        MyFontProvider myFontProvider = new MyFontProvider(BaseColor.BLACK, "", "",
                false, false, 10, 1, bfChinese);

        // 为该Document创建一个Writer实例
        PdfWriter pdfwriter = PdfWriter.getInstance(document, out);
        pdfwriter.setViewerPreferences(PdfWriter.HideToolbar);
        // 打开当前的document
        document.open();
        final TagProcessorFactory tagProcessorFactory = Tags.getHtmlTagProcessorFactory();
        tagProcessorFactory.removeProcessor(HTML.Tag.IMG);
        tagProcessorFactory.addProcessor(new ImageTagProcessor(), HTML.Tag.IMG);

        final CssFilesImpl cssFiles = new CssFilesImpl();
        cssFiles.add(XMLWorkerHelper.getInstance().getDefaultCSS());
        final StyleAttrCSSResolver cssResolver = new StyleAttrCSSResolver(cssFiles);
        final HtmlPipelineContext hpc = new HtmlPipelineContext(new CssAppliersImpl(myFontProvider));
        hpc.setAcceptUnknown(true).autoBookmark(true).setTagFactory(tagProcessorFactory);
        final HtmlPipeline htmlPipeline = new HtmlPipeline(hpc, new PdfWriterPipeline(document, writer));
        final Pipeline<?> pipeline = new CssResolverPipeline(cssResolver, htmlPipeline);

        final XMLWorker worker = new XMLWorker(pipeline, true);

        final Charset charset = StandardCharsets.UTF_8;
        final XMLParser xmlParser = new XMLParser(true, worker, charset);

        @Cleanup
        ByteArrayInputStream htmlStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        xmlParser.parse(htmlStream, charset);

        document.close();
        return out.toByteArray();
    }

    @SuppressWarnings("unused")
    public String htmlEscape(String s) {
        return HtmlUtils.htmlEscape(s);
    }

    public static VelocityContext initVelocityContext(ErdOnlineModel erdOnlineModel, Map<String, String> images) {
        VelocityContext context = new VelocityContext();
        context.put("projectName", erdOnlineModel.getProjectName());
        context.put("modules", erdOnlineModel.getProjectJSON().getModules());
        context.put("images", images);
        context.put("datatypes", erdOnlineModel.getProjectJSON().getDataTypeDomains().getDatatype());
        List<DatabaseBean> databases = erdOnlineModel.getProjectJSON().getDataTypeDomains().getDatabase();
        databases = databases.stream()
                .filter(it -> {
                    Boolean fileShow = it.getFileShow();
                    if (fileShow == null) {
                        return false;
                    }
                    return fileShow;
                })
                .collect(Collectors.toList());
        context.put("databases", databases);
        String str = databases.stream()
                .map(DatabaseBean::getCode)
                .collect(Collectors.joining("/"));
        context.put("databaseColumn", str);
        context.internalPut("helperObj", new ErdOnlineProjectServiceImpl());
        return context;
    }

    @SuppressWarnings("unused")
    public String getColumnType(List<DatabaseBean> database, List<DatatypeBean> datatype, String type) {
        Set<String> fieldTypes = Sets.newHashSet();
        for (DatabaseBean databaseBean : database) {
            fieldTypes.add(getFieldType(datatype, type));
        }
        return StringUtils.join(fieldTypes, "/");
    }

    public String getFieldType(List<DatatypeBean> datatype, String type) {
        List<DatatypeBean> datatypeBeans = datatype.stream()
                .filter(it -> it.getCode().equals(type)).collect(Collectors.toList());
        if (datatypeBeans.isEmpty()) {
            return type;
        }
        return datatypeBeans.get(0).getApply().getMYSQL().getType();
    }

    public static void initVelocity() {
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        try {
            Velocity.init(prop);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
