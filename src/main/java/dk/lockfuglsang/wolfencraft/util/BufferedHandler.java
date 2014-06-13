package dk.lockfuglsang.wolfencraft.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Generic Invocation Handler supporting interception of messages.
 */
public class BufferedHandler implements InvocationHandler {
    private final PropertyChangeSupport propertyChangeSupport;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private PrintStream ps = new PrintStream(baos);

    private Object proxee;
    public BufferedHandler(Object proxee) {
        this.proxee = proxee;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("sendMessage") || method.getName().equals("sendRawMessage")) {
            if (args.length == 1 && args[0] instanceof String) {
                ps.println(args[0]);
            } else {
                for (Object o : args) {
                    ps.println("" + o);
                }
            }
            propertyChangeSupport.firePropertyChange("stdout", null, getStdout());
            return null;
        }
        return method.invoke(proxee, args);
    }

    public String getStdout() {
        return baos.toString();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void clear() {
        baos.reset();
    }
}
