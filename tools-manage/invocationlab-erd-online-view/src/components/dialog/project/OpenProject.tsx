import React from 'react';
import {Button} from "antd";
import * as cache from "@/utils/cache";
import {history} from "@@/core/history";

export type OpenProjectProps = {
  project: any;
};

const OpenProject: React.FC<OpenProjectProps> = (props) => {


  return (<>
    <Button type="primary" ghost onClick={() => {
      cache.setItem("erd:projectId", props.project.id);
      history.push({
        pathname: '/design/table/model'
      });
    }}>
      打开模型
    </Button>
  </>);
}

export default React.memo(OpenProject)
