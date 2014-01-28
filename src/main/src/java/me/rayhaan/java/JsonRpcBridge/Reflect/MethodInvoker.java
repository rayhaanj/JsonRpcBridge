package me.rayhaan.java.JsonRpcBridge.Reflect;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.logging.Logger;

public class MethodInvoker {

	private Class<?> clazz;
	private Object instance;
    private Logger log = Logger.getLogger("MethodInvoker");


	public MethodInvoker(Object instance) {
		this.clazz = instance.getClass();
		this.instance = instance;
	}

	public Method resolve(String methodName, Object[] arguments)
			throws Exception {

		// All the methods in the class
		Method[] classMethods = this.clazz.getDeclaredMethods();

		// Store the ones with the sam methodName as the one we are looking for
		LinkedList<Method> candidates = new LinkedList<>();

		for (Method candidate : clazz.getDeclaredMethods()) {
			if (candidate.getName().equals(methodName)) {
				candidates.add(candidate);
			}
		}

        System.err.println("Number of candidates found:" + candidates.size());
		if (candidates.size() == 0) {
            this.log.severe("Method not found!");
			throw new Exception("Method not found!");
        }

		if (candidates.size() == 1) {
            this.log.fine("Method found" + candidates.get(0));
			return candidates.get(0);
        }

		// Must have more than one method with the same name, must do matching
		// based on method signature

		for (Method candidate : candidates) {
			if (matchSig(candidate, arguments)) {
                this.log.fine("Method found based on signature matching " + candidate);
                return candidate;
			}	
		}
        throw new Exception("Could not match to any method");
	}

	/**
	 * Determine if the arguments specified match the signature of a known java method.
	 * @param m The method to compare to
	 * @param args The arguments to use as the signature to validate
	 * @return true if the arguments are for the given method
	 */
	public boolean matchSig(Method m, Object[] args) {
		Class<?>[] params = m.getParameterTypes();
        System.out.println(params.length);

		if (params.length != args.length) return false;
		
		for (int i=0; i < params.length; i++) {
            // Compare the type of the known method parameter to the type of the one specified
			if (params[i] != args[i].getClass()) return false;
		}
		return true;
	}

    /**
     * Call a method registered on this JsonRpcBridge.
     * @param m
     * @param arguments
     * @return
     * @throws Exception
     */
	public Object invoke(Method m, Object[] arguments) throws Exception {
		return m.invoke(instance, arguments);
	}

}
