package org.javamaster.invocationlab.admin.service.creation;

import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.Pair;

/**
 * @author yudong
 */
public interface Creator {

    Pair<Boolean, String> create(String cluster, GAV gav, String serviceName);

    Pair<Boolean, String> refresh(String cluster, String serviceName);

    String getLatestVersion(GAV gav);
}
