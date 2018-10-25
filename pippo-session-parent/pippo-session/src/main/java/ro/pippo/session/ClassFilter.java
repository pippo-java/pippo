package ro.pippo.session;

import java.util.ArrayList;

/**
 * @author idealzh
 */
public class ClassFilter {
    private ArrayList<String> WhiteList= null;
    public ClassFilter() {
        WhiteList=new ArrayList<String>();
        WhiteList.add("ro.pippo.session.SessionData");
        WhiteList.add("java.util.HashMap");
        WhiteList.add("ro.pippo.core.Flash");
        WhiteList.add("java.util.ArrayList");
    }

    public boolean isWhiteListed(String className) {
        if (className==null) return false;
        for(String name:WhiteList) {
            if(name.equals(className)) return true;
        } return false;
    }
}