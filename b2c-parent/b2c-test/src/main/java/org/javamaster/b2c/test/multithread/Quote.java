package org.javamaster.b2c.test.multithread;

/**
 * @author yudong
 * @date 2019/1/8
 */
public class Quote {
    private final String shopName;
    private final double price;
    private final Discount.MemberLevelEnum memberLevelEnum;

    public Quote(String shopName, double price, Discount.MemberLevelEnum memberLevelEnum) {
        this.shopName = shopName;
        this.price = price;
        this.memberLevelEnum = memberLevelEnum;
    }

    /**
     * 解析shop对象返回的字符串，可以得到Quote类的一个实例，它包含了shop的名称、折扣之前的价格，以及会员等级
     */
    public static Quote parse(String s) {
        String[] split = s.split(":");
        String shopName = split[0];
        double price = Double.parseDouble(split[1]);
        Discount.MemberLevelEnum memberLevelEnum = Discount.MemberLevelEnum.valueOf(split[2]);
        return new Quote(shopName, price, memberLevelEnum);
    }

    public String getShopName() {
        return shopName;
    }

    public double getPrice() {
        return price;
    }

    public Discount.MemberLevelEnum getMemberLevelEnum() {
        return memberLevelEnum;
    }
}
