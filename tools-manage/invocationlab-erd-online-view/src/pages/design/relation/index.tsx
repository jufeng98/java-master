import G6Relation from "@/pages/design/relation/g6";
import useProjectStore from "@/store/project/useProjectStore";
import shallow from "zustand/shallow";
import React, { useEffect, useRef, useState } from "react";
import { ModuleEntity } from "@/store/tab/useTabStore";
import './index.less';

export type RelationProps = {
  moduleEntity: ModuleEntity
};
const Relation: React.FC<RelationProps> = (props) => {
  const { projectJSON, projectDispatch } = useProjectStore(state => ({
    projectJSON: state.project?.projectJSON,
    projectDispatch: state.dispatch,
  }), shallow);

  const columnOrder = [
    { code: 'chnname', value: '字段名', com: 'Input', relationNoShow: false },
    { code: 'name', value: '逻辑名(英文名)', com: 'Input', relationNoShow: false },
    { code: 'type', value: '类型', com: 'Select', relationNoShow: false },
    { code: 'dataType', value: '数据库类型', com: 'Text', relationNoShow: true },
    { code: 'remark', value: '说明', com: 'Input', relationNoShow: true },
    { code: 'pk', value: '主键', com: 'Checkbox', relationNoShow: false },
    { code: 'notNull', value: '非空', com: 'Checkbox', relationNoShow: true },
    { code: 'autoIncrement', value: '自增', com: 'Checkbox', relationNoShow: true },
    { code: 'defaultValue', value: '默认值', com: 'Input', relationNoShow: true },
    { code: 'relationNoShow', value: '关系图', com: 'Icon', relationNoShow: true },
    { code: 'uiHint', value: 'UI建议', com: 'Select', relationNoShow: true },
  ];

  const getWindowWidth = () => {
    return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth
  }
  const getWindowHeight = () => {
    return window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight
  }

  const normalHeight = getWindowHeight() - 105
  const [height, setHeight] = useState<number>(normalHeight);

  let fullFlag = false
  const toggleFullscreen = () => {
    // console.log("toggleRelationSize", fullFlag);
    try {
      if (fullFlag) {
        document.exitFullscreen();
      } else {
        divRef?.current?.requestFullscreen();
      }
    } catch (e) {
      console.warn(e)
    }
  }

  useEffect(() => {
    relationRef?.current?.autoSave()
  }, [])

  const keydown = (e: KeyboardEvent) => {
    // console.log("keydown", e);
    if (e.code !== 'KeyN') {
      return
    }
    if (!document.activeElement || !document.activeElement.id) {
      return
    }
    if (!document.activeElement.id.includes("canvas")) {
      return
    }
    toggleFullscreen()
    e.stopPropagation();
    e.preventDefault()
  }

  const toggleRelationSize = () => {
    if (document.fullscreenElement) {
      setHeight(getWindowHeight() - 10)
    } else {
      setHeight(normalHeight - 10)
    }
    fullFlag = !fullFlag
    relationRef?.current?.autoSave()
  }

  useEffect(() => {
    window.addEventListener('keydown', keydown);
    document.addEventListener("fullscreenchange", toggleRelationSize)
    return () => {
      window.removeEventListener('keydown', keydown);
      window.removeEventListener('fullscreenchange', toggleRelationSize);
    }
  }, [])

  let divRef = useRef<any>(null)
  let relationRef = useRef<any>(null)

  return (
    <div className={'fullscreen fullscreen-enabled'} ref={divRef}>
      <G6Relation ref={relationRef} dataSource={JSON.parse(JSON.stringify(projectJSON))}
        columnOrder={columnOrder}
        value={`map&${props.moduleEntity.module}`}
        height={height}
        width={getWindowWidth()}
        projectDispatch={projectDispatch}
      />
    </div>
  );
}
export default React.memo(Relation)
