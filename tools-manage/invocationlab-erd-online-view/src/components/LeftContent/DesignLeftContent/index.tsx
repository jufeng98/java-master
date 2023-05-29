import React from 'react';

import "./index.less";
import DataTable from "@/components/LeftContent/DesignLeftContent/component/DataTable";
import DataDomain from "@/components/LeftContent/DesignLeftContent/component/DataDomain";
import { Input, Tabs } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import useGlobalStore from "@/store/global/globalStore";
import shallow from "zustand/shallow";

const { TabPane } = Tabs;


export type DesignLeftContentProps = {
  collapsed: boolean | undefined;
};

const DesignLeftContent: React.FC<DesignLeftContentProps> = (props) => {
  const { globalDispatch } = useGlobalStore(state => ({
    globalDispatch: state.dispatch
  }), shallow);

  return (
    props.collapsed ? <></> :
      <>
        <Input
          style={{
            borderRadius: 4,
            marginInlineEnd: 12,
          }}
          allowClear
          size={"small"}
          prefix={<SearchOutlined />}
          placeholder="搜索元数据"
          onPressEnter={(e) => {
            // @ts-ignore
            globalDispatch.setSearchKey(e.target?.value)
          }}
        />
        <Tabs defaultActiveKey="1" centered={true}
          items={[
            { label: '元数据', key: '1', children: <DataTable /> },
            { label: '数据域', key: '2', children: <DataDomain /> },
          ]}>
        </Tabs>
      </>
  )
};

export default React.memo(DesignLeftContent)
