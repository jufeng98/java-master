package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/16
 */


@AllOpen
class Tree {
    var id: String? = null
    var key: String? = null
    var title: String? = null
    var label: String? = null
    var value: String? = null
    var isLeaf: Boolean? = null
    var parentId: String? = null
    var parentKey: Any? = null
    var sqlInfo: String? = null
    var selectDB: String? = null
    var parentKeys: List<String>? = null
    var children: MutableList<Tree>? = null
}
