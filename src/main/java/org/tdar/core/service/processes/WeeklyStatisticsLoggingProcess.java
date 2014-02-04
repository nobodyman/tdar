package org.tdar.core.service.processes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.resource.ResourceService;

@Component
public class WeeklyStatisticsLoggingProcess extends ScheduledProcess.Base<HomepageGeographicKeywordCache> {

    private static final long serialVersionUID = 6866081834770368244L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient GenericService genericService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    int batchCount = 0;
    boolean run = false;

    @Override
    public void execute() {
        run = true;
        logger.info("generating weekly stats");
        List<AggregateStatistic> stats = new ArrayList<>();
        stats.add(generateStatistics(StatisticType.NUM_PROJECT, resourceService.countActiveResources(ResourceType.PROJECT), ""));
        stats.add(generateStatistics(StatisticType.NUM_DOCUMENT, resourceService.countActiveResources(ResourceType.DOCUMENT), ""));
        stats.add(generateStatistics(StatisticType.NUM_DATASET, resourceService.countActiveResources(ResourceType.DATASET), ""));
        stats.add(generateStatistics(StatisticType.NUM_VIDEO, resourceService.countActiveResources(ResourceType.VIDEO), ""));
        stats.add(generateStatistics(StatisticType.NUM_CODING_SHEET, resourceService.countActiveResources(ResourceType.CODING_SHEET), ""));
        stats.add(generateStatistics(StatisticType.NUM_SENSORY_DATA, resourceService.countActiveResources(ResourceType.SENSORY_DATA), ""));
        stats.add(generateStatistics(StatisticType.NUM_ONTOLOGY, resourceService.countActiveResources(ResourceType.ONTOLOGY), ""));
        stats.add(generateStatistics(StatisticType.NUM_IMAGE, resourceService.countActiveResources(ResourceType.IMAGE), ""));
        stats.add(generateStatistics(StatisticType.NUM_GIS, resourceService.countActiveResources(ResourceType.GEOSPATIAL), ""));
        stats.add(generateStatistics(StatisticType.NUM_ARCHIVES, resourceService.countActiveResources(ResourceType.ARCHIVE), ""));
        stats.add(generateStatistics(StatisticType.NUM_AUDIO, resourceService.countActiveResources(ResourceType.AUDIO), ""));

        stats.add(generateStatistics(StatisticType.NUM_DOCUMENT_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.DOCUMENT), ""));
        stats.add(generateStatistics(StatisticType.NUM_DATASET_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.DATASET), ""));
        stats.add(generateStatistics(StatisticType.NUM_VIDEO_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.VIDEO), ""));
        stats.add(generateStatistics(StatisticType.NUM_CODING_SHEET_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.CODING_SHEET), ""));
        stats.add(generateStatistics(StatisticType.NUM_SENSORY_DATA_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.SENSORY_DATA), ""));
        stats.add(generateStatistics(StatisticType.NUM_ONTOLOGY_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.ONTOLOGY), ""));
        stats.add(generateStatistics(StatisticType.NUM_IMAGE_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.IMAGE), ""));
        stats.add(generateStatistics(StatisticType.NUM_GIS_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.GEOSPATIAL), ""));
        stats.add(generateStatistics(StatisticType.NUM_ARCHIVES_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.ARCHIVE), ""));
        stats.add(generateStatistics(StatisticType.NUM_AUDIO_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.AUDIO), ""));

        stats.add(generateStatistics(StatisticType.NUM_USERS, entityService.findAllRegisteredUsers().size(), ""));
        stats.add(generateStatistics(StatisticType.NUM_ACTUAL_CONTRIBUTORS, entityService.findNumberOfActualContributors(), ""));
        stats.add(generateStatistics(StatisticType.NUM_COLLECTIONS, resourceCollectionService.findAllResourceCollections().size(), ""));
        long repositorySize = TdarConfiguration.getInstance().getFilestore().getSizeInBytes();
        stats.add(generateStatistics(StatisticType.REPOSITORY_SIZE, Long.valueOf(repositorySize), FileUtils.byteCountToDisplaySize(repositorySize)));
        genericService.saveOrUpdate(stats);
    }

    protected AggregateStatistic generateStatistics(AggregateStatistic.StatisticType statisticType, Number value, String comment) {
        AggregateStatistic stat = new AggregateStatistic();
        stat.setRecordedDate(new Date());
        stat.setStatisticType(statisticType);
        stat.setComment(comment);
        stat.setValue(value.longValue());
        logger.info("stat: {}", stat);
        return stat;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Weekly System Statistics Task";
    }

    @Override
    public Class<HomepageGeographicKeywordCache> getPersistentClass() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        return run;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

}
