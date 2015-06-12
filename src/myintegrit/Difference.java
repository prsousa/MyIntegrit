/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package myintegrit;

import java.util.Objects;

/**
 *
 * @author Paulo
 */
public abstract class Difference {
    private final String document;
    private final String description;

    public Difference(String document, String description) {
        this.document = document;
        this.description = description;
    }
    
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Document Changed: ").append(document).append(" - ");
        sb.append(description);
        
        return sb.toString();
    }
    
    
    
    
    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.document);
        hash = 47 * hash + Objects.hashCode(this.description);
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
        final Difference other = (Difference) obj;
        if (!Objects.equals(this.document, other.document)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return true;
    }
    
}
