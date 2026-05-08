
import org.bukkit.Bukkit;
import org.bukkit.Registry;

public class MethodList {
    public static void main(String[] args) {
        for (java.lang.reflect.Method m : Bukkit.class.getMethods()) {
            if (m.getName().contains("getRegistry")) {
                System.out.println(m.toString());
            }
        }
    }
}
