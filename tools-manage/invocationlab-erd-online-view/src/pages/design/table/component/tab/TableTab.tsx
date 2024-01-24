import React from 'react';
import TableInfoEdit from "@/pages/design/table/component/table/TableInfoEdit";
import CodeTab from "@/pages/design/table/component/tab/CodeTab";
import { ModuleEntity } from "@/store/tab/useTabStore";
import { copyValue } from '@/pages/design/query/component/QueryResultUtils';
import TableIndexEdit from "@/pages/design/table/component/table/TableIndexEdit";

import { Tabs, Tag } from "antd";

const { TabPane } = Tabs;
export type TableTabProps = {
  moduleEntity: ModuleEntity
};

const TableTab: React.FC<TableTabProps> = (props) => {
  // console.log('TableTab13', TableTab)
  return (
    <>
      <Tabs
        id="tableNav"
        defaultActiveKey="field"
        size={'small'}
      >
        <TabPane key="field" tab="字段">
          <Tag bordered={false} onDoubleClick={() => copyValue(props.moduleEntity?.entity || '')} style={{ margin: "3px", fontSize: "16px" }}>
            {props.moduleEntity?.entity}
          </Tag>
          <TableInfoEdit moduleEntity={props.moduleEntity} />
        </TabPane>
        <TabPane key="index" tab="索引"><TableIndexEdit moduleEntity={props.moduleEntity} /></TabPane>
        <TabPane key="code" tab="元数据应用"><CodeTab moduleEntity={props.moduleEntity} /></TabPane>
      </Tabs>
    </>
  );
}

export default React.memo(TableTab)
