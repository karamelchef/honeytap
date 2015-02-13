/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.command;

import dnl.utils.text.table.TextTable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.ClusterManager;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.running.model.ClusterEntity;
import se.kth.karamel.backend.running.model.GroupEntity;
import se.kth.karamel.backend.running.model.MachineEntity;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.common.ClasspathResourceUtil;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class CommandService {

  private static String chosenCluster = null;
  private static final ClusterService clusterService = ClusterService.getInstance();
//  private static final KaramelApi api = new KaramelApiImpl();

  public static String processCommand(String command) {
    String cmd = command.toLowerCase();
    String result = "command not found";
    if (cmd.equals("help")) {
      try {
        result = ClasspathResourceUtil.readContent("se/kth/karamel/backend/command/cheatsheet");
      } catch (IOException ex) {
        result = "sorry, couldn't load the cheatsheet";
      }
    } else if (cmd.equals("clusters")) {
      result = clusters();
    } else if (cmd.equals("yaml")) {
      if (chosenCluster != null) {
        ClusterManager cluster = cluster(chosenCluster);
        JsonCluster json = cluster.getDefinition();
        try {
          result = ClusterDefinitionService.jsonToYaml(json);
        } catch (KaramelException ex) {
          result = "sorry couldn't load the yaml";
        }
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("pause")) {
      if (chosenCluster != null) {
        try {
          clusterService.pauseCluster(chosenCluster);
          result = "Pausing the installation...";
        } catch (KaramelException ex) {
          result = ex.getMessage();
        }
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("resume")) {
      if (chosenCluster != null) {
        try {
          clusterService.resumeCluster(chosenCluster);
          result = "Resuming the installation...";
        } catch (KaramelException ex) {
          result = ex.getMessage();
        }
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("purge")) {
      if (chosenCluster != null) {
        try {
          clusterService.pauseCluster(chosenCluster);
          result = "Purging " + chosenCluster;
        } catch (KaramelException ex) {
          result = ex.getMessage();
        }
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("status")) {
      if (chosenCluster != null) {
        StringBuilder builder = new StringBuilder();
        ClusterManager cluster = cluster(chosenCluster);
        ClusterEntity clusterEntity = cluster.getRuntime();
        builder.append(clusterEntity.getName()).append(" is ").append(clusterEntity.getPhase());
        if (clusterEntity.isPaused()) {
          builder.append(" but it is paused.");
        }
        if (clusterEntity.isFailed()) {
          builder.append(" Some failures have happened.");
        }
        builder.append("\n");
        builder.append(machinesTasksTable(clusterEntity));
        result = builder.toString();
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("detail")) {
      if (chosenCluster != null) {
        ClusterManager cluster = cluster(chosenCluster);
        JsonCluster json = cluster.getDefinition();

        try {
          result = ClusterDefinitionService.serializeJson(json);
        } catch (KaramelException ex) {
          result = "sorry couldn't load the yaml";
        }
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("groups")) {
      if (chosenCluster != null) {
        ClusterManager cluster = cluster(chosenCluster);
        ClusterEntity clusterEntity = cluster.getRuntime();
        String[] columnNames = {"Name", "Phase", "Failed"};
        String[][] data = new String[clusterEntity.getGroups().size()][3];
        for (int i = 0; i < clusterEntity.getGroups().size(); i++) {
          GroupEntity group = clusterEntity.getGroups().get(i);
          data[i][0] = group.getName();
          data[i][1] = String.valueOf(group.getPhase());
          data[i][2] = String.valueOf(group.isFailed());
        }
        result = makeTable(columnNames, 2, data, true);
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("machines")) {
      if (chosenCluster != null) {
        ClusterManager cluster = cluster(chosenCluster);
        ClusterEntity clusterEntity = cluster.getRuntime();
        ArrayList<MachineEntity> machines = new ArrayList<>();
        for (GroupEntity group : clusterEntity.getGroups()) {
          for (MachineEntity machine : group.getMachines()) {
            machines.add(machine);
          }
        }
        result = machinesTable(machines, true);
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("tasks")) {
      if (chosenCluster != null) {
        ClusterManager cluster = cluster(chosenCluster);
        ClusterEntity clusterEntity = cluster.getRuntime();
        result = machinesTasksTable(clusterEntity);
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else {
      boolean found = false;
      Pattern p = Pattern.compile("use\\s+(\\w+)");
      Matcher matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        if (cluster(clusterName) != null) {
          chosenCluster = clusterName;
          result = String.format("switched to %s now", clusterName);
        } else {
          result = String.format("cluster %s is not registered yet!!", clusterName);
        }
      }

      p = Pattern.compile("which\\s+(cluster|ec2|ssh)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String subcmd = matcher.group(1);
        if (subcmd.equals("cluster")) {
          if (chosenCluster != null) {
            result = String.format("%s has been chosen.", chosenCluster);
          } else {
            result = "no cluster has been chosen yet!!";
          }
        } else if (subcmd.equals("ec2")) {
          Ec2Context ec2Context = clusterService.getCommonContext().getEc2Context();
          if (ec2Context != null) {
            result = String.format("ec2 account id is %s", ec2Context.getCredentials().getAccountId());
          } else {
            result = "no ec2 account has been chosen yet!!";
          }
        } else if (subcmd.equals("ssh")) {
          SshKeyPair sshKeyPair = clusterService.getCommonContext().getSshKeyPair();
          if (sshKeyPair != null) {
            result = String.format("public key path: %s \nprivate key path: %s", sshKeyPair.getPublicKeyPath(), sshKeyPair.getPrivateKeyPath());
          } else {
            result = "no ssh keys has been chosen yet!!";
          }
        }
      }
    }
    return result;
  }

  private static ClusterManager cluster(String name) {
    Map<String, ClusterManager> repository = clusterService.getRepository();
    Set<Map.Entry<String, ClusterManager>> clusters = repository.entrySet();
    for (Map.Entry<String, ClusterManager> cluster : clusters) {
      if (cluster.getKey().toLowerCase().equals(name.toLowerCase())) {
        return cluster.getValue();
      }
    }
    return null;
  }

  private static String clusters() {
    String result;
    Map<String, ClusterManager> repository = clusterService.getRepository();
    Set<String> keySet = repository.keySet();
    if (!keySet.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (String name : keySet) {
        builder.append(name).append("\n");
      }
      result = builder.toString();
    } else {
      result = "No cluster is registered yet..";
    }
    return result;
  }

  private static String tasksTable(List<Task> tasks, boolean rowNumbering) {
    String[] columnNames = {"Task", "Status", "Machine"};
    Object[][] data = new Object[tasks.size()][columnNames.length];
    for (int i = 0; i < tasks.size(); i++) {
      Task task = tasks.get(i);
      data[i][0] = task.getName();
      data[i][1] = task.getStatus();
      data[i][2] = task.getMachineId();
    }
    return makeTable(columnNames, 1, data, rowNumbering);
  }

  private static String machinesTable(ArrayList<MachineEntity> machines, boolean rowNumbering) {
    String[] columnNames = {"Group", "Public IP", "Private IP", "SSH Port", "SSH User", "Life Status", "Task Status"};
    Object[][] data = new Object[machines.size()][columnNames.length];
    for (int i = 0; i < machines.size(); i++) {
      MachineEntity machine = machines.get(i);
      data[i][0] = machine.getGroup().getName();
      data[i][1] = machine.getPublicIp();
      data[i][2] = machine.getPrivateIp();
      data[i][3] = machine.getSshPort();
      data[i][4] = machine.getSshUser();
      data[i][5] = machine.getLifeStatus();
      data[i][6] = machine.getTasksStatus();
    }
    return makeTable(columnNames, 6, data, rowNumbering);
  }

  private static String makeTable(String[] columnNames, int sortIndex, Object[][] data, boolean rowNumbering) {
    TextTable tt = new TextTable(columnNames, data);
// this adds the numbering on the left      
    tt.setAddRowNumbering(rowNumbering);
// sort by the first column                              
    tt.setSort(sortIndex);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(os);
    tt.printTable(ps, 0);
    ps.flush();
    return new String(os.toByteArray(), Charset.forName("utf8"));
  }

  private static String machinesTasksTable(ClusterEntity clusterEntity) {
    StringBuilder builder = new StringBuilder();
    for (GroupEntity group : clusterEntity.getGroups()) {
      for (MachineEntity machine : group.getMachines()) {
        ArrayList<MachineEntity> machines = new ArrayList<>();
        machines.add(machine);
        builder.append(machinesTable(machines, false));
        builder.append("\n");
        builder.append(tasksTable(machine.getTasks(), true));
        builder.append("\n");
      }
    }
    return builder.toString();
  }

}