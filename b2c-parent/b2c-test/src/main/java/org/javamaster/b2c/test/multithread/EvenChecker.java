package org.javamaster.b2c.test.multithread;

/**
 * @author yudong
 * @date 2019/12/10
 */
public class EvenChecker implements Runnable {

    @Override
    public void run() {
        EvenGeneratorFactory evenGeneratorFactory = EvenGeneratorFactory.newInstance();
        while (!evenGeneratorFactory.isBroken()) {
            int number = evenGeneratorFactory.next();
            if (number % 2 != 0) {
                System.err.println(number + "不是一个偶数!");
                evenGeneratorFactory.setBroken(true);
            }
        }
    }

}
