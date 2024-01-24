import React, { useState } from 'react';
import { ModalForm } from '@ant-design/pro-components';
import useProjectStore from "@/store/project/useProjectStore";
import { Button, message } from "antd";
import { EditOutlined } from "@ant-design/icons";
import type { DragEndEvent } from '@dnd-kit/core';
import { DndContext, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import { restrictToVerticalAxis } from '@dnd-kit/modifiers';
import {
  SortableContext,
  arrayMove,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import * as cache from "@/utils/cache";
import { CONSTANT } from "@/utils/constant";
import request from "../../../utils/request";

export type SortModuleProps = {
  disable: boolean
};

const AddModule: React.FC<SortModuleProps> = (props) => {
  const projectDispatch = useProjectStore(state => state.dispatch);

  interface DataType {
    name: string;
    chnname: string;
  }
  const columns: ColumnsType<DataType> = [
    {
      title: '模块英文名',
      dataIndex: 'name',
    },
    {
      title: '模块中文名',
      dataIndex: 'chnname',
    }
  ]
  interface RowProps extends React.HTMLAttributes<HTMLTableRowElement> {
    'data-row-key': string;
  }
  const Row = (props: RowProps) => {
    const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
      id: props['data-row-key'],
    });
    const style: React.CSSProperties = {
      ...props.style,
      transform: CSS.Transform.toString(transform && { ...transform, scaleY: 1 }),
      transition,
      cursor: 'move',
      ...(isDragging ? { position: 'relative', zIndex: 9999 } : {}),
    };
    return <tr {...props} ref={setNodeRef} style={style} {...attributes} {...listeners} />;
  };
  const modules = projectDispatch.getModuleEntityTree('')
  const [dataSource, setDataSource] = useState(modules.map((module: any) => {
    return {
      name: module.name,
      chnname: module.chnname
    }
  }));
  const [loading, setLoading] = useState(false);
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 1,
      },
    }),
  );
  const onDragEnd = ({ active, over }: DragEndEvent) => {
    if (active.id !== over?.id) {
      setDataSource((prev: any[]) => {
        const activeIndex = prev.findIndex((i) => i.name === active.id);
        const overIndex = prev.findIndex((i) => i.name === over?.id);
        return arrayMove(prev, activeIndex, overIndex);
      });
    }
  };

  return (<>
    <ModalForm
      title="拖动调整顺序"
      loading={loading}
      trigger={
        <Button icon={<EditOutlined />}
          type="text"
          size={"small"}
          disabled={props.disable}>模块排序</Button>
      }
      onFinish={async (_: any) => {
        setLoading(true)
        request.post('/ncnb/project/sortModule', { data: { projectId: cache.getItem(CONSTANT.PROJECT_ID), sortModuleVos: dataSource } })
          .then(res => {
            setLoading(false)
            if (!res || !res.data) {
              return;
            }
            message.success(res.data)
            setTimeout(() => location.reload(), 2000)
          })
      }}
    >
      <DndContext sensors={sensors} modifiers={[restrictToVerticalAxis]} onDragEnd={onDragEnd}>
        <SortableContext
          // rowKey array
          items={dataSource.map((i: { name: any; }) => i.name)}
          strategy={verticalListSortingStrategy}
        >
          <Table
            components={{
              body: {
                row: Row,
              },
            }}
            rowKey="name"
            scroll={{ y: 'calc(100vh - 300px)' }}
            columns={columns}
            dataSource={dataSource}
            pagination={false}
          />
        </SortableContext>
      </DndContext>
    </ModalForm>
  </>);
}

export default React.memo(AddModule)
