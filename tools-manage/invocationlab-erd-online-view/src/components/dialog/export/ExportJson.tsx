import React from 'react';
import {Alignment, Button} from "@blueprintjs/core";
import {MyIcon} from "@/components/Menu";
import useProjectStore from "@/store/project/useProjectStore";
import shallow from "zustand/shallow";


export type ExportJsonProps = {};

const ExportJson: React.FC<ExportJsonProps> = (props) => {
  const {projectDispatch} = useProjectStore(state => ({
    projectDispatch: state.dispatch,
  }), shallow);
  return (<>
    <Button
      key="JSON"
      icon={<MyIcon type="icon-JSON"/>}
      text="导出ERD"
      minimal={true}
      small={true}
      fill={true}
      onClick={()=>projectDispatch.exportFile('JSON')}
      alignText={Alignment.LEFT}
    ></Button>
  </>);
}

export default React.memo(ExportJson)
