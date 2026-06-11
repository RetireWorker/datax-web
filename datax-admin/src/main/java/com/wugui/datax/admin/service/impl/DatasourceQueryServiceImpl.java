package com.wugui.datax.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.wugui.datax.admin.entity.JobDatasource;
import com.wugui.datax.admin.service.DatasourceQueryService;
import com.wugui.datax.admin.service.JobDatasourceService;
import com.wugui.datax.admin.tool.database.ColumnInfo;
import com.wugui.datax.admin.tool.query.*;
import com.wugui.datax.admin.util.JdbcConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * datasource query
 *
 * @author zhouhongfa@gz-yibo.com
 * @ClassName JdbcDatasourceQueryServiceImpl
 * @Version 1.0
 * @since 2019/7/31 20:51
 */
@Service
public class DatasourceQueryServiceImpl implements DatasourceQueryService {

    @Autowired
    private JobDatasourceService jobDatasourceService;

    @Override
    public List<String> getDBs(Long id) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        return new MongoDBQueryTool(datasource).getDBNames();
    }


    @Override
    public List<String> getTables(Long id, String tableSchema) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        //queryTool组装
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        if (JdbcConstants.HBASE.equals(datasource.getDatasource())) {
            return new HBaseQueryTool(datasource).getTableNames();
        } else if (JdbcConstants.MONGODB.equals(datasource.getDatasource())) {
            return new MongoDBQueryTool(datasource).getCollectionNames(datasource.getDatabaseName());
        } else {
            BaseQueryTool qTool = QueryToolFactory.getByDbType(datasource);
            if(StringUtils.isBlank(tableSchema)){
                return qTool.getTableNames();
            }else{
                return qTool.getTableNames(tableSchema);
            }
        }
    }

    @Override
    public List<String> getTableSchema(Long id) {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        //queryTool组装
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        BaseQueryTool qTool = QueryToolFactory.getByDbType(datasource);
        return qTool.getTableSchema();
    }

    @Override
    public List<String> getCollectionNames(long id, String dbName) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        //queryTool组装
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        return new MongoDBQueryTool(datasource).getCollectionNames(dbName);
    }


    @Override
    public List<String> getColumns(Long id, String tableName) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        //queryTool组装
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        if (JdbcConstants.HBASE.equals(datasource.getDatasource())) {
            return new HBaseQueryTool(datasource).getColumns(tableName);
        } else if (JdbcConstants.MONGODB.equals(datasource.getDatasource())) {
            return new MongoDBQueryTool(datasource).getColumns(tableName);
        } else {
            BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
            return queryTool.getColumnNames(tableName, datasource.getDatasource());
        }
    }

    @Override
    public List<ColumnInfo> getColumnsDetail(Long id, String tableName) throws IOException {
        // 获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        // HBase特殊处理
        if (JdbcConstants.HBASE.equals(datasource.getDatasource())) {
            List<String> columns = new HBaseQueryTool(datasource).getColumns(tableName);
            return parseHBaseColumns(columns);
        }
        // MongoDB特殊处理
        if (JdbcConstants.MONGODB.equals(datasource.getDatasource())) {
            List<String> columns = new MongoDBQueryTool(datasource).getColumns(tableName);
            return parseMongoColumns(columns);
        }
        // RDBMS（MySQL、Oracle、PostgreSQL、SQL Server、Hive、ClickHouse、DM等）
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
        return queryTool.getColumns(tableName);
    }

    /**
     * 解析HBase列字符串为ColumnInfo列表
     * HBase格式: "列族:列名"
     */
    private List<ColumnInfo> parseHBaseColumns(List<String> columns) {
        List<ColumnInfo> result = new ArrayList<>();
        if (columns != null) {
            for (String col : columns) {
                ColumnInfo info = new ColumnInfo();
                info.setName(col);
                // HBase只有列族:列名，没有单独的类型和注释
                info.setType("");
                info.setComment("");
                result.add(info);
            }
        }
        return result;
    }

    /**
     * 解析MongoDB列字符串为ColumnInfo列表
     * MongoDB格式: "字段名:类型"
     */
    private List<ColumnInfo> parseMongoColumns(List<String> columns) {
        List<ColumnInfo> result = new ArrayList<>();
        if (columns != null) {
            for (String col : columns) {
                ColumnInfo info = new ColumnInfo();
                if (col.contains(":")) {
                    String[] parts = col.split(":", 2);
                    info.setName(parts[0]);
                    info.setType(parts.length > 1 ? parts[1] : "");
                } else {
                    info.setName(col);
                    info.setType("");
                }
                info.setComment("");
                result.add(info);
            }
        }
        return result;
    }

    @Override
    public List<String> getColumnsByQuerySql(Long datasourceId, String querySql) throws SQLException {
        //获取数据源对象
        JobDatasource jdbcDatasource = jobDatasourceService.getById(datasourceId);
        //queryTool组装
        if (ObjectUtil.isNull(jdbcDatasource)) {
            return Lists.newArrayList();
        }
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(jdbcDatasource);
        return queryTool.getColumnsByQuerySql(querySql);
    }
}
