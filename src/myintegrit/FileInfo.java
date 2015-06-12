/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myintegrit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Paulo
 */
public class FileInfo {

    public enum Timefield {

        CREATED, ACCESSED, WRITTEN
    }

    private final File file;
    private final String filename;
    private final String owner;
    private final String group;
    private final Set<PosixFilePermission> mod;
    private final long modificationTime;
    private final long creationTime;
    private final long size;

    public FileInfo(File file) throws IOException {

        Path filePath = file.toPath();
        PosixFileAttributes attr = Files.readAttributes(filePath, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

        this.file = file;
        this.filename = file.getName();
        this.creationTime = attr.creationTime().toMillis();
        this.modificationTime = attr.lastModifiedTime().toMillis();
        this.size = attr.size();
        this.owner = attr.owner().getName();
        this.group = attr.group().getName();
        this.mod = Files.getPosixFilePermissions(filePath);

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.filename);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileInfo other = (FileInfo) obj;
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        if (!Objects.equals(this.owner, other.owner)) {
            return false;
        }
        if (!Objects.equals(this.group, other.group)) {
            return false;
        }
        if (!Objects.equals(this.mod, other.mod)) {
            return false;
        }
        if (this.modificationTime != other.modificationTime) {
            return false;
        }
        if (this.creationTime != other.creationTime) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        return true;
    }

    public Collection<Difference> getDifferences(FileInfo file) {
        Collection<Difference> res = new ArrayList<>();

        if (!owner.equals(file.owner)) {
            res.add(new AlertDiff(this.filename, "Owner Chanded"));
        }

        if (!group.equals(file.group)) {
            res.add(new AlertDiff(this.filename, "Group Chanded"));
        }

        if (this.creationTime != file.creationTime) {
            res.add(new AlertDiff(this.filename, "Creation Time Changed"));
        }

        if (this.modificationTime != file.modificationTime) {
            res.add(new AlertDiff(this.filename, "Modification Time Changed"));
        }

        if (this.size != file.size) {
            res.add(new AlertDiff(this.filename, "File Size Changed"));
        }
        
        if( !this.mod.equals(file.mod) ) {
            res.add(new CriticalDiff(this.filename, "Permission Changed"));
        }
        
        return res;
    }
}
