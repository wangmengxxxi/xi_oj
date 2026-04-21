import java.lang.reflect.Method;

public class _TmpInspectTool {
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("dev.langchain4j.agent.tool.Tool");
        System.out.println(c.getName());
        for (Method m : c.getDeclaredMethods()) {
            System.out.println(m.getName() + " -> " + m.getReturnType().getSimpleName());
        }
    }
}
