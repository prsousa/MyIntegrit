/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myintegrit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Paulo
 */
public class Filler {

    private final Map<String, FileInfo> info;

    public Filler() {
        this.info = new HashMap<>();
    }

    public void clear() {
        info.clear();
    }

    private void run(String initialPath) throws IOException {
        File file = new File(initialPath);

        for (File f : file.listFiles()) {
            String absolutePath = f.getAbsolutePath();

            if (f.isDirectory()) {
                this.run(absolutePath);
            }

            try {
                FileInfo fileInfo = new FileInfo(f);
                info.put(absolutePath, fileInfo);
            } catch(IOException ex) {}
        }
    }

    public Map<String, FileInfo> getDatabase(String initialPath) throws IOException {
        this.run(initialPath);
        return info;
    }

    public long getNumFiles() {
        return this.info.size();
    }

    public static Collection<Difference> getDifferences(Map<String, FileInfo> prev, Map<String, FileInfo> next) {
        List<Difference> res = new ArrayList<>();
        Set<String> filesPrev = prev.keySet();

        for (String nextFile : next.keySet()) {
            if (!filesPrev.contains(nextFile)) {
                // Ficheiro Criado
                res.add( new AlertDiff(nextFile, "File Created") );
            } else {
                FileInfo prevInfo = prev.get(nextFile);
                FileInfo nextInfo = next.get(nextFile);

                Collection<Difference> differences = prevInfo.getDifferences(nextInfo);
                
                res.addAll(differences);
                
                filesPrev.remove(nextFile);
            }
        }

        for (String prevFile : filesPrev) {
            res.add( new CriticalDiff(prevFile, "File Excluded") );
        }

        return res;
    }
}
