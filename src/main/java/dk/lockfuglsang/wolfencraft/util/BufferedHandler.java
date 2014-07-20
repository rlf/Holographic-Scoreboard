package dk.lockfuglsang.wolfencraft.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Generic Invocation Handler supporting interception of messages.
 */
public class BufferedHandler implements InvocationHandler {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private PrintStream ps;

    private Object proxee;
    public BufferedHandler(Object proxee) {
        this.proxee = proxee;
        try {
            ps = new PrintStream(baos, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("WTH! Your operating system doesn't support UTF-8, get real!");
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("sendMessage") || method.getName().equals("sendRawMessage")) {
            if (args.length == 1 && args[0] instanceof String) {
                ps.println(args[0]);
            } else if (args.length == 1 && args[0] != null && args[0].getClass().isArray() && args[0].getClass().getComponentType().isAssignableFrom(String.class)) {
                for (String s : (String[]) args[0]) {
                    ps.println(s);
                }
            } else {
                for (Object o : args) {
                    ps.println("" + o);
                }
            }
            return null;
        }
        return method.invoke(proxee, args);
    }

    public String getStdout() {
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("WTH! Your operating system doesn't support UTF-8, get real!");
        }
    }

}
