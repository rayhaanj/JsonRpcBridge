package me.rayhaan.java.JsonRpcBridge.Reflect;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.LinkedList;

import com.google.gson.JsonArray;

public class MethodInvoker {

	private Class<?> clazz;
	private Object instance;

	public MethodInvoker(Object instance) {
		this.clazz = instance.getClass();
		this.instance = instance;
	}

	public Method resolve(String methodName, Object[] arguments)
			throws Exception {
		Method m = null;

		// All the methods in the class
		Method[] classMethods = this.clazz.getDeclaredMethods();

        for (Method me : classMethods) System.out.print(me.getName() + " ");
        System.out.println("");

		// Store the ones with the sam methodName as the one we are looking for
		LinkedList<Method> candidates = new LinkedList<Method>();

		for (Method candidate : clazz.getDeclaredMethods()) {
			if (candidate.getName().equals(methodName)) {
				candidates.add(candidate);
			}
		}

        System.out.println("Number of candidates found:" + candidates.size());
		if (candidates.size() == 0) {
            System.out.println("NO METHOD FOUND!");
			throw new Exception("Method not found!");
        }

		if (candidates.size() == 1)
			return candidates.get(0);

		// Must have more than one method with the same name, must do matching
		// based on method signature

		for (Method candidate : candidates) {
			if (matchSig(candidate, arguments)) {
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
        System.out.println("MatchSig");
		Class<?>[] params = m.getParameterTypes();
        System.out.println(params.length);

		if (params.length != args.length) return false;
		
		for (int i=0; i < params.length; i++) {
            System.out.println("Comparing actual first argument class " + params[i] + " To " + args[i].getClass() );
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
