import React from "react";
import {AnchorButton, Button, ButtonGroup, Icon, Menu, MenuDivider, MenuItem, Props} from "@blueprintjs/core";
import {createFromIconfontCN} from "@ant-design/icons";
import {history} from 'umi';
import AddVersion from "@/components/dialog/version/AddVersion";
import SyncConfig from "@/components/dialog/version/SyncConfig";
import InitVersion from "@/components/dialog/version/InitVersion";
import RebuildVersion from "@/components/dialog/version/RebuildVersion";
import ReverseDatabase from "../dialog/import/ReverseDatabase";
import ReversePdMan from "@/components/dialog/import/ReversePdMan";
import ExportHTML from "@/components/dialog/export/ExportHTML";
import ExportDDL from "@/components/dialog/export/ExportDDL";
import ExportJson from "@/components/dialog/export/ExportJson";
import DatabaseSetUp from "@/components/dialog/setup/DatabaseSetUp";
import {Popover2} from "@blueprintjs/popover2";
import {IconName} from "@blueprintjs/icons";
import {MaybeElement} from "@blueprintjs/core/src/common/props";
import DefaultSetUp from "@/components/dialog/setup/DefaultSetUp";
import useShortcutStore, {PANEL} from "@/store/shortcut/useShortcutStore";
import CompareVersion, {CompareVersionType} from "@/components/dialog/version/CompareVersion";
import RenameVersion from "@/components/dialog/version/RenameVersion";
import RemoveVersion from "@/components/dialog/version/RemoveVersion";
import SyncVersion from "@/components/dialog/version/SyncVersion";
import ExportWord from "@/components/dialog/export/ExportWord";
import ExportMarkdown from "@/components/dialog/export/ExportMarkdown";
import ReverseERD from "@/components/dialog/import/ReverseERD";


export const MyIcon = createFromIconfontCN({
  scriptUrl: '//at.alicdn.com/t/font_1485538_uljgplzg6rm.js', // 在 iconfont.cn 上生成
});


export interface IFileMenuProps extends Props {
  shouldDismissPopover?: boolean;
}


export const VersionMenu: React.FunctionComponent<IFileMenuProps> = props => (
  <>
    <AddVersion trigger="bp"/>
    <SyncConfig/>
    <InitVersion/>
    <RebuildVersion/>
  </>
);


export const ImportMenu: React.FunctionComponent<IFileMenuProps> = props => (
  <Menu className={props.className}>
    <ReverseDatabase/>
    {/*<ReversePDM/>*/}
    {/*<ReverseERWin/>*/}
    <ReversePdMan/>
    <ReverseERD/>
  </Menu>
);

export const ExportMenu: React.FunctionComponent<IFileMenuProps> = props => (
  <Menu className={props.className}>
    <ExportHTML/>
    <ExportWord/>
    <ExportMarkdown/>
    <ExportDDL/>
    <ExportJson/>
  </Menu>
);

export const SetUpMenu: React.FunctionComponent<IFileMenuProps> = props => (
  <Menu className={props.className}>
    <DatabaseSetUp/>
    <DefaultSetUp/>
  </Menu>
);

export const HelpMenu: React.FunctionComponent<IFileMenuProps> = props => (
/*  <Menu className={props.className}>
    <MenuItem key="video" text="教程" icon="video" {...props} ></MenuItem>
    {/!*<MenuItem key="default" text="快捷键" icon="key-command" {...props} />*!/}
  </Menu>*/
  <AnchorButton
    href="https://portal.zerocode.net.cn/docs/getting-started"
    icon="share"
    target="_blank"
    minimal={true}
    text={"教程"}
  />
);


const shortcutState = useShortcutStore.getState();

const setShortcut = (shortcut: string) => {
  shortcutState.dispatch.setPanel(shortcut)
}

const renderButton = (icon: IconName | MaybeElement, text: string, content: string | JSX.Element, shortcut?: string) => {
  return (
    <Popover2
      autoFocus={false}
      enforceFocus={false}
      hasBackdrop={true}
      content={content}
      position="right"
    >
      <Button rightIcon="caret-right" icon={icon} text={text} onClick={() => {
        if (shortcut) {
          setShortcut(shortcut)
        }
      }}/>
    </Popover2>
  );
}

export const ProjectMenu: React.FunctionComponent<IFileMenuProps> = props => {
  return (
    false ? <Menu>
        <MenuItem key="history" shouldDismissPopover={false} text="版本" icon="history"
                  onMouseOver={() => setShortcut(PANEL.VERSION)}><VersionMenu/></MenuItem>
        <MenuItem key="import" shouldDismissPopover={false} text="导入" icon="import"
                  onMouseOver={() => setShortcut(PANEL.DEFAULT)}><ImportMenu/></MenuItem>
        <MenuItem key="export" shouldDismissPopover={false} text="导出" icon="export"><ExportMenu/></MenuItem>
        <MenuItem key="cog" shouldDismissPopover={false} text="设置" icon="cog"><SetUpMenu/></MenuItem>
      </Menu>
      : <ButtonGroup vertical={true}>
        {renderButton("history", "版本", <VersionMenu/>, PANEL.VERSION)}
        {renderButton("import", "导入", <ImportMenu/>, PANEL.DEFAULT)}
        {renderButton("export", "导出", <ExportMenu/>)}
        {renderButton("cog", "设置", <SetUpMenu/>)}
      </ButtonGroup>
  );
};


export const ProjectSortMenu: React.FunctionComponent<IFileMenuProps> = props => {
  return (
    <Menu>
      <MenuItem text="创建时间" icon="time"/>
      <MenuItem text="最近修改" icon="updated"/>
    </Menu>
  );
};

export const ProjectFilterMenu: React.FunctionComponent<IFileMenuProps> = props => {
  return (
    <Menu>
      <MenuItem key="history" shouldDismissPopover={false} text="过滤1" icon="history"
                onMouseOver={() => setShortcut(PANEL.VERSION)}></MenuItem>
      <MenuItem key="import" shouldDismissPopover={false} text="过滤2" icon="import"
                onMouseOver={() => setShortcut(PANEL.DEFAULT)}></MenuItem>
    </Menu>
  );
};

export const NavigationMenu: React.FunctionComponent<IFileMenuProps> = props => {
  const {className} = props;
  return (
    <Menu className={className}>
      <MenuItem text="返回工作台" icon="chevron-left" onClick={() => {
        history.push('/project/recent')
      }}></MenuItem>
      <MenuDivider/>
      <MenuItem text="资源社区" icon="globe-network" labelElement={<Icon icon="share"/>}></MenuItem>
      <MenuDivider/>
      <MenuItem text="帮助" icon="help"><HelpMenu className={className}/></MenuItem>
     {/* <MenuItem text="账号设置" icon="user"></MenuItem>*/}
      <MenuItem text="退出登录" icon="log-out"></MenuItem>
    </Menu>
  );
};

export const VersionHandle: React.FunctionComponent<IFileMenuProps> = props => {
  return (
    true ? <Menu>
        <CompareVersion type={CompareVersionType.DETAIL}/>
        <CompareVersion type={CompareVersionType.COMPARE}/>
        <RenameVersion/>
        <RemoveVersion/>
        <SyncVersion/>
      </Menu>
      : <ButtonGroup vertical={true}>
        {renderButton("history", "版本", <VersionMenu/>, PANEL.VERSION)}
        {renderButton("import", "导入", <ImportMenu/>, PANEL.DEFAULT)}
        {renderButton("export", "导出", <ExportMenu/>)}
        {renderButton("cog", "设置", <SetUpMenu/>)}
      </ButtonGroup>
  );
}

