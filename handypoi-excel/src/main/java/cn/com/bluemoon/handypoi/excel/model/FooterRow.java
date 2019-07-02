package cn.com.bluemoon.handypoi.excel.model;

import java.util.List;

public class FooterRow {

    private List<FooterColumn> footerColumns;

    public FooterRow(List<FooterColumn> footerColumns) {
        this.footerColumns = footerColumns;
    }

    public List<FooterColumn> getFooterColumns() {
        return footerColumns;
    }

    public void setFooterColumns(List<FooterColumn> footerColumns) {
        this.footerColumns = footerColumns;
    }
}
