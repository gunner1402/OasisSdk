package com.oasis.sdk.base.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.oasis.sdk.base.Exception.OasisSdkException;

/**
 * 数据库操作的帮助类.
 * 
 * @author xdb
 * 
 */
public class DBHelper {

	private static final String TAG = "DBHelper";
	private static final int DATABASE_VERSION = 5;
	private static final String DATABASE_NAME = "OASSDK";

	private final Context context;
	private DatabaseHelper DBHelper;
	public SQLiteDatabase db;
	private String[] createTable;
	private String[] dropTable;

	/**
	 * 构造方法.
	 * 
	 * @param ctx
	 *            上下文对象
	 * @param createTable
	 *            建表的sql语句,是一个数组,每个元素是一个建表语句.
	 * @param dropTable
	 *            删除表的sql语句,是一个数组,每个元素是一个删除表语句.
	 */
	public DBHelper(Context ctx, String[] createTable, String[] dropTable) {

		this.context = ctx;
		this.createTable = createTable;
		this.dropTable = dropTable;
		DBHelper = new DatabaseHelper(context);
	}

	/**
	 * 主要是建数据库和升级数据库.
	 * 
	 * @author Administrator
	 * 
	 */
	private class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			int len = createTable.length;
			for (int i = 0; i < len; i++) {
				if (createTable[i] != null && createTable[i].trim().length() > 0) {
					db.execSQL(createTable[i]);
				}
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			int len = dropTable.length;
			for (int i = 0; i < len; i++) {
				db.execSQL(dropTable[i]);
			}

			onCreate(db);
		}
	}

	/**
	 * 执行sql操作数据库
	 * 
	 * @param sql
	 */
	public void execSQL(String sql) {
		db.execSQL(sql);
	}

	/**
	 * 打开数据库.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public DBHelper open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	/**
	 * 关才数据库.
	 */
	public void close() {
		DBHelper.close();
	}

	public long insert(String tableName, ContentValues initialValues) {
		return db.insert(tableName, null, initialValues);
	}

	/**
	 * 删除记录.
	 * 
	 * @param tableName
	 *            表名
	 * @param where
	 *            条件.
	 * @return
	 */
	public boolean delete(String tableName, String where) {
		return db.delete(tableName, where, null) > 0;
	}

	/**
	 * 删除记录.
	 * 
	 * @param tableName
	 *            表名
	 * @param where
	 *            条件.
	 * @return
	 */
	public int delete(String tableName, String where, String[] whereArgs) {
		return db.delete(tableName, where, whereArgs);
	}

	/**
	 * 加载所有数据.
	 * 
	 * @param tableName
	 *            表名
	 * @param columns
	 *            列名,字符串数组,每一个元素是要显示的列.
	 * @param order
	 *            排序方式.
	 * @return
	 */
	public Cursor loadAll(String tableName, String[] columns, String order) {

		return db.query(tableName, columns, null, null, null, null, order);
	}

	/**
	 * 根据指定的条件加载数据.
	 * 
	 * @param tableName
	 *            表名
	 * @param columns
	 *            列名,字符串数组,每一个元素是要显示的列.
	 * @param where
	 *            条件.
	 * @return
	 * @throws SQLException
	 */
	public Cursor loadByWhere(String tableName, String[] columns, String where) throws SQLException {

		Cursor mCursor = db.query(true, tableName, columns, where, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;
	}

	/**
	 * 根据指定的条件加载数据.
	 * 
	 * @param tableName
	 *            表名
	 * @param columns
	 *            列名,字符串数组,每一个元素是要显示的列.
	 * @param where
	 *            条件.
	 * @return
	 * @throws SQLException
	 */
	public Cursor loadByWhere(String tableName, String[] columns, String where, String[] whereArgs) throws SQLException {

		Cursor mCursor = db.query(true, tableName, columns, where, whereArgs, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;
	}

	/**
	 * 更新数据.
	 * 
	 * @param tableName
	 *            表名
	 * @param args
	 *            参数.
	 * @param where
	 *            条件.
	 * @return
	 */
	public boolean update(String tableName, ContentValues args, String where) {

		return db.update(tableName, args, where, null) > 0;
	}

	/**
	 * 更新数据.
	 * 
	 * @param tableName
	 *            表名
	 * @param args
	 *            参数.
	 * @param where
	 *            条件.
	 * @return
	 */
	public boolean update(String tableName, ContentValues args, String where, String[] whereArgs) {

		return db.update(tableName, args, where, whereArgs) > 0;
	}

	/**
	 * 不带查询条件的数据库查询.
	 * 
	 * @param tableName
	 *            表名
	 * @param columns
	 *            字段名
	 * @param order
	 *            顺序
	 * @param obj
	 *            需要映射的对象
	 * @return List
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws OasisSdkException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List loadAll2List(String tableName, String[] columns, String order, Object obj) throws SQLException, IllegalAccessException, InstantiationException,
	OasisSdkException {
		List list = new ArrayList();
		Cursor mCursor = db.query(tableName, columns, null, null, null, null, order);
		String columnName;
		String columnValue;
		Object valueObj;
		if (mCursor.getCount() > 0) {
			while (mCursor.moveToNext()) {
				valueObj = obj.getClass().newInstance();
				for (int i = 0; i < columns.length; i++) {
					columnName = columns[i];
					columnValue = mCursor.getString(mCursor.getColumnIndex(columnName));
					this.setValue(columnName, columnValue, valueObj);
				}
				list.add(valueObj);
			}
		}
		mCursor.close();
		return list;
	}

	/**
	 * * 带查询条件的数据库查询.
	 * 
	 * @param tableName
	 *            表名
	 * @param columns
	 *            字段名
	 * @param obj
	 *            需要映射的对象
	 * @param selection
	 *            查询条件
	 * @param selectionArgs
	 *            查询条件对应的值
	 * @param groupBy
	 *            groupBy
	 * @param orderBy
	 *            orderBy
	 * @param limit
	 *            limit
	 * @return List
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws OasisSdkException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List loadByWhere2List(String tableName, String[] columns, Object obj, String selection, String[] selectionArgs, String groupBy, String orderBy,
			String limit) throws SQLException, IllegalAccessException, InstantiationException, OasisSdkException {

		List list = new ArrayList();
		Cursor mCursor = db.query(true, tableName, columns, selection, selectionArgs, groupBy, null, orderBy, limit);
		String columnName;
		String columnValue;
		Object valueObj;
		if (mCursor.getCount() > 0) {
			while (mCursor.moveToNext()) {
				valueObj = obj.getClass().newInstance();
				for (int i = 0; i < columns.length; i++) {
					columnName = columns[i];
					columnValue = mCursor.getString(mCursor.getColumnIndex(columnName));
					this.setValue(columnName, columnValue, valueObj);
				}
				list.add(valueObj);
			}
		}
		mCursor.close();
		return list;
	}

	/**
	 * 对象赋值方法.
	 * 
	 * @param fieldName
	 *            属性名称
	 * @param fieldValue
	 *            属性值
	 * @param obj
	 *            所属对象
	 * @throws OasisSdkException
	 */
	@SuppressWarnings({ "rawtypes" })
	private void setValue(String fieldName, String fieldValue, Object obj) throws OasisSdkException {

		if (fieldValue == null || fieldValue.length() == 0)
			return;

		Class cls = obj.getClass();

		String methodName = getMethodName(fieldName);
		Method method;

		try {
			method = cls.getDeclaredMethod(methodName, String.class);
			method.invoke(obj, new Object[] { fieldValue });
		} catch (SecurityException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (NoSuchMethodException e) {
			Class superclass = cls.getSuperclass();
			try {
				method = superclass.getDeclaredMethod(methodName, String.class);
				method.invoke(obj, new Object[] { fieldValue });
			} catch (SecurityException ex) {
				throw new OasisSdkException(ex.getMessage());
			} catch (NoSuchMethodException ex) {
				return;
			} catch (IllegalArgumentException ex) {
				throw new OasisSdkException(ex.getMessage());
			} catch (IllegalAccessException ex) {
				throw new OasisSdkException(ex.getMessage());
			} catch (InvocationTargetException ex) {
				throw new OasisSdkException(ex.getMessage());
			}
		}

	}

	/**
	 * 根据属性名得到相应的set方法名.
	 * 
	 * @param fieldName
	 *            属性名
	 * @return
	 */
	private String getMethodName(String fieldName) {
		char[] charArray = fieldName.toCharArray();
		if (charArray[0] >= 'a' && charArray[0] <= 'z')
			charArray[0] = (char) (charArray[0] - 32);
		return "set" + new String(charArray);
	}
}
