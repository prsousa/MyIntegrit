package myintegrit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.productivity.java.syslog4j.Syslog;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 *
 * @author Paulo
 *
 *
 * sudo vim /etc/rsyslog.conf
 *
 * $ActionQueueType LinkedList # use asynchronous processing
 * $ActionQueueFileName srvrfwd # set file name, also enables disk mode *
 * $ActionResumeRetryCount -1 # infinite retries on insert failure
 * $ActionQueueSaveOnShutdown on # save in-memory data if rsyslog shuts down .
 *
 * @@chronos34.no-ip.org:5555
 */
public class MyIntegrit {
    
    public static String RootFolder;
    public static int Interval;
    public static String RemoteServer;
    private static int RemoteServerPort;
    

    private static void readConfigurations() throws IOException {
        FileInputStream fis = new FileInputStream("/etc/myintegrit.conf");
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        
        
        
        Map<String, String> configs = new HashMap<>();
        
        configs.put("rootfolder", "/home/prsousa/Public");
        configs.put("interval", "5");
        configs.put("remoteserver", "");
        configs.put("remoteserverport", "0");
        
        
        String line;
        while( (line = br.readLine()) != null ) {
            String tokens[] = line.split(":");
            if(tokens.length == 2) {
                configs.put(tokens[0].toLowerCase().trim(), tokens[1].trim());
            }
        }
        
        
        MyIntegrit.RootFolder = configs.get("rootfolder");
        MyIntegrit.Interval = Integer.valueOf(configs.get("interval"));
        MyIntegrit.RemoteServer = configs.get("remoteserver");
        MyIntegrit.RemoteServerPort = Integer.valueOf(configs.get("remoteserverport"));
        
        br.close();
        fis.close();
        
        if( wantReportRemoteServer() ) {
            Syslog.getInstance("tcp").getConfig().setHost(MyIntegrit.RemoteServer);
            Syslog.getInstance("tcp").getConfig().setPort(RemoteServerPort);
        }
        
        Syslog.getInstance("unix_syslog").warn(configs.toString());
    }
    
    public static boolean wantReportRemoteServer() {
        return MyIntegrit.RemoteServer.length() > 3;
    }

    public static void logDifferences(Collection<Difference> differences) {
        boolean remoteServer = MyIntegrit.wantReportRemoteServer();
        
        for (Difference diff : differences) {
            if (diff instanceof AlertDiff) {
                if(remoteServer) Syslog.getInstance("tcp").alert(diff.getMessage());
                Syslog.getInstance("unix_syslog").alert(diff.getMessage());

            } else if (diff instanceof CriticalDiff) {
                if(remoteServer) Syslog.getInstance("tcp").critical(diff.getMessage());
                Syslog.getInstance("unix_syslog").critical(diff.getMessage());
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        System.out.close();
        System.err.close();
        
        try {
            System.in.close();
        } catch (IOException ex) { }
        
        
        try {
            readConfigurations();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return;
        }
        

        Signal.handle(new Signal("HUP"), new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                try {
                    readConfigurations();
                } catch (IOException ex) { }
            }
        });

        

        try {
            Filler fillerA = new Filler();
            Map<String, FileInfo> prev = fillerA.getDatabase(RootFolder);

            while (true) {
                Thread.sleep(Interval * 1000);

                Filler fillerB = new Filler();
                Map<String, FileInfo> next = fillerB.getDatabase(RootFolder);

                Collection<Difference> differences = Filler.getDifferences(prev, next);

                prev = next;

                logDifferences(differences);
            }

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(MyIntegrit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
