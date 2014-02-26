package org.onebusaway.nyc.admin.service.bundle.task;

import java.io.File;
import java.util.Collection;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.nyc.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.conveyal.gtfs.model.Statistic;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;

public class GtfsStatisticsTask implements Runnable {
  private static final String FILENAME = "gtfs_stats.csv";
  private Logger _log = LoggerFactory.getLogger(GtfsStatisticsTask.class);
  private static final String ALL_AGENCIES = "TOTAL";
  private GtfsMutableRelationalDao _dao;
  private FederatedTransitDataBundle _bundle;
  @Autowired
  private MultiCSVLogger logger;

  
  public void setLogger(MultiCSVLogger logger) {
    this.logger = logger;
  }

  
    @Autowired
    public void setGtfsDao(GtfsMutableRelationalDao dao) {
      _dao = dao;
    }

    @Autowired
    public void setBundle(FederatedTransitDataBundle bundle) {
      _bundle = bundle;
    }
    
    public void run() {
      File basePath = _bundle.getPath();
      _log.info("Starting GTFS stats to basePath=" + basePath);
      GtfsStatisticsService service = new GtfsStatisticsService(_dao);
      
      logger.header(FILENAME, "agency,route_count,trip_count,stop_count,stop_times_count,calendar_service_start,calendar_service_end,calendar_start_date,calendar_end_date");
      
      // per agency status
      Collection<Agency> agencies = service.getAllAgencies();
      for (Agency agency:agencies) {
        _log.info("processing stats for agency: " + agency.getId() + " (" + agency.getName() + ")");
        logger.logCSV(FILENAME, service.getStatisticAsCSV(agency.getId()));
      }

      // overall stats/totals
      Statistic all = new Statistic();
      Agency allAgency = new Agency();
      allAgency.setId(ALL_AGENCIES);
      all.setAgencyId(ALL_AGENCIES);
      all.setRouteCount(service.getRouteCount());
      all.setTripCount(service.getTripCount());
      all.setStopCount(service.getStopCount());
      all.setStopTimeCount(service.getStopTimesCount());
      all.setCalendarStartDate(service.getCalendarServiceRangeStart());
      all.setCalendarEndDate(service.getCalendarServiceRangeEnd());

      logger.logCSV(FILENAME, GtfsStatisticsService.formatStatisticAsCSV(all));
      _log.info("exiting");
    }
}
