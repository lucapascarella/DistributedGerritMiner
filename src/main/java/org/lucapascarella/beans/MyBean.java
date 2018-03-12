package org.lucapascarella.beans;

import java.sql.Timestamp;

public class MyBean {
    
    public String formatString(String toFormat) {
        if (toFormat != null)
            return toFormat.replaceAll("\\'", "").replaceAll("\"", "").replaceAll("%", "").replace("\\", "");
        return "";
    }

    public String formatString(Timestamp toFormat) {
        if (toFormat != null)
            return toFormat.toString().replaceAll("\\'", "").replaceAll("\"", "").replaceAll("%", "").replace("\\", "");
        return "";
    }
    
    public String formatString(Integer toFormat) {
        if (toFormat != null)
            return toFormat.toString().replaceAll("\\'", "").replaceAll("\"", "").replaceAll("%", "").replace("\\", "");
        return "0";
    }
    
    public String formatString(Long toFormat) {
        if (toFormat != null)
            return toFormat.toString().replaceAll("\\'", "").replaceAll("\"", "").replaceAll("%", "").replace("\\", "");
        return "0";
    }
}
