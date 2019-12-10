package org.javamaster.b2c.test.multithread;

/**
 * @author yudong
 * @date 2019/12/10
 */
public class EvenGeneratorFactory {
    private static EvenGeneratorFactory evenGeneratorFactory = new EvenGeneratorFactory();
    /**
     * 损坏标志
     */
    private volatile boolean broken;
    private int currentValue = 0;

    private EvenGeneratorFactory() {
    }

    public static EvenGeneratorFactory newInstance() {
        return evenGeneratorFactory;
    }

    public int next() {
        try {
            currentValue++;
            currentValue++;
            return currentValue;
        } finally {
        }
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }
}
