import React, { useEffect } from "react";
import { FloatButton, Modal } from 'antd';
import "./index.scss";
import TableTab from "@/pages/design/table/component/tab/TableTab";
import useTabStore, { ModuleEntity, TabGroup } from "@/store/tab/useTabStore";
import useProjectStore from "@/store/project/useProjectStore";
import Relation from "@/pages/design/relation";
import { Dropdown, Empty, Menu, Tabs, TabsProps } from "antd";
import Query from "@/pages/design/query";
import { EllipsisMiddle } from "@/components/EllipsisMiddle";

export type TableProps = {};
const Table: React.FC<TableProps> = (props) => {
  const tableTabs = useTabStore(state => state.tableTabs);
  const selectTabId = useTabStore(state => state.selectTabId);
  const tabDispatch = useTabStore(state => state.dispatch);
  // @ts-ignore
  const { showErrorTipFloatButton } = useProjectStore()

  // console.log('tableTabs', tableTabs)
  // console.log('selectTabId', selectTabId)

  useEffect(() => {
    if (showErrorTipFloatButton) {
      Modal.confirm({
        title: '警告！！！',
        content: '服务器数据已发生变化，为避免互相覆盖导致数据丢失，所有保存都将失败。请刷新页面后再重试！！！',
        okText: '刷新页面(推荐)',
        cancelText: '关闭弹窗',
        onOk: () => {
          window.location.reload()
        },
        onCancel: (close) => { close() }
      });
    }
  }, [showErrorTipFloatButton])

  const getTab = (tab: ModuleEntity) => {
    if (tab.group === TabGroup.MODEL) {
      if (tab.entity?.startsWith('关系图')) {
        return <Relation moduleEntity={tab} />
      } else {
        return <TableTab moduleEntity={tab} />;
      }
    }

    if (tab.group === TabGroup.QUERY) {
      return <Query id={tab.module + ''} />;
    }

    return <Empty
      image="/invocationlab-erd-online-view/empty.svg"
      imageStyle={{
        height: 200,
      }}
      style={{
        marginTop: '100px'
      }}
      description={
        <span>这里空空如也!</span>
      } />
  }

  const closeCurrent = (tab: ModuleEntity) => {
    // console.log('currentEntity19', tab)
    tabDispatch.removeTab(tab);
  }

  const closeLeft = (tab: ModuleEntity) => {
    // console.log('currentEntity 29', tab)
    tabDispatch.removeLeftTab(tab);
  }

  const closeRight = (tab: ModuleEntity) => {
    // console.log('currentEntity 34', tab)
    tabDispatch.removeRightTab(tab);
  }

  const closeAll = (tab: ModuleEntity) => {
    // console.log('currentEntity 37', tab)
    tabDispatch.removeAllTab(tab);
  }


  // useEffect(() => {
  // console.log('re-rending11')
  // })
  const { TabPane } = Tabs;

  const getModuleEntity = (key: string) => {
    return { group: TabGroup.MODEL, module: key.split('###')[0], entity: key.split('###')[1] };
  }

  const onChange = (targetKey: string) => {
    tabDispatch.activeTab(getModuleEntity(targetKey));
  };

  const onEdit = (targetKey: any, action: 'add' | 'remove') => {
    // console.log(targetKey)
    if (action === 'remove') {
      closeCurrent(getModuleEntity(targetKey));
    } else {
    }
  };


  const renderRightContent = (tab: ModuleEntity) => {
    return (
      <Menu>
        <Menu.Item key={"closeCurrent"} onClick={() => closeCurrent(tab)}>关闭当前</Menu.Item>
        <Menu.Item key={"closeLeft"} onClick={() => closeLeft(tab)}>关闭左边</Menu.Item>
        <Menu.Item key={"closeRight"} onClick={() => closeRight(tab)}>关闭右边</Menu.Item>
        <Menu.Item key={"closeAll"} onClick={() => closeAll(tab)}>关闭全部</Menu.Item>
      </Menu>
    );
  };


  const renderTabBar: TabsProps['renderTabBar'] = (tabBarProps, DefaultTabBar) => (
    // @ts-ignore
    <DefaultTabBar {...tabBarProps}>
      {node => (
        // @ts-ignore
        <Dropdown overlay={renderRightContent({ module: node?.key?.split('###')[0], entity: node?.key?.split('###')[1] })}
          trigger={['contextMenu']}>
          {node}
        </Dropdown>
      )}
    </DefaultTabBar>
  );

  return (
    <>
      {selectTabId ?
        <Tabs type="editable-card" hideAdd onEdit={(e, action) => onEdit(e, action)} activeKey={selectTabId}
          key={selectTabId}
          onChange={onChange}
          renderTabBar={renderTabBar}
        >
          {tableTabs?.map((tab: ModuleEntity, index: number) => {
            return <TabPane
              tab={
                <EllipsisMiddle title={tab.entity}>
                  {tab.entity}
                </EllipsisMiddle>
              }
              key={`${tab.module}###${tab.entity}`}
              closable={true}
            >
              {getTab(tab)}
            </TabPane>
          }
          )}
        </Tabs>
        : <Empty
          image="/invocationlab-erd-online-view/empty.svg"
          imageStyle={{
            height: 200,
          }}
          style={{
            marginTop: '100px'
          }}
          description={
            <span>这里空空如也!</span>
          } />
      }
      {
        showErrorTipFloatButton && <FloatButton description="服务器数据已发生变化，为避免互相覆盖导致数据丢失，所有保存都将失败。请刷新页面后再重试！"
          shape="square" style={{ right: 230, top: 20, width: 340, height: 30, backgroundColor: "#00000000", zIndex: 9999, transform: "scale(1.2)" }} />
      }
    </>
  );
}

export default React.memo(Table)
