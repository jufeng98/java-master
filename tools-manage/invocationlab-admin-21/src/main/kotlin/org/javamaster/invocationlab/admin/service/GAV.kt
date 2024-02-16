package org.javamaster.invocationlab.admin.service

/**
 * @author yudong
 */

class GAV {
    var groupID: String? = null
    var artifactID: String? = null
    var version: String? = null

    override fun toString(): String {
        return this.groupID + ":" + this.artifactID + ":" + this.version
    }
}
