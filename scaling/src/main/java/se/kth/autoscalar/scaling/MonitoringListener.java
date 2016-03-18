package se.kth.autoscalar.scaling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kth.autoscalar.common.monitoring.MachineMonitoringEvent;
import se.kth.autoscalar.common.monitoring.ResourceMonitoringEvent;
import se.kth.autoscalar.scaling.core.ElasticScalarAPI;
import se.kth.autoscalar.scaling.exceptions.ElasticScalarException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class MonitoringListener {

    Log log = LogFactory.getLog(MonitoringListener.class);

    ElasticScalarAPI elasticScalarAPI;

    public MonitoringListener(ElasticScalarAPI elasticScalarAPI) throws ElasticScalarException {
        this.elasticScalarAPI = elasticScalarAPI;
    }

    /*start of resource monitoring related methods*/
    public void onHighCPU(String groupId, ResourceMonitoringEvent event) throws ElasticScalarException {
        try {
            elasticScalarAPI.handleEvent(groupId, event);
        } catch (ElasticScalarException e) {
            log.error("Error while handling onHighCPU event for group: " + groupId + " . " + e.getMessage());
            throw e;
        }
    }

    public void onLowCPU(String groupId, ResourceMonitoringEvent event) {

    }

    public void onHighRam(String groupId, ResourceMonitoringEvent event) throws ElasticScalarException {
        try {
            elasticScalarAPI.handleEvent(groupId, event);
        } catch (ElasticScalarException e) {
            log.error("Error while handling onHighCPU event for group: " + groupId + " . " + e.getMessage());
            throw e;
        }
    }

    public void onLowRam(String groupId, ResourceMonitoringEvent event) {

    }

  /*start of machine monitoring related methods*/
  public void onStateChange(String groupId, MachineMonitoringEvent event) throws ElasticScalarException {
      try {
          elasticScalarAPI.handleEvent(groupId, event);
      } catch (ElasticScalarException e) {
          log.error("Error while handling stateChange event for group: " + groupId + " with state change: " +
                  event.getStatus() + " . " + e.getMessage());
          throw e;
      }
  }

}
