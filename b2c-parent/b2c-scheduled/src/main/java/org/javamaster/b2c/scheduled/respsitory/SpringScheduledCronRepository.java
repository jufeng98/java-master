package org.javamaster.b2c.scheduled.respsitory;

import org.javamaster.b2c.scheduled.entity.SpringScheduledCron;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yudong
 * @date 2019/8/24
 */
public interface SpringScheduledCronRepository extends JpaRepository<SpringScheduledCron, Integer> {

    SpringScheduledCron findByCronId(Integer cronId);

    SpringScheduledCron findByCronKey(String cronKey);

    /**
     * 更新定时任务cron表达式
     *
     * @param newCron
     * @param cronKey
     * @return
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update spring_scheduled_cron set cron_expression=?1 where cron_key=?2", nativeQuery = true)
    int updateCronExpressionByCronKey(String newCron, String cronKey);

    /**
     * 更新定时任务状态
     *
     * @param status
     * @param cronKey
     * @return
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update spring_scheduled_cron set status=?1 where cron_key=?2", nativeQuery = true)
    int updateStatusByCronKey(Integer status, String cronKey);

}
