package org.javamaster.b2c.core.java8;

import static java.util.stream.Collectors.groupingBy;
import org.javamaster.b2c.core.model.java8.Transaction;
import org.javamaster.b2c.core.model.java8.TransactionVo;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class StreamTest extends CommonCode {

    @Test
    public void testJava7() {
        List<Transaction> tmpTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getValue() > 1500) {
                tmpTransactions.add(transaction);
            }
        }
        Collections.sort(tmpTransactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction o1, Transaction o2) {
                int i = Integer.compare(o2.getYear(), o1.getYear());
                if (i != 0) {
                    return i;
                }
                return Integer.compare(o1.getMonth(), o2.getMonth());
            }
        });
        List<TransactionVo> tmpTransactionVos = new ArrayList<>(tmpTransactions.size());
        for (Transaction tmpTransaction : tmpTransactions) {
            TransactionVo transactionVo = new TransactionVo();
            BeanUtils.copyProperties(tmpTransaction, transactionVo);
            tmpTransactionVos.add(transactionVo);
        }
        Map<String, List<TransactionVo>> map = new HashMap<>();
        for (TransactionVo tmpTransactionVo : tmpTransactionVos) {
            List<TransactionVo> list = map.get(tmpTransactionVo.getCurrency());
            if (list == null) {
                list = new ArrayList<>();
                map.put(tmpTransactionVo.getCurrency(), list);
            }
            list.add(tmpTransactionVo);
        }
        System.out.println(map);
    }

    @Test
    public void testJava8() {
        Map<String, List<TransactionVo>> map = transactions.stream()
                .filter(transaction -> transaction.getValue() > 1500)
                .sorted(Comparator.comparing(Transaction::getYear).reversed().thenComparing(Transaction::getMonth))
                .map(transaction -> {
                    TransactionVo transactionVo = new TransactionVo();
                    BeanUtils.copyProperties(transaction, transactionVo);
                    return transactionVo;
                })
                .collect(groupingBy(TransactionVo::getCurrency));
        System.out.println(map);
    }
}
