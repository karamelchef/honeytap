package se.kth.honeytap.scaling.models;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class MachineType {

    public enum Properties {
        TYPE, BIDDING_PRICE;
    }

    public enum EC2Properties {
       AMI, REGION, SUBNET, VPC, TYPE;
    }

    private String launcher;
    private boolean isPreemptible;
    private Map<String,String> properties;

    public MachineType(String launcher, boolean isPreemptible, Map<String,String> properties) {
        this.launcher = launcher;
        this.isPreemptible = isPreemptible;
        this.properties = properties;
    }

    public String getLauncher() {
        return launcher;
    }

    public boolean isPreemptible() {
        return isPreemptible;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean isValid() {
        boolean isValid = true;
        if (isPreemptible) {
            String bidPriceString = getProperty(Properties.BIDDING_PRICE.name());
            if (bidPriceString == null) {
                isValid = false;
            } else {
                try {
                    Double bid = Double.valueOf(bidPriceString);
                } catch (NumberFormatException e) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

}
