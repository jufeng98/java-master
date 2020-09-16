package org.javamaster.b2c.core;

import org.javamaster.b2c.core.model.Inventor;
import org.javamaster.b2c.core.model.vo.CreateUserReqVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

/**
 * @author yudong
 * @date 2019/7/5
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CoreApplication.class)
@WebAppConfiguration
public class SpELTest {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Value("#{systemProperties['file.encoding']}")
    private String encoding;
    @Value("#{systemEnvironment['JAVA_HOME']}")
    private String home;

    @Autowired
    private ExpressionParser parser;

    @Test
    public void test() {
        // 可以获取系统属性和环境属性
        logger.info("file.encoding:{}", encoding);
        logger.info("JAVA_HOME:{}", home);
    }

    @Test
    public void test1() {
        // 可以解析Java基本类型数据,可以调用相应类型的方法
        Expression exp = parser.parseExpression("'Hello World'");
        logger.info((String) exp.getValue());

        logger.info(parser.parseExpression("'test' + ' ' + 'string'").getValue(String.class));

        Expression exp1 = parser.parseExpression("'Hello World'.concat('!')");
        logger.info((String) exp1.getValue());

        logger.info(parser.parseExpression("'abc'.substring(2, 3)").getValue(String.class));

        logger.info((String) parser.parseExpression("6.0221415E+23").getValue());
    }

    @Test
    public void test2() {
        // 可以解析对象属性值
        CreateUserReqVo createUserReqVo = new CreateUserReqVo();
        createUserReqVo.setUsername("liangyudong");
        createUserReqVo.setPassword("123456");

        // 标准写法
        Expression exp = parser.parseExpression("username");
        logger.info((String) exp.getValue(createUserReqVo));

        EvaluationContext context = new StandardEvaluationContext(createUserReqVo);
        logger.info((String) exp.getValue(context));

        // 模板写法,同@Value注解的写法
        logger.info((String) parser.parseExpression("#{username}", ParserContext.TEMPLATE_EXPRESSION).getValue(context));
    }

    @Test
    public void test3() {
        // 调用Java类的静态方法
        logger.info(String.valueOf(parser.parseExpression("#{T(java.lang.Math).random() * 100.0}", ParserContext.TEMPLATE_EXPRESSION).getValue(Double.class)));

        // java.lang包内的不需要使用全限定名
        logger.info(String.valueOf(parser.parseExpression("T(String)").getValue(Class.class)));

        // 条件判断
        logger.info(String.valueOf(parser.parseExpression("2==2").getValue(Boolean.class)));

        logger.info(String.valueOf(parser.parseExpression("'xyz' instanceof T(Integer)").getValue(Boolean.class)));

        logger.info(String.valueOf(parser.parseExpression("'5.00' matches '^-?\\d+(\\.\\d{2})?$'").getValue(Boolean.class)));

        logger.info(String.valueOf(parser.parseExpression("1 + 1").getValue(Integer.class)));

    }

    @Test
    @SuppressWarnings("ALL")
    public void test4() throws Exception {
        List<Integer> primes = new ArrayList<>(Arrays.asList(2, 3, 5, 7, 11, 13, 17));

        StandardEvaluationContext context = new StandardEvaluationContext();
        // 注册一个对象变量,以便能在spEL中引用
        context.setVariable("primes", primes);
        Integer age = null;
        context.setVariable("num", 12);
        context.setVariable("age", age);
        context.setVariable("timestamp", System.currentTimeMillis());
        context.setVariable("hasPayed", false);
        context.setVariable("name", null);

        // 过滤List并做投影运算
        List<Integer> primesGreaterThanTen = (List<Integer>) parser.parseExpression("#primes.?[#this>10]").getValue(context);
        logger.info(primesGreaterThanTen.toString());

        // 注册一个方法,以便能在spEL中调用
        context.registerFunction("reverseString", SpELTest.class.getDeclaredMethod("reverseString", String.class));
        logger.info(parser.parseExpression("#reverseString('hello')").getValue(context, String.class));

        logger.info((String) parser.parseExpression("#num+'_'+#timestamp+'_'+#hasPayed+'_'+#name+'_'+#age").getValue(context));
    }

    public static String reverseString(String input) {
        StringBuilder backwards = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            backwards.append(input.charAt(input.length() - 1 - i));
        }
        return backwards.toString();
    }

    @Test
    public void test5() {
        Inventor tesla = new Inventor("Nikola Tesla", new Date(), "Serbian");
        EvaluationContext context = new StandardEvaluationContext(tesla);

        // null时取默认值及null时安全调用
        logger.info(parser.parseExpression("name?:'Unknown'").getValue(context, String.class));
        logger.info(Objects.requireNonNull(parser.parseExpression("name?.length()").getValue(context, Integer.class)).toString());

        tesla.setName(null);
        logger.info(parser.parseExpression("name?:'Unknown'").getValue(context, String.class));
        logger.info(String.valueOf(parser.parseExpression("name?.length()").getValue(context, Integer.class)));
    }

    @Test
    @SuppressWarnings("ALL")
    public void test6() {
        Inventor tesla = new Inventor("Nikola Tesla", new Date(), "Bei Jin");
        Inventor tesla1 = new Inventor("Nikola Tesla", new Date(), "Shang Hai");
        Inventor tesla2 = new Inventor("Nikola Tesla", new Date(), "New York");
        Inventor tesla3 = new Inventor("Nikola Tesla", new Date(), "Serbian");
        Inventor tesla4 = new Inventor("Nikola Tesla", new Date(), "Serbian");
        List<Inventor> inventors = Arrays.asList(tesla, tesla1, tesla2, tesla3, tesla4);
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("inventors", inventors);

        // 对List做各类运算
        List<Inventor> list = (List<Inventor>) parser.parseExpression("#inventors.?[serbian=='Serbian']").getValue(context);
        logger.info(list.toString());

        List<Inventor> list1 = (List<Inventor>) parser.parseExpression("#inventors.![serbian]").getValue(context);
        logger.info(list1.toString());

        List<Inventor> list2 = (List<Inventor>) parser.parseExpression("#inventors.![serbian=='Serbian']").getValue(context);
        logger.info(list2.toString());

        List<Inventor> list3 = (List<Inventor>) parser.parseExpression("#inventors.![#this.getSerbian()]").getValue(context);
        logger.info(list3.toString());
    }

    @Test
    public void test7() {
        // 访问map
        EvaluationContext context = new StandardEvaluationContext();
        Map<String, Object> params = new HashMap<>(1, 1);
        params.put("backOrderCode", "H12345764564");
        context.setVariable("params", params);
        Object object = parser.parseExpression("#params[backOrderCode]").getValue(context);
        logger.info((String) object);
    }

}

