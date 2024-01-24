import create from "zustand";
import { DataNode } from "antd/lib/tree";
import { ADD, DEL, EDIT, GET, POST, TREE } from "@/services/crud";
import produce from "immer";
import * as cache from "@/utils/cache";
import { CONSTANT } from "@/utils/constant";
import _ from "lodash";
import useTabStore, { TabGroup } from "@/store/tab/useTabStore";
import { message } from "antd";
import { exportSql } from "@/utils/request";

// 类型：对象、函数两者都适用，但是 type 可以用于基础类型、联合类型、元祖。
// 同名合并：interface 支持，type 不支持。
// 计算属性：type 支持, interface 不支持。
// 总的来说，公共的用 interface 实现，不能用 interface 实现的再用 type 实现。主要是一个项目最好保持一致。

type actions = {
  queryHistory(params: any): Promise<COMMON.R>;
  explain(params: any): Promise<COMMON.R>;
  exec(selectValue: any): Promise<COMMON.R>;
  getTableRecordTotal(selectValue: any): Promise<COMMON.R>;
  exportSql(selectValue: any, setLoading: Function): void;
  updateSqlInfo(model: any): void;
  renameQuery(model: any): void;
  removeQuery(model: any): void;
  addQuery(model: any): void;
  fetchQueryInfo: (id: string | number) => Promise<COMMON.R>;
  onSelectNode(selectedKeys: import("rc-tree/lib/interface").Key[], info: { event: "select"; selected: boolean; node: import("rc-tree/lib/interface").EventDataNode<DataNode>; selectedNodes: DataNode[]; nativeEvent: MouseEvent; }): void;
  fetchTreeData: (params: any) => Promise<any>,
  setQuerySearchKey: (searchKey: string) => void

}

export type QueryState = {
  querySearchKey: string;
  treeData: DataNode[];
  dispatch: actions
};


const useQueryStore = create<QueryState>(
  (set, get) => ({
    querySearchKey: '',
    treeData: [],
    dispatch: {
      queryHistory: (model) => {
        return GET('/ncnb/queryHistory', model);
      },
      explain: (model) => {
        return POST('/ncnb/queryInfo/explain', model);
      },
      exec: (model) => {
        return POST('/ncnb/queryInfo/exec', model);
      },
      getTableRecordTotal: (model) => {
        return POST('/ncnb/queryInfo/getTableRecordTotal', model);
      },
      exportSql: (model, setLoading) => {
        exportSql(model, setLoading, "/ncnb/queryInfo/exportSql")
      },
      updateSqlInfo: (model) => {
        EDIT('/ncnb/queryInfo/' + model.id, model).then(r => {
          if (r?.code === 200) {
            message.success(r?.msg);
            return
          }
          console.error('41', r);
        });
      },
      renameQuery: (model) => {
        EDIT('/ncnb/queryInfo/' + model.id, model).then(r => {
          if (r?.code === 200) {
            message.success('修改成功');
            get().dispatch.fetchTreeData({
              projectId: model.projectId
            });
          }
        });
      },
      removeQuery: (model) => {
        let projectId = cache.getItem(CONSTANT.PROJECT_ID) || '';
        DEL('/ncnb/queryInfo/' + projectId + '/' + model.id, {}).then(r => {
          if (r?.code === 200) {
            message.success('删除成功');
            get().dispatch.fetchTreeData({
              projectId: projectId
            });
          }
        });
      },
      addQuery: (model) => {
        ADD('/ncnb/queryInfo', model).then(r => {
          if (r?.code === 200) {
            message.success('新增成功');
            get().dispatch.fetchTreeData({
              projectId: model.projectId
            });
          }
        });
      },
      fetchQueryInfo: (id) => {
        return GET('/ncnb/queryInfo/' + id, {});
      },
      fetchTreeData: (params) => {
        const title = get().querySearchKey;
        if (title) {
          _.set(params, 'title', title);
        }
        return new Promise((resolve, reject) => {
          TREE('/ncnb/queryInfo/tree', params).then(r => {
            if (r?.code === 200) {
              set({
                treeData: r?.data || []
              })
              resolve(r?.data || [])
            } else {
              reject(r?.code)
            }
          });
        })
      },
      setQuerySearchKey: (querySearchKey: string) => set(produce(state => {
        state.querySearchKey = querySearchKey;
      })),
      onSelectNode: (selectedKeys, info) => set(produce(state => {
        // console.log(49, selectedKeys, info);
        const tabDispatch = useTabStore.getState().dispatch;
        if (info.node.isLeaf) {
          tabDispatch.addTab({ group: TabGroup.QUERY, module: info.node.key + '', entity: info.node.title + '' });
        }
      })),
    }

  })
);

export default useQueryStore;
