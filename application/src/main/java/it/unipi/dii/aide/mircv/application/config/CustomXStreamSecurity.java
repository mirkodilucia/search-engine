package it.unipi.dii.aide.mircv.application.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.TypePermission;

public class CustomXStreamSecurity implements TypePermission {

    public static void configureXStreamSecurity(XStream xstream) {
        xstream.addPermission(new CustomXStreamSecurity());
        XStream.setupDefaultSecurity(xstream);
    }

    @Override
    public boolean allows(Class type) {
        return type.equals(Config.class);

    }
}