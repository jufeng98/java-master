package org.javamaster.b2c.mybatis.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 请勿手工改动此文件,请使用 mybatis generator
 * 
 * @author mybatis generator
 */
public class ExamsExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ExamsExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andExamsCodeIsNull() {
            addCriterion("exams_code is null");
            return (Criteria) this;
        }

        public Criteria andExamsCodeIsNotNull() {
            addCriterion("exams_code is not null");
            return (Criteria) this;
        }

        public Criteria andExamsCodeEqualTo(String value) {
            addCriterion("exams_code =", value, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeNotEqualTo(String value) {
            addCriterion("exams_code <>", value, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeGreaterThan(String value) {
            addCriterion("exams_code >", value, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeGreaterThanOrEqualTo(String value) {
            addCriterion("exams_code >=", value, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeLessThan(String value) {
            addCriterion("exams_code <", value, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeLessThanOrEqualTo(String value) {
            addCriterion("exams_code <=", value, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeLike(String value) {
            addCriterion("exams_code like", value, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeNotLike(String value) {
            addCriterion("exams_code not like", value, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeIn(List<String> values) {
            addCriterion("exams_code in", values, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeNotIn(List<String> values) {
            addCriterion("exams_code not in", values, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeBetween(String value1, String value2) {
            addCriterion("exams_code between", value1, value2, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsCodeNotBetween(String value1, String value2) {
            addCriterion("exams_code not between", value1, value2, "examsCode");
            return (Criteria) this;
        }

        public Criteria andExamsNameIsNull() {
            addCriterion("exams_name is null");
            return (Criteria) this;
        }

        public Criteria andExamsNameIsNotNull() {
            addCriterion("exams_name is not null");
            return (Criteria) this;
        }

        public Criteria andExamsNameEqualTo(String value) {
            addCriterion("exams_name =", value, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameNotEqualTo(String value) {
            addCriterion("exams_name <>", value, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameGreaterThan(String value) {
            addCriterion("exams_name >", value, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameGreaterThanOrEqualTo(String value) {
            addCriterion("exams_name >=", value, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameLessThan(String value) {
            addCriterion("exams_name <", value, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameLessThanOrEqualTo(String value) {
            addCriterion("exams_name <=", value, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameLike(String value) {
            addCriterion("exams_name like", value, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameNotLike(String value) {
            addCriterion("exams_name not like", value, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameIn(List<String> values) {
            addCriterion("exams_name in", values, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameNotIn(List<String> values) {
            addCriterion("exams_name not in", values, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameBetween(String value1, String value2) {
            addCriterion("exams_name between", value1, value2, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsNameNotBetween(String value1, String value2) {
            addCriterion("exams_name not between", value1, value2, "examsName");
            return (Criteria) this;
        }

        public Criteria andExamsDescIsNull() {
            addCriterion("exams_desc is null");
            return (Criteria) this;
        }

        public Criteria andExamsDescIsNotNull() {
            addCriterion("exams_desc is not null");
            return (Criteria) this;
        }

        public Criteria andExamsDescEqualTo(String value) {
            addCriterion("exams_desc =", value, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescNotEqualTo(String value) {
            addCriterion("exams_desc <>", value, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescGreaterThan(String value) {
            addCriterion("exams_desc >", value, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescGreaterThanOrEqualTo(String value) {
            addCriterion("exams_desc >=", value, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescLessThan(String value) {
            addCriterion("exams_desc <", value, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescLessThanOrEqualTo(String value) {
            addCriterion("exams_desc <=", value, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescLike(String value) {
            addCriterion("exams_desc like", value, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescNotLike(String value) {
            addCriterion("exams_desc not like", value, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescIn(List<String> values) {
            addCriterion("exams_desc in", values, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescNotIn(List<String> values) {
            addCriterion("exams_desc not in", values, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescBetween(String value1, String value2) {
            addCriterion("exams_desc between", value1, value2, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andExamsDescNotBetween(String value1, String value2) {
            addCriterion("exams_desc not between", value1, value2, "examsDesc");
            return (Criteria) this;
        }

        public Criteria andCertsIdIsNull() {
            addCriterion("certs_id is null");
            return (Criteria) this;
        }

        public Criteria andCertsIdIsNotNull() {
            addCriterion("certs_id is not null");
            return (Criteria) this;
        }

        public Criteria andCertsIdEqualTo(Integer value) {
            addCriterion("certs_id =", value, "certsId");
            return (Criteria) this;
        }

        public Criteria andCertsIdNotEqualTo(Integer value) {
            addCriterion("certs_id <>", value, "certsId");
            return (Criteria) this;
        }

        public Criteria andCertsIdGreaterThan(Integer value) {
            addCriterion("certs_id >", value, "certsId");
            return (Criteria) this;
        }

        public Criteria andCertsIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("certs_id >=", value, "certsId");
            return (Criteria) this;
        }

        public Criteria andCertsIdLessThan(Integer value) {
            addCriterion("certs_id <", value, "certsId");
            return (Criteria) this;
        }

        public Criteria andCertsIdLessThanOrEqualTo(Integer value) {
            addCriterion("certs_id <=", value, "certsId");
            return (Criteria) this;
        }

        public Criteria andCertsIdIn(List<Integer> values) {
            addCriterion("certs_id in", values, "certsId");
            return (Criteria) this;
        }

        public Criteria andCertsIdNotIn(List<Integer> values) {
            addCriterion("certs_id not in", values, "certsId");
            return (Criteria) this;
        }

        public Criteria andCertsIdBetween(Integer value1, Integer value2) {
            addCriterion("certs_id between", value1, value2, "certsId");
            return (Criteria) this;
        }

        public Criteria andCertsIdNotBetween(Integer value1, Integer value2) {
            addCriterion("certs_id not between", value1, value2, "certsId");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameIsNull() {
            addCriterion("create_username is null");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameIsNotNull() {
            addCriterion("create_username is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameEqualTo(String value) {
            addCriterion("create_username =", value, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameNotEqualTo(String value) {
            addCriterion("create_username <>", value, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameGreaterThan(String value) {
            addCriterion("create_username >", value, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("create_username >=", value, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameLessThan(String value) {
            addCriterion("create_username <", value, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameLessThanOrEqualTo(String value) {
            addCriterion("create_username <=", value, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameLike(String value) {
            addCriterion("create_username like", value, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameNotLike(String value) {
            addCriterion("create_username not like", value, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameIn(List<String> values) {
            addCriterion("create_username in", values, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameNotIn(List<String> values) {
            addCriterion("create_username not in", values, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameBetween(String value1, String value2) {
            addCriterion("create_username between", value1, value2, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateUsernameNotBetween(String value1, String value2) {
            addCriterion("create_username not between", value1, value2, "createUsername");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}