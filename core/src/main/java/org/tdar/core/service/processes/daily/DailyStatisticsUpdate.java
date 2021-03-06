package org.tdar.core.service.processes.daily;

import org.hibernate.stat.Statistics;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.StatisticsService;
import org.tdar.core.service.processes.AbstractScheduledProcess;

/**
 * $Id$
 * 
 * ScheduledProcess to update aggregate stats
 * 
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */

@Component
@Scope("prototype")
public class DailyStatisticsUpdate extends AbstractScheduledProcess {

    private static final long serialVersionUID = 840474044233878010L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient StatisticsService statisticService;

    @Autowired
    private transient GenericService genericService;

    @Override
    public String getDisplayName() {
        return "Daily statistics service";
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public boolean shouldRunAtStartup() {
        return true;
    }

    @Override
    public void execute() {
        Statistics sessionStatistics = genericService.getSessionStatistics();
        sessionStatistics.clear();
        logger.info("adding statistics");
        DateTime date = DateTime.now().minusDays(1);
        statisticService.initializeNewAggregateEntries(date);
        statisticService.generateMonthlyResourceStats(date);
        // delete old stats
        statisticService.cleanupOldDailyStats(DateTime.now().minusMonths(1).toDate());
        statisticService.generateAggregateDailyDownloadData(DateTime.now().minusDays(1).toDate());
        logger.info("done daily stats");
    }

    /**
     * This ScheduledProcess is finished to completion after execute().
     */
    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
