package org.javamaster.b2c.core.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mybatis generator
 * @date 2019/07/11 14:41:40
 */
public class MicrowebsiteExamExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MicrowebsiteExamExample() {
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

        public Criteria andExamCodeIsNull() {
            addCriterion("exam_code is null");
            return (Criteria) this;
        }

        public Criteria andExamCodeIsNotNull() {
            addCriterion("exam_code is not null");
            return (Criteria) this;
        }

        public Criteria andExamCodeEqualTo(String value) {
            addCriterion("exam_code =", value, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeNotEqualTo(String value) {
            addCriterion("exam_code <>", value, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeGreaterThan(String value) {
            addCriterion("exam_code >", value, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeGreaterThanOrEqualTo(String value) {
            addCriterion("exam_code >=", value, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeLessThan(String value) {
            addCriterion("exam_code <", value, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeLessThanOrEqualTo(String value) {
            addCriterion("exam_code <=", value, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeLike(String value) {
            addCriterion("exam_code like", value, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeNotLike(String value) {
            addCriterion("exam_code not like", value, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeIn(List<String> values) {
            addCriterion("exam_code in", values, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeNotIn(List<String> values) {
            addCriterion("exam_code not in", values, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeBetween(String value1, String value2) {
            addCriterion("exam_code between", value1, value2, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamCodeNotBetween(String value1, String value2) {
            addCriterion("exam_code not between", value1, value2, "examCode");
            return (Criteria) this;
        }

        public Criteria andExamNameIsNull() {
            addCriterion("exam_name is null");
            return (Criteria) this;
        }

        public Criteria andExamNameIsNotNull() {
            addCriterion("exam_name is not null");
            return (Criteria) this;
        }

        public Criteria andExamNameEqualTo(String value) {
            addCriterion("exam_name =", value, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameNotEqualTo(String value) {
            addCriterion("exam_name <>", value, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameGreaterThan(String value) {
            addCriterion("exam_name >", value, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameGreaterThanOrEqualTo(String value) {
            addCriterion("exam_name >=", value, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameLessThan(String value) {
            addCriterion("exam_name <", value, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameLessThanOrEqualTo(String value) {
            addCriterion("exam_name <=", value, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameLike(String value) {
            addCriterion("exam_name like", value, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameNotLike(String value) {
            addCriterion("exam_name not like", value, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameIn(List<String> values) {
            addCriterion("exam_name in", values, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameNotIn(List<String> values) {
            addCriterion("exam_name not in", values, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameBetween(String value1, String value2) {
            addCriterion("exam_name between", value1, value2, "examName");
            return (Criteria) this;
        }

        public Criteria andExamNameNotBetween(String value1, String value2) {
            addCriterion("exam_name not between", value1, value2, "examName");
            return (Criteria) this;
        }

        public Criteria andExamTypeIsNull() {
            addCriterion("exam_type is null");
            return (Criteria) this;
        }

        public Criteria andExamTypeIsNotNull() {
            addCriterion("exam_type is not null");
            return (Criteria) this;
        }

        public Criteria andExamTypeEqualTo(Byte value) {
            addCriterion("exam_type =", value, "examType");
            return (Criteria) this;
        }

        public Criteria andExamTypeNotEqualTo(Byte value) {
            addCriterion("exam_type <>", value, "examType");
            return (Criteria) this;
        }

        public Criteria andExamTypeGreaterThan(Byte value) {
            addCriterion("exam_type >", value, "examType");
            return (Criteria) this;
        }

        public Criteria andExamTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("exam_type >=", value, "examType");
            return (Criteria) this;
        }

        public Criteria andExamTypeLessThan(Byte value) {
            addCriterion("exam_type <", value, "examType");
            return (Criteria) this;
        }

        public Criteria andExamTypeLessThanOrEqualTo(Byte value) {
            addCriterion("exam_type <=", value, "examType");
            return (Criteria) this;
        }

        public Criteria andExamTypeIn(List<Byte> values) {
            addCriterion("exam_type in", values, "examType");
            return (Criteria) this;
        }

        public Criteria andExamTypeNotIn(List<Byte> values) {
            addCriterion("exam_type not in", values, "examType");
            return (Criteria) this;
        }

        public Criteria andExamTypeBetween(Byte value1, Byte value2) {
            addCriterion("exam_type between", value1, value2, "examType");
            return (Criteria) this;
        }

        public Criteria andExamTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("exam_type not between", value1, value2, "examType");
            return (Criteria) this;
        }

        public Criteria andExamStatusIsNull() {
            addCriterion("exam_status is null");
            return (Criteria) this;
        }

        public Criteria andExamStatusIsNotNull() {
            addCriterion("exam_status is not null");
            return (Criteria) this;
        }

        public Criteria andExamStatusEqualTo(Byte value) {
            addCriterion("exam_status =", value, "examStatus");
            return (Criteria) this;
        }

        public Criteria andExamStatusNotEqualTo(Byte value) {
            addCriterion("exam_status <>", value, "examStatus");
            return (Criteria) this;
        }

        public Criteria andExamStatusGreaterThan(Byte value) {
            addCriterion("exam_status >", value, "examStatus");
            return (Criteria) this;
        }

        public Criteria andExamStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("exam_status >=", value, "examStatus");
            return (Criteria) this;
        }

        public Criteria andExamStatusLessThan(Byte value) {
            addCriterion("exam_status <", value, "examStatus");
            return (Criteria) this;
        }

        public Criteria andExamStatusLessThanOrEqualTo(Byte value) {
            addCriterion("exam_status <=", value, "examStatus");
            return (Criteria) this;
        }

        public Criteria andExamStatusIn(List<Byte> values) {
            addCriterion("exam_status in", values, "examStatus");
            return (Criteria) this;
        }

        public Criteria andExamStatusNotIn(List<Byte> values) {
            addCriterion("exam_status not in", values, "examStatus");
            return (Criteria) this;
        }

        public Criteria andExamStatusBetween(Byte value1, Byte value2) {
            addCriterion("exam_status between", value1, value2, "examStatus");
            return (Criteria) this;
        }

        public Criteria andExamStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("exam_status not between", value1, value2, "examStatus");
            return (Criteria) this;
        }

        public Criteria andDelFlagIsNull() {
            addCriterion("del_flag is null");
            return (Criteria) this;
        }

        public Criteria andDelFlagIsNotNull() {
            addCriterion("del_flag is not null");
            return (Criteria) this;
        }

        public Criteria andDelFlagEqualTo(Byte value) {
            addCriterion("del_flag =", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagNotEqualTo(Byte value) {
            addCriterion("del_flag <>", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagGreaterThan(Byte value) {
            addCriterion("del_flag >", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagGreaterThanOrEqualTo(Byte value) {
            addCriterion("del_flag >=", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagLessThan(Byte value) {
            addCriterion("del_flag <", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagLessThanOrEqualTo(Byte value) {
            addCriterion("del_flag <=", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagIn(List<Byte> values) {
            addCriterion("del_flag in", values, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagNotIn(List<Byte> values) {
            addCriterion("del_flag not in", values, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagBetween(Byte value1, Byte value2) {
            addCriterion("del_flag between", value1, value2, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagNotBetween(Byte value1, Byte value2) {
            addCriterion("del_flag not between", value1, value2, "delFlag");
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