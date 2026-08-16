package io.poby_house.log;

public class JpaQueryContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    
    /** null 을 넣으면 지운다. 스레드가 재사용될 때 값이 남아 있지 않게 한다 */
    public static void set(String context) {
        if (context == null) {
            contextHolder.remove();
        } else {
            contextHolder.set(context);
        }
    }
    
    public static String get() {
        return contextHolder.get();
    }
    
    public static void clear() {
        contextHolder.remove();
    }
}
