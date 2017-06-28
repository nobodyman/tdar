package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.statistics.AggregateViewStatistic;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * The AggregateStatisticsDao works with the two main aggregate statistics tables:
 * * resource_access_day_agg
 * * file_download_day_agg
 * 
 * These two tables are populated by the DailyStatisticsUpdate which runs daily as a scheduled process pulling the last day's data into the tables aggregating
 * it at the "day," with columns for the year and month which allow for indexing and faster querying. These tables can then be queried to get historic data. The
 * goal of these tables is mainly for performance. Each table has the following columns:
 * * Date Accessed
 * * Count (count of views that day)
 * * Month
 * * Year
 * * Reference Id (Resource or InformationResourceFile)
 * 
 * @author abrin
 *
 */
@Component
public class AggregateStatisticsDao extends GenericDao {

    /**
     * Returns access and download statistics for the set of resources for every year since 2010. Downloads are aggregated to the Resource Id.
     * 
     * @param resourceIds
     * @param provider
     * @return
     */
    public StatsResultObject getAnnualStats(Collection<Long> resourceIds, TextProvider provider) {
        int start = 2010;
        int end = DateTime.now().getYear();
        List<String> labelKeys = new ArrayList<>();
        String sql = buildAnnualQueryAndLabels(provider, start, end, labelKeys);
        StatsResultObject results = populateResultsObject(resourceIds, labelKeys, sql);
        return results;
    }

    /**
     * Returns access and download statistics for the set of resources for the last 12 months from the date specified. Downloads are aggregated to the Resource
     * Id.
     * 
     * @param resourceIds
     * @param provider
     * @return
     */
    public StatsResultObject getMonthlyStats(Collection<Long> resourceIds, TextProvider provider) {
        DateTime lastYear = DateTime.now().minusYears(1);
        List<String> labelKeys = new ArrayList<>();
        String sql = buildMonthQueryAndLabels(provider, lastYear.toDate(), labelKeys);
        StatsResultObject results = populateResultsObject(resourceIds, labelKeys, sql);
        return results;
    }

    /**
     * Returns access and download statistics for the set of resources for the last 7 days available (current day is not available as it's not in these tables).
     * Downloads are aggregated to the Resource Id.
     * 
     * @param ids
     * @param provider
     * @return
     */
    public StatsResultObject getDailyStats(Collection<Long> ids, TextProvider provider) {
        DateTime lastYear = DateTime.now().minusDays(8);
        List<String> labelKeys = new ArrayList<>();
        String sql = buildDayQueryAndLabels(provider, lastYear.toDate(), labelKeys);
        StatsResultObject results = populateResultsObject(ids, labelKeys, sql);
        return results;
    }

    /**
     * Migrate daily download stats into aggregate tables
     * 
     * @param date
     */
    public void generateAggregateDailyDownloadData(Date date) {
        String sql = String.format(TdarNamedQueries.DAILY_DOWNLOAD_UPDATE, date);
        getCurrentSession().createSQLQuery(sql).executeUpdate();

    }

    /**
     * Migrate daily view stats into aggregate tables
     * 
     * @param date
     */
    public void generateAggregateDailyResourceData(Date date) {
        String sql = String.format(TdarNamedQueries.DAILY_RESOURCE_UPDATE, date);
        getLogger().trace(sql);
        getCurrentSession().createSQLQuery(sql).executeUpdate();
    }

    /**
     * For the set of resourceIds, the SQL Query, and labels, run the query and populate the StatsResultObject that's passed back to the controller
     * 
     * @param resourceIds
     * @param labelKeys
     * @param sql
     * @return
     */
    @SuppressWarnings("unchecked")
    private StatsResultObject populateResultsObject(Collection<Long> resourceIds, List<String> labelKeys, String sql) {
        Query query = getCurrentSession().createSQLQuery(sql);
        query.setParameterList("ids", resourceIds);
        StatsResultObject results = new StatsResultObject();
        for (Object[] row : (List<Object[]>) query.list()) {
            List<Number> numbers = new ArrayList<>();
            Resource resource = new Resource(((Number) row[0]).longValue(), (String) row[1], ResourceType.valueOf((String) row[2]), "",
                    Status.valueOf((String) row[3]));
            ResourceStatWrapper rowData = new ResourceStatWrapper();
            rowData.setResource(resource);
            rowData.setData(numbers);

            for (int j = 4; j < row.length; j++) {
                numbers.add((Number) row[j]);
            }
            results.addRowData(rowData);
        }
        results.setRowLabels(labelKeys);
        return results;
    }

    /**
     * Generate aggregate stats for the 7 days starting with the date specified
     * 
     * @param provider
     * @param startYear
     * @param startMonth
     * @param startDay
     * @param labelKeys
     * @return
     */
    private String buildDayQueryAndLabels(TextProvider provider, Date start_, List<String> labelKeys) {
        StringBuilder viewSubQuerypart = new StringBuilder();
        StringBuilder downloadSubQuerypart = new StringBuilder();
        List<String> labelDownloadKeys = new ArrayList<>();
        DateTime start = new DateTime(start_);
        for (int i = 0; i < 7; i++) {
            if (i != 0) {
                viewSubQuerypart.append(", ");
            }
            downloadSubQuerypart.append(", ");

            start = start.plusDays(1);
            String date = start.toString("YYYY-MM-dd");
            viewSubQuerypart.append(String.format(TdarNamedQueries.DAY_VIEW_PART, date, start.getDayOfMonth(), start.getYear(), start.getMonthOfYear()));
            downloadSubQuerypart.append(String.format(TdarNamedQueries.DAY_DOWNLAOD_PART, date));
            labelKeys.add(provider.getText("statisticsService.view_count_day", Arrays.asList(date)));
            labelDownloadKeys.add(provider.getText("statisticsService.download_count_day", Arrays.asList(date)));
        }

        labelKeys.addAll(labelDownloadKeys);

        String sql = String.format(TdarNamedQueries.ANNUAL_ACCESS_SKELETON, viewSubQuerypart.toString(), downloadSubQuerypart.toString());
        getLogger().trace(sql);
        return sql;
    }

    /**
     * Generate an aggregate query for the 12 months starting with the month and year specified
     * 
     * @param provider
     * @param startYear
     * @param startMonth
     * @param labelKeys
     * @return
     */
    private String buildMonthQueryAndLabels(TextProvider provider, Date start_, List<String> labelKeys) {
        StringBuilder viewSubQuerypart = new StringBuilder();
        StringBuilder downloadSubQuerypart = new StringBuilder();
        List<String> labelDownloadKeys = new ArrayList<>();
        int count = 0;
        DateTime start = new DateTime(start_);
        while (count < 12) {
            if (count != 0) {
                viewSubQuerypart.append(", ");
            }
            count++;
            int month = start.getMonthOfYear();
            int year = start.getYear();
            downloadSubQuerypart.append(", ");
            viewSubQuerypart.append(String.format(TdarNamedQueries.MONTH_VIEW_PART, month, year));
            downloadSubQuerypart.append(String.format(TdarNamedQueries.MONTH_DOWNLOAD_PART, month, year));
            labelKeys.add(provider.getText("statisticsService.view_count_month", Arrays.asList(month, year)));
            labelDownloadKeys.add(provider.getText("statisticsService.download_count_month", Arrays.asList(month, year)));
            start = start.plusMonths(1);
        }
        labelKeys.addAll(labelDownloadKeys);

        String sql = String.format(TdarNamedQueries.ANNUAL_ACCESS_SKELETON, viewSubQuerypart.toString(), downloadSubQuerypart.toString());
        getLogger().trace(sql);
        return sql;
    }

    /**
     * Generate annual, aggregate usage stats for the range of specified years.
     * 
     * @param provider
     * @param start
     * @param end
     * @param labelKeys
     * @return
     */
    private String buildAnnualQueryAndLabels(TextProvider provider, int start, int end, List<String> labelKeys) {
        int i = start;
        StringBuilder viewSubQuerypart = new StringBuilder();
        StringBuilder downloadSubQuerypart = new StringBuilder();
        List<String> labelDownloadKeys = new ArrayList<>();
        while (i <= end) {
            if (i != start) {
                viewSubQuerypart.append(", ");
            }
            downloadSubQuerypart.append(", ");
            viewSubQuerypart.append(String.format(TdarNamedQueries.ANNUAL_VIEW_PART, i));
            downloadSubQuerypart.append(String.format(TdarNamedQueries.ANNUAL_DOWNLOAD_PART, i));
            labelKeys.add(provider.getText("statisticsService.view_count_annual", Arrays.asList(i)));
            labelDownloadKeys.add(provider.getText("statisticsService.download_count_annual", Arrays.asList(i)));
            i++;
        }
        labelKeys.addAll(labelDownloadKeys);

        String sql = String.format(TdarNamedQueries.ANNUAL_ACCESS_SKELETON, viewSubQuerypart.toString(), downloadSubQuerypart.toString());
        getLogger().trace(sql);
        return sql;
    }

    public void cleanupOldDailyStats(Date date) {
        String sql = String.format(TdarNamedQueries.DAILY_RESOURCE_STATS_CLEANUP, date);
        getLogger().trace(sql);
        getCurrentSession().createSQLQuery(sql).executeUpdate();

    }

    /**
     * Creates the entries in the year aggregate table so that all other tasks can be update statements
     * 
     * @param date
     */
    public void createNewAggregateEntries(DateTime date) {
        Query query = getCurrentSession().createSQLQuery(TdarNamedQueries.AGG_RESOURCE_SETUP_MONTH);
        // query.setParameter("month", date.getMonthOfYear());
        query.setParameter("date", date.toDateMidnight().toDate());
        if (date.getMonthOfYear() == 1) {
            query.setParameter("date", DateTime.now().minusYears(100));
        }
        query.executeUpdate();
    }

    /**
     * inserts into the aggregate table the actual values for the last month
     * 
     * @param date
     */
    public void updateMonthly(DateTime date) {

        DateMidnight midnight = date.toDateMidnight();
        String sql = String.format(TdarNamedQueries.AGG_RESOURCE_INSERT_MONTH, date.getDayOfMonth(), "", midnight.toString("yyyy-MM-dd"),
                midnight.plusDays(1).toString("yyyy-MM-dd"));
        Query query = getCurrentSession().createSQLQuery(sql);
        query.setParameter("month", date.getMonthOfYear());
        query.setParameter("year", date.getYear());
        query.setParameter("bot", Boolean.FALSE);
        query.executeUpdate();

        sql = String.format(TdarNamedQueries.AGG_RESOURCE_INSERT_MONTH, date.getDayOfMonth() + "_bot", "_bot", midnight.toString("yyyy-MM-dd"),
                midnight.plusDays(1).toString("yyyy-MM-dd"));
        query = getCurrentSession().createSQLQuery(sql);
        query.setParameter("month", date.getMonthOfYear());
        query.setParameter("year", date.getYear() + 1900);
        query.setParameter("bot", Boolean.TRUE);
        query.executeUpdate();

    }

    public List<Resource> getWeeklyPopularResources(int count) {
        List<Resource> resources = new ArrayList<>();
        DateTime end = new DateTime();
        DateTime start = end.minusDays(7);
        String sql = String.format(TdarNamedQueries.WEEKLY_POPULAR, start.toString("yyyy-MM-dd"), end.toString("yyyy-MM-dd"), count);
        Query query = getCurrentSession().createSQLQuery(sql);
        List list = query.list();
        for (Object o : list) {
            Object[] obj = (Object[])o;
            Resource resource = find(Resource.class, ((Number) obj[0]).longValue());
            if (PersistableUtils.isNotNullOrTransient(resource)) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public List<WeeklyViewStatistic> getUsageStatsForResource(Date start_, Date end_, Long minCount, List<Long> resourceIds) {
        DateTime start = new DateTime(start_);
        DateTime end = new DateTime(end_);

        String sql = "select r.id, r.title, r.status, r.resource_type  ";
        if (start.getMonthOfYear() != end.getMonthOfYear()) {
            for (int d = start.getDayOfMonth(); d <= 31; d++) {
                sql += String.format(", coalesce( s1.d%s,0) ", d);
            }

            for (int d = 1; d <= end.getDayOfMonth(); d++) {
                sql += String.format(", coalesce( s2.d%s,0) ", d);
            }
        } else {
            for (int d = start.getDayOfMonth(); d <= end.getDayOfMonth(); d++) {
                sql += String.format(", coalesce( s1.d%s,0) ", d);
            }
        }
        sql += " from resource left join  resource_access_month_agg s1 on r.id=s1.resource_id ";

        if (start.getMonthOfYear() != end.getMonthOfYear()) {
            sql += "left join resource_access_month_agg s2 on r.id=s2.resource_id ";
        }
        sql += " where r.id in (:ids)";
        sql += String.format(" and s1.year=%s and s1.month=%s", start.getYear(), start.getMonthOfYear());

        if (start.getMonthOfYear() != end.getMonthOfYear()) {
            sql += String.format(" and s2.year=%s and s2.month=%s", end.getYear(), end.getMonthOfYear());
        }
        
        Query query = getCurrentSession().createSQLQuery(sql);
        List<WeeklyViewStatistic> results = new ArrayList<>();
        List list = query.list();
        for (Object o : list) {
            Object[] obj = (Object[])o;
            Long id = ((Number)obj[0]).longValue();
            String title = (String) obj[1];
            ResourceType rt = ResourceType.valueOf((String) obj[2]);
            Status stat = Status.valueOf((String) obj[3]);
            Integer d1 = ((Number)obj[4]).intValue();
            Integer d2 = ((Number)obj[5]).intValue();
            Integer d3 = ((Number)obj[6]).intValue();
            Integer d4 = ((Number)obj[7]).intValue();
            Integer d5 = ((Number)obj[8]).intValue();
            Integer d6 = ((Number)obj[9]).intValue();
            Integer d7 = ((Number)obj[10]).intValue();
            WeeklyViewStatistic wvs = new WeeklyViewStatistic(new Resource(id, title, rt, stat, -1L), d1, d2,d3,d4,d5,d6,d7, start, end);
            results.add(wvs);
        }

        return results;
    }

}
