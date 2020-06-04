package xyz.xiezc.ioc.starter.orm.xml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import lombok.Data;
import org.apache.ibatis.builder.BuilderException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import xyz.xiezc.ioc.starter.orm.util.DocumentUtil;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Data
public class DocumentMapperDefine {
    boolean validation = true;

    MapperDefine mapperDefine;

    Document document;

    String nameSpace;

    Path path;

    boolean hasCheckDoc = false;

    public DocumentMapperDefine(MapperDefine mapperDefine, Path path) throws IOException {
        InputSource inputSource = new InputSource(Files.newBufferedReader(path));
        try {
            this.path = path;
            inputSource.setEncoding("utf8");
            this.mapperDefine = mapperDefine;
            document = this.createDocument(inputSource);
            nameSpace = getNameSpace();
            if (!Objects.equals(nameSpace, mapperDefine.getMapperInterface().getName())) {
                throw new RuntimeException("文档与mapper接口不对应");
            }
        } finally {
            inputSource.getCharacterStream().close();
        }
    }

    public DocumentMapperDefine(File path) throws IOException {
        InputSource inputSource = new InputSource(FileUtil.getInputStream(path));
        try {
            this.path = path.toPath();
            inputSource.setEncoding("utf8");
            document = this.createDocument(inputSource);
            nameSpace = getNameSpace();
        } finally {
            inputSource.getCharacterStream().close();
        }
    }

    public DocumentMapperDefine(Path path) throws IOException {
        InputSource inputSource = new InputSource(Files.newBufferedReader(path));
        try {
            this.path = path;
            inputSource.setEncoding("utf8");
            document = this.createDocument(inputSource);
            nameSpace = getNameSpace();
        } finally {
            inputSource.getCharacterStream().close();
        }
    }

    public DocumentMapperDefine(MapperDefine mapperDefine) {
        this.mapperDefine = mapperDefine;
        document = this.createDocument();
        nameSpace = mapperDefine.getMapperInterface().getName();
        this.hasCheckDoc = true;
    }


    /**
     * 通过解析文档, 获取文档对应的mapper接口
     *
     * @return
     */
    public String getNameSpace() {
        NodeList mapper = document.getElementsByTagName("mapper");
        if (mapper.getLength() > 0) {
            Node node = mapper.item(0);
            NamedNodeMap attributes = node.getAttributes();
            return attributes.getNamedItem("namespace").getNodeValue();
        }
        return null;
    }

    public void checkDoc() {
        if (hasCheckDoc) {
            return;
        }
        Element mapper = document.getDocumentElement();
        checkResultMap(mapper);
        checkSql(mapper);
        checkSelect(mapper);
        checkUpdate(mapper);
        checkDelete(mapper);
        checkInsert(mapper);
        hasCheckDoc = true;
    }

    private void checkInsert(Element mapper) {
        NodeList inserts = mapper.getElementsByTagName("insert");
        List<String> insertIds = new ArrayList<>();
        insertIds.add("insert");
        insertIds.add("insertSelective");

        checkById(inserts, insertIds);

        if (insertIds.contains("insert")) {
            mapper.appendChild(this.createInsert());
        }
        if (insertIds.contains("insertSelective")) {
            mapper.appendChild(this.createInsertSelective());
        }
    }

    private void checkDelete(Element mapper) {
        NodeList deletes = mapper.getElementsByTagName("delete");

        List<String> deleteIds = new ArrayList<>();
        deleteIds.add("deleteByPrimaryKey");
        deleteIds.add("deleteByExample");
        checkById(deletes, deleteIds);

        if (deleteIds.contains("deleteByExample")) {
            mapper.appendChild(this.createDeleteByExample());
        }
        if (deleteIds.contains("deleteByPrimaryKey")) {
            mapper.appendChild(this.createDeleteByPrimaryKey());
        }
    }


    private void checkUpdate(Element mapper) {
        NodeList updates = mapper.getElementsByTagName("update");

        List<String> updateIds = new ArrayList<>();
        updateIds.add("updateByPrimaryKey");
        updateIds.add("updateByPrimaryKeySelective");
        updateIds.add("updateByExample");
        updateIds.add("updateByExampleSelective");

        checkById(updates, updateIds);
        if (updateIds.contains("updateByPrimaryKey")) {
            mapper.appendChild(this.createUpdateByPrimaryKey());
        }
        if (updateIds.contains("updateByPrimaryKeySelective")) {
            mapper.appendChild(this.createUpdateByPrimaryKeySelective());
        }
        if (updateIds.contains("updateByExample")) {
            mapper.appendChild(this.createUpdateByExample());
        }
        if (updateIds.contains("updateByExampleSelective")) {
            mapper.appendChild(this.createUpdateByExampleSelective());
        }

    }


    private void checkSelect(Element mapper) {
        NodeList selects = mapper.getElementsByTagName("select");

        List<String> selectIds = new ArrayList<>();
        selectIds.add("selectByPrimaryKey");
        selectIds.add("countByExample");
        selectIds.add("selectByExample");
        checkById(selects, selectIds);
        if (selectIds.contains("selectByPrimaryKey")) {
            mapper.appendChild(this.createSelectByPrimaryKey());
        }
        if (selectIds.contains("countByExample")) {
            mapper.appendChild(this.createCountByExample());
        }
        if (selectIds.contains("selectByExample")) {
            mapper.appendChild(this.createSelectByExample());
        }

    }

    private void checkById(NodeList selects, List<String> selectIds) {
        int length = selects.getLength();
        for (int i = 0; i < length; i++) {
            Node sql = selects.item(i);
            Node id = sql.getAttributes().getNamedItem("id");
            if (id == null) {
                continue;
            }
            selectIds.remove(id.getNodeValue());

        }
    }


    private void checkSql(Element mapper) {
        NodeList sqls = mapper.getElementsByTagName("sql");

        List<String> sqlStr = new ArrayList<>();
        sqlStr.add("XZCBase_Column_List");
        sqlStr.add("Update_By_XZCExample_Where_Clause");
        sqlStr.add("XZCExample_Where_Clause");

        checkById(sqls, sqlStr);

        if (sqlStr.contains("XZCBase_Column_List")) {
            mapper.appendChild(this.createAllColumnSql());
        }
        if (sqlStr.contains("XZCExample_Where_Clause")) {
            mapper.appendChild(this.createWhereSql());
        }
        if (sqlStr.contains("Update_By_XZCExample_Where_Clause")) {
            mapper.appendChild(this.createUpdateWhereSql());
        }
    }

    private void checkResultMap(Element mapper) {
        NodeList resultMaps = mapper.getElementsByTagName("resultMap");
        int length = resultMaps.getLength();
        for (int i = 0; i < length; i++) {
            Node resultMap = resultMaps.item(i);
            Node xzcBaseResultMap = resultMap.getAttributes().getNamedItem("XZCBaseResultMap");
            if (xzcBaseResultMap != null) {
                return;
            }
        }
        mapper.appendChild(this.createResultMap());
    }


    private Document createDocument(InputSource inputSource) {
        // important: this must only be called AFTER common constructor
        try {
            DocumentBuilder builder = DocumentUtil.getDocumentBuilder(validation);
            return builder.parse(inputSource);
        } catch (Exception e) {
            throw new BuilderException("Error creating document instance.  Cause: " + e, e);
        }
    }


    private Document createDocument() {
        try {
            DocumentBuilder builder = DocumentUtil.getDocumentBuilder(validation);
            document = builder.newDocument();
            this.createMapperXml();
            return document;
        } catch (Exception e) {
            throw new BuilderException("Error creating document instance.  Cause: " + e, e);
        }
    }


    private Element createMapper() {
        Document doc = document;
        Element mapper = doc.createElement("mapper");
        mapper.setAttribute("namespace", mapperDefine.getMapperInterface().getName());
        return mapper;
    }

    private Element createResultMap() {
        Document doc = document;
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Element resultMap = doc.createElement("resultMap");
        resultMap.setAttribute("id", "XZCBaseResultMap");
        resultMap.setAttribute("type", entityTableDefine.getTable().getClazz().getName());
        Element id = doc.createElement("id");

        //   column="id" jdbcType="INTEGER" property="id" />
        id.setAttribute("column", entityTableDefine.getId().getColumn());
        id.setAttribute("property", entityTableDefine.getId().getProperty());
        resultMap.appendChild(id);
        //column
        Set<EntityTableDefine.ColumnProp> columns = entityTableDefine.getColumns();
        columns = CollUtil.emptyIfNull(columns);
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            Element result = doc.createElement("result");
            resultMap.appendChild(result);
            result.setAttribute("column", columnProp.getColumn());
            result.setAttribute("property", columnProp.getProperty());
        }
        return resultMap;
    }

    private Element createUpdateByPrimaryKey() {
        Document doc = document;
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Element update = doc.createElement("update");
        update.setAttribute("id", "updateByPrimaryKey");
        update.setAttribute("parameterType", entityTableDefine.getTable().getClazz().getName());
        update.appendChild(doc.createTextNode(" update " + entityTableDefine.getTable().getColumn()));
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" set ");
        Set<EntityTableDefine.ColumnProp> columns = entityTableDefine.getColumns();
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            stringBuffer.append(columnProp.getColumn()).append("=#{")
                    .append(columnProp.getProperty()).append("},");
        }
        stringBuffer.append(" where " + entityTableDefine.getId().getColumn() + " = #{" + entityTableDefine.getId().getProperty() + " } ");
        update.appendChild(doc.createTextNode(stringBuffer.toString()));
        return update;
    }

    private Element createUpdateByPrimaryKeySelective() {
        Document doc = document;
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Element update = doc.createElement("update");
        update.setAttribute("id", "updateByPrimaryKeySelective");
        update.setAttribute("parameterType", entityTableDefine.getTable().getClazz().getName());
        update.appendChild(doc.createTextNode(" update " + entityTableDefine.getTable().getColumn()));
        Element set = doc.createElement("set");
        update.appendChild(set);

        Set<EntityTableDefine.ColumnProp> columns = entityTableDefine.getColumns();
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            Element anIf = doc.createElement("if");
            set.appendChild(anIf);
            anIf.setAttribute("test", columnProp.getProperty() + " != null");
            anIf.appendChild(doc.createTextNode(columnProp.getColumn() + " = #{" + columnProp.getProperty() + "},"));
        }

        update.appendChild(doc.createTextNode(" where " + entityTableDefine.getId().getColumn() + "= #{" + entityTableDefine.getId().getProperty() + "}"));
        return update;
    }

    private Element createUpdateByExample() {
        Document doc = document;
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Element update = doc.createElement("update");
        update.setAttribute("id", "updateByExample");
        update.setAttribute("parameterType", "map");
        update.appendChild(doc.createTextNode(" update " + entityTableDefine.getTable().getColumn()));
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" set ").append(entityTableDefine.getId().getColumn())
                .append("=#{record.").append(entityTableDefine.getId().getProperty())
                .append("},");
        Set<EntityTableDefine.ColumnProp> columns = entityTableDefine.getColumns();
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            stringBuffer.append(columnProp.getColumn()).append("= #{record.")
                    .append(columnProp.getProperty()).append("},");
        }
        Element anIf = doc.createElement("if");
        update.appendChild(anIf);
        anIf.setAttribute("test", "_parameter != null");
        Element include = doc.createElement("include");
        anIf.appendChild(include);
        include.setAttribute("refid", "Update_By_XZCExample_Where_Clause");
        return update;
    }

    private Element createUpdateByExampleSelective() {
        Document doc = document;
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Element update = doc.createElement("update");
        update.setAttribute("id", "updateByExampleSelective");
        update.setAttribute("parameterType", "map");
        update.appendChild(doc.createTextNode(" update " + entityTableDefine.getTable().getColumn()));
        Element set = doc.createElement("set");
        update.appendChild(set);
        Set<EntityTableDefine.ColumnProp> columns = entityTableDefine.getColumns();
        Element anIf = doc.createElement("if");
        set.appendChild(anIf);
        anIf.setAttribute("test", "record." + entityTableDefine.getId().getProperty() + " != null");
        anIf.appendChild(doc.createTextNode(entityTableDefine.getId().getColumn() + "= #{record." + entityTableDefine.getId().getProperty() + "},"));
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            Element anIf1 = doc.createElement("if");
            set.appendChild(anIf1);
            anIf1.setAttribute("test", "record." + columnProp.getProperty() + " != null");
            anIf1.appendChild(doc.createTextNode(columnProp.getColumn() + "= #{record." + columnProp.getProperty() + "},"));
        }
        Element anIf2 = doc.createElement("if");
        update.appendChild(anIf2);
        anIf2.setAttribute("test", "_parameter  != null");
        Element include = doc.createElement("include");
        anIf2.appendChild(include);
        include.setAttribute("refid", "Update_By_XZCExample_Where_Clause");
        return update;
    }

    private Element createCountByExample() {
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Document doc = document;
        Element select = doc.createElement("select");
        select.setAttribute("id", "countByExample");
        select.setAttribute("parameterType", EntityTableDefine.ExampleName.getName());
        select.setAttribute("resultType", "java.lang.Long");
        select.appendChild(doc.createTextNode(" select count(*) from " + entityTableDefine.getTable().getColumn()));
        Element anIf = doc.createElement("if");
        select.appendChild(anIf);
        anIf.setAttribute("test", "_parameter != null");
        Element include = doc.createElement("include");
        anIf.appendChild(include);
        include.setAttribute("refid", "XZCExample_Where_Clause");
        return select;

    }

    private Element createInsertSelective() {
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Document doc = document;
        Element insert = doc.createElement("insert");
        insert.setAttribute("id", "insertSelective");
        insert.setAttribute("keyColumn", entityTableDefine.getId().getColumn());
        insert.setAttribute("keyProperty", entityTableDefine.getId().getProperty());
        insert.setAttribute("parameterType", entityTableDefine.getTable().getClazz().getName());
        insert.setAttribute("useGeneratedKeys", "true");
        insert.appendChild(doc.createTextNode(" insert into " + entityTableDefine.getTable().getColumn()));
        Element trim = doc.createElement("trim");
        insert.appendChild(trim);
        trim.setAttribute("prefix", "(");
        trim.setAttribute("suffix", ")");
        trim.setAttribute("suffixOverrides", ",");
        Set<EntityTableDefine.ColumnProp> columns = entityTableDefine.getColumns();
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            Element anIf = doc.createElement("if");
            trim.appendChild(anIf);
            anIf.setAttribute("test", columnProp.getProperty() + " != null");
            anIf.appendChild(doc.createTextNode(columnProp.getColumn() + ","));
        }
        Element trim1 = doc.createElement("trim");
        insert.appendChild(trim1);
        trim1.setAttribute("prefix", "values (");
        trim1.setAttribute("suffix", ")");
        trim1.setAttribute("suffixOverrides", ",");
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            Element anIf = doc.createElement("if");
            trim1.appendChild(anIf);
            anIf.setAttribute("test", columnProp.getProperty() + " != null");
            anIf.appendChild(doc.createTextNode(" #{" + columnProp.getProperty() + "},"));
        }
        return insert;
    }


    private Element createInsert() {
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Document doc = document;
        Element insert = doc.createElement("insert");
        insert.setAttribute("id", "insert");
        insert.setAttribute("keyColumn", entityTableDefine.getId().getColumn());
        insert.setAttribute("keyProperty", entityTableDefine.getId().getProperty());
        insert.setAttribute("parameterType", entityTableDefine.getTable().getClazz().getName());
        insert.setAttribute("useGeneratedKeys", "true");
        StringBuffer stringBuffer = new StringBuffer();
//
        stringBuffer.append("  insert into ")
                .append(entityTableDefine.getTable().getColumn())
                .append(" (");
        Set<EntityTableDefine.ColumnProp> columns = entityTableDefine.getColumns();
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            stringBuffer.append(columnProp.getColumn()).append(",");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1).append(" )   values (");
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            stringBuffer.append("#{");
            stringBuffer.append(columnProp.getProperty()).append("},");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1).append(" )");
        insert.appendChild(doc.createTextNode(stringBuffer.toString()));
        return insert;

    }

    private Element createDeleteByExample() {
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Document doc = document;
        Element select = doc.createElement("delete");
        select.setAttribute("id", "deleteByExample");
        select.setAttribute("parameterType", EntityTableDefine.ExampleName.getName());
        select.appendChild(doc.createTextNode("  delete from " + entityTableDefine.getTable().getColumn()));
        Element anIf = doc.createElement("if");
        select.appendChild(anIf);
        anIf.setAttribute("test", "_parameter != null");
        Element include = doc.createElement("include");
        anIf.appendChild(include);
        include.setAttribute("refid", "XZCExample_Where_Clause");
        return select;
    }

    private Element createDeleteByPrimaryKey() {
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Document doc = document;
        Element select = doc.createElement("delete");
        select.setAttribute("id", "deleteByPrimaryKey");
        select.setAttribute("parameterType", entityTableDefine.getId().getClazz().getName());
        select.appendChild(doc.createTextNode(" delete from " + entityTableDefine.getTable().getColumn()));
        select.appendChild(doc.createTextNode(" where " + entityTableDefine.getId().getColumn() + "= #{" + entityTableDefine.getId().getProperty() + "}"));
        return select;
    }

    private Element createSelectByPrimaryKey() {
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Document doc = document;
        Element select = doc.createElement("select");
        select.setAttribute("id", "selectByPrimaryKey");
        select.setAttribute("parameterType", entityTableDefine.getId().getClazz().getName());
        select.setAttribute("resultMap", "XZCBaseResultMap");
        select.appendChild(doc.createTextNode(" select "));
        Element include = doc.createElement("include");
        select.appendChild(include);
        include.setAttribute("refid", "XZCBase_Column_List");
        select.appendChild(doc.createTextNode(" from " + entityTableDefine.getTable().getColumn() +
                "   where " + entityTableDefine.getId().getColumn() + " = #{" + entityTableDefine.getId().getProperty() + "}"));
        return select;
    }

    private Element createSelectByExample() {
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        Document doc = document;
        Element select = doc.createElement("select");
        select.setAttribute("id", "selectByExample");
        select.setAttribute("parameterType", EntityTableDefine.ExampleName.getName());
        select.setAttribute("resultMap", "XZCBaseResultMap");
        select.appendChild(doc.createTextNode(" select "));


        Element anIf = doc.createElement("if");
        select.appendChild(anIf);
        anIf.setAttribute("test", "distinct");
        anIf.appendChild(doc.createTextNode(" distinct "));

        Element include = doc.createElement("include");
        select.appendChild(include);
        include.setAttribute("refid", "XZCBase_Column_List");
        select.appendChild(doc.createTextNode(" from " + entityTableDefine.getTable().getColumn()));

        Element anIf1 = doc.createElement("if");
        select.appendChild(anIf1);
        anIf1.setAttribute("test", "_parameter != null");
        Element include1 = doc.createElement("include");
        anIf1.appendChild(include1);
        include1.setAttribute("refid", "XZCExample_Where_Clause");

        Element anIf2 = doc.createElement("if");
        select.appendChild(anIf2);
        anIf2.setAttribute("test", "orderByClause != null");
        anIf2.appendChild(doc.createTextNode(" order by ${orderByClause} "));

        Element anIf3 = doc.createElement("if");
        select.appendChild(anIf3);
        anIf3.setAttribute("test", "limit != null");
        Element anIf4 = doc.createElement("if");
        anIf3.appendChild(anIf4);
        anIf4.setAttribute("test", "offset != null");
        anIf4.appendChild(doc.createTextNode(" limit ${offset}, ${limit} "));
        Element anIf5 = doc.createElement("if");
        anIf3.appendChild(anIf5);
        anIf5.setAttribute("test", "offset == null");
        anIf5.appendChild(doc.createTextNode(" limit ${limit} "));
        return select;
    }


    private Element createAllColumnSql() {
        Document doc = document;
        Element sql = doc.createElement("sql");
        sql.setAttribute("id", "XZCBase_Column_List");
        EntityTableDefine entityTableDefine = mapperDefine.getEntityTableDefine();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" ").append(entityTableDefine.getId().getColumn()).append(" ");
        Set<EntityTableDefine.ColumnProp> columns = entityTableDefine.getColumns();
        columns = CollUtil.emptyIfNull(columns);
        for (EntityTableDefine.ColumnProp columnProp : columns) {
            stringBuffer.append(",");
            stringBuffer.append(" ").append(columnProp.getColumn()).append(" ");
        }
        sql.appendChild(doc.createTextNode(stringBuffer.toString()));
        return sql;
    }

    private Element createUpdateWhereSql() {
        Document doc = document;
        Element sql = doc.createElement("sql");
        sql.setAttribute("id", "Update_By_XZCExample_Where_Clause");
        Element where = doc.createElement("where");
        sql.appendChild(where);
        Element foreach = doc.createElement("foreach");
        where.appendChild(foreach);
        foreach.setAttribute("collection", "example.oredCriteria"); //collection="example.oredCriteria" item="criteria" separator="or"
        foreach.setAttribute("item", "criteria");
        foreach.setAttribute("separator", "or");
        Element anIf = doc.createElement("if");
        foreach.appendChild(anIf);
        anIf.setAttribute("test", "criteria.valid");
        Element trim = doc.createElement("trim");
        anIf.appendChild(trim);
        trim.setAttribute("prefix", "(");
        trim.setAttribute("prefixOverrides", "and");
        trim.setAttribute("suffix", ")");
        Element foreach1 = doc.createElement("foreach");
        trim.appendChild(foreach1);
        foreach1.setAttribute("collection", "criteria.criteria");
        foreach1.setAttribute("item", "criterion");
        Element choose = doc.createElement("choose");
        foreach1.appendChild(choose);
        Element when = doc.createElement("when");
        choose.appendChild(when);
        when.setAttribute("test", "criterion.noValue");
        when.appendChild(doc.createTextNode(" and ${criterion.condition} "));
        Element when1 = doc.createElement("when");
        choose.appendChild(when1);
        when1.setAttribute("test", "criterion.singleValue");
        when1.appendChild(doc.createTextNode(" and ${criterion.condition} #{criterion.value} "));
        Element when2 = doc.createElement("when");
        choose.appendChild(when2);
        when2.setAttribute("test", "criterion.betweenValue");
        when2.appendChild(doc.createTextNode(" and ${criterion.condition} #{criterion.value} and #{criterion.secondValue} "));
        Element when3 = doc.createElement("when");
        choose.appendChild(when3);
        when3.setAttribute("test", "criterion.listValue");
        when3.appendChild(doc.createTextNode(" and ${criterion.condition} "));
        Element foreach2 = doc.createElement("foreach");
        when3.appendChild(foreach2);
        foreach2.setAttribute("close", ")");
        foreach2.setAttribute("collection", "criterion.value");
        foreach2.setAttribute("item", "listItem");
        foreach2.setAttribute("open", "(");
        foreach2.setAttribute("separator", ",");
        foreach2.appendChild(doc.createTextNode(" #{listItem} "));
        return sql;
    }

    private Element createWhereSql() {
        Document doc = document;
        Element sql = doc.createElement("sql");
        sql.setAttribute("id", "XZCExample_Where_Clause");
        Element where = doc.createElement("where");
        sql.appendChild(where);
        Element foreach = doc.createElement("foreach");
        where.appendChild(foreach);
        foreach.setAttribute("collection", "oredCriteria");
        foreach.setAttribute("item", "criteria");
        foreach.setAttribute("separator", "or");
        Element anIf = doc.createElement("if");
        foreach.appendChild(anIf);
        anIf.setAttribute("test", "criteria.valid");
        Element trim = doc.createElement("trim");
        anIf.appendChild(trim);
        trim.setAttribute("prefix", "(");
        trim.setAttribute("prefixOverrides", "and");
        trim.setAttribute("suffix", ")");
        Element foreach1 = doc.createElement("foreach");
        trim.appendChild(foreach1);
        foreach1.setAttribute("collection", "criteria.criteria");
        foreach1.setAttribute("item", "criterion");
        Element choose = doc.createElement("choose");
        foreach1.appendChild(choose);
        Element when = doc.createElement("when");
        choose.appendChild(when);
        when.setAttribute("test", "criterion.noValue");
        Text textNode = doc.createTextNode(" and ${criterion.condition} ");
        when.appendChild(textNode);

        Element when1 = doc.createElement("when");
        choose.appendChild(when1);
        when1.setAttribute("test", "criterion.singleValue");
        when1.appendChild(doc.createTextNode(" and ${criterion.condition} #{criterion.value} "));

        Element when2 = doc.createElement("when");
        choose.appendChild(when2);
        when2.setAttribute("test", "riterion.betweenValue");
        when2.appendChild(doc.createTextNode(" and ${criterion.condition} #{criterion.value} and #{criterion.secondValue} "));


        Element when3 = doc.createElement("when");
        choose.appendChild(when3);
        when3.setAttribute("test", "criterion.listValue");
        when3.appendChild(doc.createTextNode(" and ${criterion.condition} "));

        Element foreach2 = doc.createElement("foreach");
        when3.appendChild(foreach2);
        foreach2.setAttribute("close", ")");
        foreach2.setAttribute("collection", "criterion.value");
        foreach2.setAttribute("item", "listItem");

        foreach2.setAttribute("separator", ",");
        foreach2.setAttribute("open", "(");
        foreach2.appendChild(doc.createTextNode("   #{listItem} "));
        return sql;
    }


    private void createMapperXml() {

        // 添加根节点
        Element mapper = this.createMapper();
        mapper.appendChild(this.createResultMap());
        mapper.appendChild(this.createWhereSql());
        mapper.appendChild(this.createUpdateWhereSql());
        mapper.appendChild(this.createAllColumnSql());
        mapper.appendChild(this.createSelectByExample());
        mapper.appendChild(this.createSelectByPrimaryKey());
        mapper.appendChild(this.createDeleteByPrimaryKey());
        mapper.appendChild(this.createDeleteByExample());
        mapper.appendChild(this.createInsert());
        mapper.appendChild(this.createInsertSelective());
        mapper.appendChild(this.createCountByExample());
        mapper.appendChild(this.createUpdateByExampleSelective());
        mapper.appendChild(this.createUpdateByExample());
        mapper.appendChild(this.createUpdateByPrimaryKeySelective());
        mapper.appendChild(this.createUpdateByPrimaryKey());

        document.appendChild(mapper);

    }


}
