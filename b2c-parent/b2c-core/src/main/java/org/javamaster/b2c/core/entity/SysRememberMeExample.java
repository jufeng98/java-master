package org.javamaster.b2c.core.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public class SysRememberMeExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public SysRememberMeExample() {
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

        public Criteria andSeriesIsNull() {
            addCriterion("series is null");
            return (Criteria) this;
        }

        public Criteria andSeriesIsNotNull() {
            addCriterion("series is not null");
            return (Criteria) this;
        }

        public Criteria andSeriesEqualTo(String value) {
            addCriterion("series =", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesNotEqualTo(String value) {
            addCriterion("series <>", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesGreaterThan(String value) {
            addCriterion("series >", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesGreaterThanOrEqualTo(String value) {
            addCriterion("series >=", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesLessThan(String value) {
            addCriterion("series <", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesLessThanOrEqualTo(String value) {
            addCriterion("series <=", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesLike(String value) {
            addCriterion("series like", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesNotLike(String value) {
            addCriterion("series not like", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesIn(List<String> values) {
            addCriterion("series in", values, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesNotIn(List<String> values) {
            addCriterion("series not in", values, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesBetween(String value1, String value2) {
            addCriterion("series between", value1, value2, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesNotBetween(String value1, String value2) {
            addCriterion("series not between", value1, value2, "series");
            return (Criteria) this;
        }

        public Criteria andUsernameIsNull() {
            addCriterion("username is null");
            return (Criteria) this;
        }

        public Criteria andUsernameIsNotNull() {
            addCriterion("username is not null");
            return (Criteria) this;
        }

        public Criteria andUsernameEqualTo(String value) {
            addCriterion("username =", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotEqualTo(String value) {
            addCriterion("username <>", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameGreaterThan(String value) {
            addCriterion("username >", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("username >=", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLessThan(String value) {
            addCriterion("username <", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLessThanOrEqualTo(String value) {
            addCriterion("username <=", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLike(String value) {
            addCriterion("username like", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotLike(String value) {
            addCriterion("username not like", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameIn(List<String> values) {
            addCriterion("username in", values, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotIn(List<String> values) {
            addCriterion("username not in", values, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameBetween(String value1, String value2) {
            addCriterion("username between", value1, value2, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotBetween(String value1, String value2) {
            addCriterion("username not between", value1, value2, "username");
            return (Criteria) this;
        }

        public Criteria andTokenIsNull() {
            addCriterion("token is null");
            return (Criteria) this;
        }

        public Criteria andTokenIsNotNull() {
            addCriterion("token is not null");
            return (Criteria) this;
        }

        public Criteria andTokenEqualTo(String value) {
            addCriterion("token =", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenNotEqualTo(String value) {
            addCriterion("token <>", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenGreaterThan(String value) {
            addCriterion("token >", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenGreaterThanOrEqualTo(String value) {
            addCriterion("token >=", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenLessThan(String value) {
            addCriterion("token <", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenLessThanOrEqualTo(String value) {
            addCriterion("token <=", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenLike(String value) {
            addCriterion("token like", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenNotLike(String value) {
            addCriterion("token not like", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenIn(List<String> values) {
            addCriterion("token in", values, "token");
            return (Criteria) this;
        }

        public Criteria andTokenNotIn(List<String> values) {
            addCriterion("token not in", values, "token");
            return (Criteria) this;
        }

        public Criteria andTokenBetween(String value1, String value2) {
            addCriterion("token between", value1, value2, "token");
            return (Criteria) this;
        }

        public Criteria andTokenNotBetween(String value1, String value2) {
            addCriterion("token not between", value1, value2, "token");
            return (Criteria) this;
        }

        public Criteria andLastUsedIsNull() {
            addCriterion("last_used is null");
            return (Criteria) this;
        }

        public Criteria andLastUsedIsNotNull() {
            addCriterion("last_used is not null");
            return (Criteria) this;
        }

        public Criteria andLastUsedEqualTo(Date value) {
            addCriterion("last_used =", value, "lastUsed");
            return (Criteria) this;
        }

        public Criteria andLastUsedNotEqualTo(Date value) {
            addCriterion("last_used <>", value, "lastUsed");
            return (Criteria) this;
        }

        public Criteria andLastUsedGreaterThan(Date value) {
            addCriterion("last_used >", value, "lastUsed");
            return (Criteria) this;
        }

        public Criteria andLastUsedGreaterThanOrEqualTo(Date value) {
            addCriterion("last_used >=", value, "lastUsed");
            return (Criteria) this;
        }

        public Criteria andLastUsedLessThan(Date value) {
            addCriterion("last_used <", value, "lastUsed");
            return (Criteria) this;
        }

        public Criteria andLastUsedLessThanOrEqualTo(Date value) {
            addCriterion("last_used <=", value, "lastUsed");
            return (Criteria) this;
        }

        public Criteria andLastUsedIn(List<Date> values) {
            addCriterion("last_used in", values, "lastUsed");
            return (Criteria) this;
        }

        public Criteria andLastUsedNotIn(List<Date> values) {
            addCriterion("last_used not in", values, "lastUsed");
            return (Criteria) this;
        }

        public Criteria andLastUsedBetween(Date value1, Date value2) {
            addCriterion("last_used between", value1, value2, "lastUsed");
            return (Criteria) this;
        }

        public Criteria andLastUsedNotBetween(Date value1, Date value2) {
            addCriterion("last_used not between", value1, value2, "lastUsed");
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