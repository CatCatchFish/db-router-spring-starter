package cn.cat.middleware.db.router;

/**
 * 数据库上下文Holder
 */
public class DBContextHolder {
    // 记录分库结果
    private static final ThreadLocal<String> dbKey = new ThreadLocal<>();
    // 记录分表结果
    private static final ThreadLocal<String> tbKey = new ThreadLocal<>();

    public static void setDbKey(String dbKeyIdx) {
        dbKey.set(dbKeyIdx);
    }

    public static String getDbKey() {
        return dbKey.get();
    }

    public static void clearDbKey() {
        dbKey.remove();
    }

    public static void setTbKey(String tbKeyIdx) {
        tbKey.set(tbKeyIdx);
    }

    public static String getTbKey() {
        return tbKey.get();
    }

    public static void clearTbKey() {
        tbKey.remove();
    }
}
