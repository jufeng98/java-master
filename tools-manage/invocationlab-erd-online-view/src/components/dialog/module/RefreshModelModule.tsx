import React from 'react';
import useProjectStore from "@/store/project/useProjectStore";
import shallow from "zustand/shallow";
import {Button, Popconfirm} from "antd";
import {EditOutlined} from "@ant-design/icons";

export type RefreshModuleProps = {
  disable: boolean;
  moduleInfo: any;
};

const RefreshModule: React.FC<RefreshModuleProps> = (props) => {
  const {projectDispatch} = useProjectStore(state => ({
    projectDispatch: state.dispatch,
  }), shallow);

  return (<>
    <Popconfirm placement="right" title="此操作将会重新从db读取表元数据构建当前模块模型"
                onConfirm={() => projectDispatch.refreshModule(props.moduleInfo).then(res=>{
                  projectDispatch.updateModule(res.data)
                })} okText="我已知悉,确定"
                cancelText="取消">
      <Button icon={<EditOutlined />}
              type="text"
              size={"small"}
              disabled={props.disable}
      >刷新模块模型</Button>
    </Popconfirm>
  </>);
}

export default React.memo(RefreshModule)
