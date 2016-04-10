package xyz.selfenrichment.robertotomas.popularmovies.SQLite;
//Created by RobertoTomás on 0031, 31, 3, 2016.
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * The parent class for contracts, contianing details about both the database structure and the URI
 * schema.
 */
public class ContractEntryWithAPI {

    public static String TABLE_NAME;
    public static String TABLE_SCHEMA;
    public static Columns COLUMNS;
    public static ContentUri CONTENT_URI;

    public ContractEntryWithAPI(String tableName, String schema, String apiPath) {
        TABLE_NAME = tableName;
        COLUMNS = new Columns();
        CONTENT_URI = new ContentUri(tableName, apiPath);
        TABLE_SCHEMA = schema;
    }
    public ContractEntryWithAPI(String tableName, String apiPath) {
        TABLE_NAME = tableName;
        COLUMNS = new Columns();
        CONTENT_URI = new ContentUri(tableName, apiPath);
    }
    public ContractEntryWithAPI(String tableName, String schema, Map<String, String> columnTypeMap, String apiPath) {
        TABLE_NAME = tableName;
        COLUMNS = new Columns(columnTypeMap);
        CONTENT_URI = new ContentUri(tableName, apiPath);
        TABLE_SCHEMA = schema;
    }
    public ContractEntryWithAPI(String tableName, Map<String, String> columnTypeMap, String apiPath) {
        TABLE_NAME = tableName;
        COLUMNS = new Columns(columnTypeMap);
        CONTENT_URI = new ContentUri(tableName, apiPath);
    }

    public String buildSchema(Map<String, String> modifiers) throws Exception {
        String schema;
        try {
            schema = _buildSchema(modifiers);

            // clean up trailing comma since there are no tableModifiers
            schema.substring(0,schema.length()-1);
        } catch (Exception e) {
            throw e;
        }
        return schema + ");";
    }

    public String buildSchema(Map<String, String> modifiers, String tableModifiers) throws Exception {
        String schema;
        try {
            schema = _buildSchema(modifiers);
        } catch (Exception e) {
            throw e;
        }
        return schema + tableModifiers + ");";
    }

    public void buildAndSaveSchema(Map<String, String> modifiers) throws Exception {
        try {
            TABLE_SCHEMA = buildSchema(modifiers);
        } catch(Exception e) {
            throw e;
        }
    }
    public void buildAndSaveSchema(Map<String, String> modifiers, String tableModifiers) throws Exception {
        try {
            TABLE_SCHEMA = buildSchema(modifiers, tableModifiers);
        } catch(Exception e) {
            throw e;
        }
    }

    private String _buildSchema(Map<String, String> modifiers) throws Exception {
        String schema = TABLE_NAME + " (";

        HashSet<String> modifierKeys = new HashSet<String>(modifiers.keySet());
        for(Map.Entry<String, String> entry: COLUMNS.COLUMN_NAME_TYPE_MAP.entrySet()){
            String columnName = entry.getKey();
            String columnType = entry.getValue();

            schema += columnName + " " + columnType;

            if (modifiers.containsKey(columnName)) {
                String columnModifiers = modifiers.get(columnName);

                schema += " " + columnModifiers;
            }
            schema += ", ";

            modifierKeys.remove(columnName);
        }
        if(!modifierKeys.isEmpty()){
            throw new Exception("[ContractEntryWithAPI] extra modifier(s) sent for table: '" + TABLE_NAME + "' " + modifierKeys.toString());
        }
        return schema;
    }

    public Uri buildTypeUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI.CONTENT_URI, id);
    }

    // inner classes
    public class ContentUri {
        public final String CONTENT_AUTHORITY = "xyz.selfenrichment.robertotomas.popularmovies";
        public final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

        public String API_PATH;

        public Uri CONTENT_URI;
        public String CONTENT_TYPE;
        public String CONTENT_ITEM_TYPE;

        public ContentUri (String relativePath, String apiPath){
            CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(relativePath).build();
            CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + relativePath;
            CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + relativePath;
            API_PATH = apiPath;
        }
    }

    public class Columns {
        public Map<String, String> COLUMN_NAME_TYPE_MAP = new HashMap<String, String>();

        public Columns(Map nameTypeMapping) {
            COLUMN_NAME_TYPE_MAP = nameTypeMapping;
        }

        public Columns() {
        }

        public String[] getNames() {
            return COLUMN_NAME_TYPE_MAP.keySet().toArray(new String[COLUMN_NAME_TYPE_MAP.size()]);
        }

        public String getType(String columnName) {
            return COLUMN_NAME_TYPE_MAP.get(columnName);
        }

        public Boolean putColumn(String name, String type) {
            if (DBUtilities.isValidType(type)) {
                COLUMN_NAME_TYPE_MAP.put(name, type);
                if (COLUMN_NAME_TYPE_MAP.containsKey(name)) {
                    //Log.w("ContractEntryWithAPI", "Columns subclass - attempted to add column already present. column name: '" + name + "' type: '" + type + "', column already present's type '" + COLUMN_NAME_TYPE_MAP.get(name) + "'");
                    return false;
                }
                return true;
            } else {
                Log.w("ContractEntryWithAPI", "Columns subclass - invalid type passed to addColumn. column name: '" + name + "' type: '" + type + "'");
            }
            return false;
        }

        public Boolean removeColumn(String name) {
            if (COLUMN_NAME_TYPE_MAP.containsKey(name)) {
                COLUMN_NAME_TYPE_MAP.remove(name);
                return true;
            }
            return false;
        }
    }
}
