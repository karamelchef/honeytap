package se.kth.autoscalar.scaling.cost.mgt;

import se.kth.autoscalar.scaling.group.Group;
import se.kth.autoscalar.scaling.models.MachineType;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class KaramelMachineProposer implements MachineProposer {

    public MachineType[] getMachineProposals(String groupId, Map<Group.ResourceRequirement, Integer> minimumResourceReq,
                                             int noOfMachines, float reliabilityPercentage) {
        throw new UnsupportedOperationException("#getMachineProposals()");
    }
}
