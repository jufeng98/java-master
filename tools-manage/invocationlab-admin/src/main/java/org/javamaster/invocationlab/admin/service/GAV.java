package org.javamaster.invocationlab.admin.service;

import lombok.Data;

/**
 * @author yudong
 */
@Data
public class GAV {
    String groupID;
    String artifactID;
    String version;

    @Override
    public String toString() {
        return this.getGroupID() + ":" + this.getArtifactID() + ":" + this.getVersion();
    }
}
