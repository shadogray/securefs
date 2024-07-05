package at.tfr.securefs.cache;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class StateInfo {
    @ProtoField(number = 1)
    final String host;
    @ProtoField(number = 2)
    final String info;

    @ProtoFactory
    public StateInfo(String host, String info) {
        this.host = host;
        this.info = info;
    }

    public String getHost() {
        return host;
    }
    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return host + ":" + info;
    }
}
