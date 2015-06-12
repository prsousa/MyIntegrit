/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myintegrit;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.productivity.java.syslog4j.Syslog;

/**
 *
 * @author Paulo
 */
public class MyIntegrit {

    public final static String URL = "/Users/Paulo/Dropbox/Universidade/Mestrado/SDC/2semestre/ssi";

    public static void showDifferences(Collection<Difference> differences) {
        System.out.println("Resultados");

        for (Difference diff : differences) {
            if (diff instanceof AlertDiff) {
                Syslog.getInstance("tcp").alert(diff.getMessage());
            } else if (diff instanceof CriticalDiff) {
                Syslog.getInstance("tcp").critical(diff.getMessage());
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Syslog.getInstance("tcp").getConfig().setHost("chronos34.no-ip.org");
        Syslog.getInstance("tcp").getConfig().setPort(5555);

        try {
            Filler fillerA = new Filler();
            Map<String, FileInfo> prev = fillerA.getDatabase(URL);

            while (true) {
                Thread.sleep(5 * 1000);

                Filler fillerB = new Filler();
                Map<String, FileInfo> next = fillerB.getDatabase(URL);

                Collection<Difference> differences = Filler.getDifferences(prev, next);

                prev = next;

                showDifferences(differences);
            }

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(MyIntegrit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
